package ru.chernyshev.control.service;

import ru.chernyshev.control.dto.Operation;
import ru.chernyshev.control.model.Log;
import ru.chernyshev.ifaces.dto.ConfigurationValues;
import ru.chernyshev.ifaces.dto.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExecuteOperationCheckCommand implements Runnable {

    private final RestClientService restClientService;
    private final TelemetryService telemetryService;
    private final MessageSender messageSender;

    private final List<Operation> operations;

    public ExecuteOperationCheckCommand(RestClientService restClientService, TelemetryService telemetryService, List<Operation> operations, MessageSender messageSender) {
        this.restClientService = restClientService;
        this.telemetryService = telemetryService;
        this.messageSender = messageSender;
        this.operations = operations;
    }

    @Override
    public void run() {
        messageSender.stdout(Log.trace("Start check execut command: "));

        String checkedOperations = operations.stream().map(Operation::getVariable)
                .collect(Collectors.joining(","));

        messageSender.stdout(Log.info("Check command: " + checkedOperations));

        Response response = null;
        Error apiError = null;

        try {
            response = restClientService.get(checkedOperations);
        } catch (Exception e) {
            apiError = Error.createApiError(operations, e);
        }
        messageSender.stdout(Log.trace("Check command, get " + (response != null ? response.getResponse() : null)));

        if (apiError == null && response == null) {
            apiError = Error.createApiError(operations, null);
        }

        if (apiError != null) {
            telemetryService.send(TelemetryType.ERROR, apiError.getMessage());
            if (apiError.isCritical()) {
                System.exit(apiError.getExitCode());
            }
            return;
        }

        List<Operation> notExecutedOperation = getNotExecutedOperation(response.getResponse());

        if (!notExecutedOperation.isEmpty()) {
            Error operationError = Error.createOperationError(notExecutedOperation);
            telemetryService.send(TelemetryType.ERROR, operationError.getMessage());
            if (operationError.isCritical()) {
                System.exit(operationError.getExitCode());
            }
        }
        messageSender.stdout(Log.trace("Finish check execut command"));
    }

    private List<Operation> getNotExecutedOperation(Map<String, ConfigurationValues> configuration) {
        List<Operation> notExecutedOperations = new ArrayList<>();

        for (Operation o : operations) {
            if (!configuration.containsKey(o.getVariable())) {
                notExecutedOperations.add(o);
                continue;
            }
            ConfigurationValues configValues = configuration.get(o.getVariable());
            if (configValues.getValue() == null || configValues.getValue().intValue() != o.getValue()) {
                notExecutedOperations.add(o);
            }
        }
        return notExecutedOperations;
    }
}
