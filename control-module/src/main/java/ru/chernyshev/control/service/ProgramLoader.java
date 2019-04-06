package ru.chernyshev.control.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import ru.chernyshev.control.dto.FlyProgram;
import ru.chernyshev.control.dto.Operation;
import ru.chernyshev.control.model.Log;
import ru.chernyshev.control.utils.OperationValidator;
import ru.chernyshev.control.utils.ProgramErrorType;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
public class ProgramLoader {

    private static final String PREFIX_MSG = "Program load. ";

    private final MessageSender messageSender;
    private final RestClientService restClientService;
    private final TelemetryService telemetryService;
    private final ObjectMapper objectMapper;
    private final String flightProgramPath;

    private final ScheduledExecutorService executor;

    private FlyProgram flyProgram;

    @Autowired
    public ProgramLoader(RestClientService restClientService,
                         TelemetryService telemetryService,
                         MessageSender messageSender,
                         ObjectMapper objectMapper,
                         String flightProgramPath) {
        this.restClientService = restClientService;
        this.telemetryService = telemetryService;
        this.messageSender = messageSender;
        this.objectMapper = objectMapper;
        this.flightProgramPath = flightProgramPath;

        this.executor = Executors.newScheduledThreadPool(5);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        messageSender.stdout(Log.trace(PREFIX_MSG + "Start read"));

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

        messageSender.stdout(Log.trace(PREFIX_MSG + "Finish read program"));

        telemetryService.start();
        flyProgram.setStartUp((int) (new Date().getTime() / 1000)); // todo убрать, это для теста
        execute(flyProgram);
        messageSender.stdout(Log.trace(PREFIX_MSG + "Finish read"));
    }

    public FlyProgram getFlyProgram() {
        return flyProgram;
    }

    public void execute(FlyProgram flyProgram) {
        messageSender.stdout(Log.trace(PREFIX_MSG + "Start execute"));

        Date startupDate = Date.from(Instant.ofEpochSecond(flyProgram.getStartUp()));

        Map<Integer, List<Operation>> operationsByDelays = flyProgram.getOperations()
                .stream()
                .collect(groupingBy(Operation::getDeltaT));

        for (Map.Entry<Integer, List<Operation>> entry : operationsByDelays.entrySet()) {
            long delay = getDelay(startupDate, entry.getKey());

            executor.schedule(
                    new OperationExecuteCommand(telemetryService, entry.getValue(), messageSender, restClientService)
                    , delay, TimeUnit.MILLISECONDS);
        }
        messageSender.stdout(Log.trace(PREFIX_MSG + "Execute was schedule"));
    }

    private long getDelay(Date startupDate, Integer delayAfterStartup) {
        return new Date().getTime() - startupDate.getTime() + delayAfterStartup * 1000;
    }
}
