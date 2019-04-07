package ru.chernyshev.control.service.tasks;

import org.springframework.util.CollectionUtils;
import ru.chernyshev.control.dto.Operation;
import ru.chernyshev.control.dto.LogMessage;
import ru.chernyshev.control.service.IMessageSender;
import ru.chernyshev.control.service.IRestClientService;
import ru.chernyshev.control.service.ITelemetryService;
import ru.chernyshev.control.type.TelemetryType;
import ru.chernyshev.control.utils.ErrorWrapper;
import ru.chernyshev.ifaces.dto.ConfigurationValues;
import ru.chernyshev.ifaces.dto.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Команда проверки выполнения задач
 * */
public class OperationExecutingCheckCommand implements Runnable {

    private static final String PREFIX_MSG = "Check operation result.";

    /**
     * Сервис взаимодействия с restApi
     */
    private final IRestClientService restClientService;

    /**
     * Сервис телеметрии
     */
    private final ITelemetryService telemetryService;

    /**
     * Сервис отправки сообщение
     */
    private final IMessageSender messageSender;

    /**
     * Фукнция требующая выполнения по завершению выполнения команды
     */
    private final Runnable checkedCallback;

    /**
     * Массив выполенных команд
     */
    private final List<Operation> operations;

    public OperationExecutingCheckCommand(IRestClientService restClientService, ITelemetryService telemetryService, List<Operation> operations, IMessageSender messageSender, Runnable checkedCallback) {
        this.restClientService = restClientService;
        this.telemetryService = telemetryService;
        this.messageSender = messageSender;
        this.operations = operations;
        this.checkedCallback = checkedCallback;
    }

    /**
     * Исполняет проверку выполнения задач
     */
    @Override
    public void run() {
        messageSender.stdout(LogMessage.trace(PREFIX_MSG + "Start"));

        String checkedOperations = operations.stream().map(Operation::getVariable)
                .collect(Collectors.joining(","));

        messageSender.stdout(LogMessage.info(PREFIX_MSG + "Params: " + checkedOperations));

        Response response = null;
        ErrorWrapper apiError = null;

        try {
            response = restClientService.get(checkedOperations);
        } catch (Exception e) {
            apiError = ErrorWrapper.createApiError(operations, e);
        }
        messageSender.stdout(LogMessage.trace(PREFIX_MSG + " Get response: " + (response != null ? response.getResponse() : null)));

        if (apiError != null) {
            telemetryService.send(TelemetryType.ERROR, PREFIX_MSG + apiError.getMessage());
            if (apiError.isCritical()) {
                System.exit(apiError.getExitCode());
            }
            return;
        }

        List<Operation> notExecutedOperation = getNotExecutedOperation(response);

        if (!notExecutedOperation.isEmpty()) {
            ErrorWrapper operationError = ErrorWrapper.createOperationError(notExecutedOperation);
            telemetryService.send(TelemetryType.ERROR, PREFIX_MSG + operationError.getMessage());
            if (operationError.isCritical()) {
                System.exit(operationError.getExitCode());
            }
        }
        messageSender.stdout(LogMessage.trace(PREFIX_MSG + "Complete for ids: " + getCurrentOperations()));

        Optional.ofNullable(checkedCallback).ifPresent(Runnable::run);
    }

    private List<Operation> getNotExecutedOperation(Response response) {

        if (response == null || CollectionUtils.isEmpty(response.getResponse())) {
            return operations;
        }

        List<Operation> notExecutedOperations = new ArrayList<>();
        Map<String, ConfigurationValues> configuration = response.getResponse();

        for (Operation o : operations) {
            if (!configuration.containsKey(o.getVariable())) {
                notExecutedOperations.add(o);
                continue;
            }
            ConfigurationValues configValues = configuration.get(o.getVariable());
            if (configValues.getValue() == null || configValues.getValue() != o.getValue()) {
                notExecutedOperations.add(o);
            }
        }
        return notExecutedOperations;
    }

    private String getCurrentOperations() {
        return operations.stream().map(o -> o.getId().toString())
                .collect(Collectors.joining(", "));
    }
}
