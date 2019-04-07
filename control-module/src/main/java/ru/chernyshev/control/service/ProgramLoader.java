package ru.chernyshev.control.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import ru.chernyshev.control.dto.FlyProgram;
import ru.chernyshev.control.dto.LogMessage;
import ru.chernyshev.control.dto.Operation;
import ru.chernyshev.control.service.tasks.OperationExecuteCommand;
import ru.chernyshev.control.type.ExitCodes;
import ru.chernyshev.control.type.ProgramErrorType;
import ru.chernyshev.control.type.TelemetryType;
import ru.chernyshev.control.utils.OperationValidator;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * Сервис загрузки программы полета и запуска ее на выполенние
 */
@Service
public class ProgramLoader implements IProgramLoader {

    private static final String PREFIX_MSG = "Program load. ";

    /**
     * Счетчик завершенных программ
     */
    private CountDownLatch commandCounter;

    /**
     * Сервис отправки сообщение
     */
    private final IMessageSender messageSender;

    /**
     * Сервис взаимодействия с restApi
     */
    private final IRestClientService restClientService;

    /**
     * Сервис телеметрии
     */
    private final ITelemetryService telemetryService;
    /**
     * Путь где лежит файл программы полета
     */
    private final String flightProgramPath;

    /**
     * Программа полета
     */
    private FlyProgram flyProgram;

    private final ObjectMapper objectMapper;
    private final ScheduledExecutorService executor;


    @Autowired
    public ProgramLoader(IRestClientService restClientService,
                         ITelemetryService telemetryService,
                         IMessageSender messageSender,
                         ObjectMapper objectMapper,
                         String flightProgramPath) {
        this.restClientService = restClientService;
        this.telemetryService = telemetryService;
        this.messageSender = messageSender;
        this.objectMapper = objectMapper;
        this.flightProgramPath = flightProgramPath;

        this.executor = Executors.newScheduledThreadPool(5);
    }

    /**
     * Инициирует загрузку программы полета при старте приложения
     */
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        messageSender.stdout(LogMessage.trace(PREFIX_MSG + "Start read"));

        File fileProgram = new File(flightProgramPath);
        if (!fileProgram.exists() || !fileProgram.isFile()) {
            telemetryService.send(TelemetryType.ERROR, PREFIX_MSG + "Fly program not found " + flightProgramPath);
            System.exit(ExitCodes.WRONG_PROGRAM.getExitCode());
        }

        try {
            flyProgram = objectMapper.readValue(fileProgram, FlyProgram.class);
        } catch (IOException e) {
            telemetryService.send(TelemetryType.ERROR, PREFIX_MSG + "Cant read program " + e.getMessage());
            System.exit(ExitCodes.WRONG_PROGRAM.getExitCode());
        }

        Map<ProgramErrorType, List<Operation>> wrongOperation = OperationValidator.findWrongOperation(flyProgram);

        if (wrongOperation.size() > 0) {
            for (ProgramErrorType error : wrongOperation.keySet()) {
                String wrongOperationIds = wrongOperation.get(error).stream().map(Operation::toString)
                        .collect(Collectors.joining(", "));
                telemetryService.send(TelemetryType.ERROR, PREFIX_MSG + error + wrongOperationIds);
            }
            System.exit(ExitCodes.WRONG_PROGRAM.getExitCode());
        }

        messageSender.stdout(LogMessage.trace(PREFIX_MSG + "Finish read program"));

        telemetryService.start();
        //flyProgram.setStartUp((int) (new Date().getTime() / 1000)); // todo убрать, это для теста
        execute(flyProgram);
        messageSender.stdout(LogMessage.trace(PREFIX_MSG + "Finish read"));
    }

    /**
     * Запустить программу полета на выполнение
     */
    public void execute(FlyProgram flyProgram) {
        messageSender.stdout(LogMessage.trace(PREFIX_MSG + "Start execute"));

        Date startupDate = Date.from(Instant.ofEpochSecond(flyProgram.getStartUp()));

        Map<Integer, List<Operation>> operationsByDelays = flyProgram.getOperations()
                .stream()
                .collect(groupingBy(Operation::getDeltaT));

        for (Map.Entry<Integer, List<Operation>> entry : operationsByDelays.entrySet()) {
            long delay = getDelay(startupDate, entry.getKey());

            OperationExecuteCommand command = new OperationExecuteCommand(telemetryService, entry.getValue(), messageSender, restClientService, checkedCallback());
            executor.schedule(command, delay, TimeUnit.MILLISECONDS);
        }
        messageSender.stdout(LogMessage.trace(PREFIX_MSG + "Execute was schedule"));

        waitExecution(operationsByDelays.entrySet().size());
    }

    /**
     * @return программу полета
     */
    public FlyProgram getFlyProgram() {
        return flyProgram;
    }

    void waitExecution(int commandCount) {
        commandCounter = new CountDownLatch(commandCount);
        try {
            commandCounter.await();
        } catch (InterruptedException ignore) {
        }
        messageSender.stdout(LogMessage.trace("Last command checked. Program exit"));
        System.exit(ExitCodes.SUCCESS.getExitCode());
    }

    private long getDelay(Date startupDate, Integer delayAfterStartup) {
        return new Date().getTime() - startupDate.getTime() + delayAfterStartup * 1000;
    }

    private Runnable checkedCallback() {
        return () -> commandCounter.countDown();
    }
}
