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
import java.util.stream.Collectors;

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

    public OperationExecuteCommand(ITelemetryService telemetryService, List<Operation> operations, IMessageSender messageSender, IRestClientService restClientService) {
        this.telemetryService = telemetryService;
        this.operations = operations;
        this.messageSender = messageSender;
        this.restClientService = restClientService;
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

        messageSender.stdout(LogMessage.trace(PREFIX_MSG + "Complete for ids: " + getCurrentOperations()));
    }

    private String getCurrentOperations() {
        return operations.stream().map(o -> o.getId().toString())
                .collect(Collectors.joining(", "));
    }
}
