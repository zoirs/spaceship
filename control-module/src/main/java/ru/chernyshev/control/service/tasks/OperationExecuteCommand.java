package ru.chernyshev.control.service.tasks;

import ru.chernyshev.control.dto.LogMessage;
import ru.chernyshev.control.dto.Operation;
import ru.chernyshev.control.service.IMessageSender;
import ru.chernyshev.control.service.IRestClientService;
import ru.chernyshev.control.service.ITelemetryService;
import ru.chernyshev.control.type.TelemetryType;
import ru.chernyshev.control.utils.ErrorWrapper;
import ru.chernyshev.ifaces.dto.Response;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * Команда выполнения задач из программы полета
 */
public class OperationExecuteCommand implements Runnable {

    private static final String PREFIX_MSG = "Execute operation.";

    /**
     * Массив команд на выполнение
     */
    private final List<Operation> operations;

    /**
     * Сервис телеметрии
     */
    private final ITelemetryService telemetryService;

    /**
     * Сервис отправки сообщение
     */
    private final IMessageSender messageSender;

    /**
     * Сервис взаимодействия с restApi
     */
    private final IRestClientService restClientService;

    /**
     * Фукнция требующая выполнения по завершению выполнения команды
     */
    private final Runnable checkedCallback;

    private final ScheduledExecutorService executor;

    public OperationExecuteCommand(ITelemetryService telemetryService, List<Operation> operations, IMessageSender messageSender, IRestClientService restClientService, Runnable checkedCallback) {
        this.telemetryService = telemetryService;
        this.operations = operations;
        this.messageSender = messageSender;
        this.restClientService = restClientService;
        this.checkedCallback = checkedCallback;

        this.executor = Executors.newScheduledThreadPool(5);
    }

    /**
     * Исполняет требуемые задачи, запускает команду проверки выполнения задач
     */
    @Override
    public void run() {
        messageSender.stdout(LogMessage.trace(PREFIX_MSG + " Start: " + getCurrentOperations()));

        Map<String, Integer> requestParam = operations.stream()
                .collect(Collectors.toMap(Operation::getVariable, Operation::getValue));

        messageSender.stdout(LogMessage.info(PREFIX_MSG + " Params: " + requestParam));

        Response response = null;
        ErrorWrapper apiError = null;

        try {
            response = restClientService.send(requestParam);
        } catch (Exception e) {
            apiError = ErrorWrapper.createApiError(operations, e);
        }

        if (apiError != null) {
            telemetryService.send(TelemetryType.ERROR, PREFIX_MSG + apiError.getMessage());
            if (apiError.isCritical()) {
                System.exit(apiError.getExitCode());
            }
        }

        messageSender.stdout(LogMessage.trace(PREFIX_MSG + " Get response: " + (response != null ? response.getResponse() : "")));

        Map<Integer, List<Operation>> operationsByTimeout = operations
                .stream()
                .collect(groupingBy(Operation::getTimeout));

        for (Map.Entry<Integer, List<Operation>> entry : operationsByTimeout.entrySet()) {
            Integer timeout = entry.getKey();

            OperationExecutingCheckCommand checkCommand = new OperationExecutingCheckCommand(restClientService, telemetryService, entry.getValue(), messageSender, checkedCallback);
            executor.schedule(checkCommand, timeout, TimeUnit.SECONDS);
        }

        messageSender.stdout(LogMessage.trace(PREFIX_MSG + "Complete for ids: " + getCurrentOperations()));
    }

    private String getCurrentOperations() {
        return operations.stream().map(o -> o.getId().toString())
                .collect(Collectors.joining(", "));
    }
}
