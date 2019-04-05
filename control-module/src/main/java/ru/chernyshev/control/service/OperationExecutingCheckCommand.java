package ru.chernyshev.control.service;

import ru.chernyshev.control.dto.Operation;
import ru.chernyshev.control.model.Log;
import ru.chernyshev.ifaces.dto.ConfigurationValues;
import ru.chernyshev.ifaces.dto.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OperationExecutingCheckCommand implements Runnable {

    private static final String PREFIX_MSG = "Check operation result.";

    private final RestClientService restClientService;
    private final TelemetryService telemetryService;
    private final MessageSender messageSender;

    private final List<Operation> operations;

    public OperationExecutingCheckCommand(RestClientService restClientService, TelemetryService telemetryService, List<Operation> operations, MessageSender messageSender) {
        this.restClientService = restClientService;
        this.telemetryService = telemetryService;
        this.messageSender = messageSender;
        this.operations = operations;
    }

    @Override
    public void run() {
        messageSender.stdout(Log.trace(PREFIX_MSG + "Start"));

        String checkedOperations = operations.stream().map(Operation::getVariable)
                .collect(Collectors.joining(","));

        messageSender.stdout(Log.info(PREFIX_MSG + "Params: " + checkedOperations));

        Response response = null;
        Error apiError = null;

        try {
            response = restClientService.get(checkedOperations);
        } catch (Exception e) {
            apiError = Error.createApiError(operations, e);
        }
        messageSender.stdout(Log.trace(PREFIX_MSG + " Get response: " + (response != null ? response.getResponse() : null)));

        if (apiError != null) {
            telemetryService.send(TelemetryType.ERROR, PREFIX_MSG + apiError.getMessage());
            if (apiError.isCritical()) {
                System.exit(apiError.getExitCode());
            }
            return;
        }

        List<Operation> notExecutedOperation = getNotExecutedOperation(response);

        if (!notExecutedOperation.isEmpty()) {
            Error operationError = Error.createOperationError(notExecutedOperation);
            telemetryService.send(TelemetryType.ERROR, PREFIX_MSG +  operationError.getMessage());
            if (operationError.isCritical()) {
                System.exit(operationError.getExitCode());
            }
        }
        messageSender.stdout(Log.trace(PREFIX_MSG + "Complete for ids: " + getCurrentOperations()));
    }

    private List<Operation> getNotExecutedOperation(Response response) {

        if (response == null) {
            return operations;
        }

        List<Operation> notExecutedOperations = new ArrayList<>();
        Map<String, ConfigurationValues> configuration = response.getResponse();

        for (String paramName : configuration.keySet()) {

            Operation operation = operations.stream()
                    .filter(o -> o.getVariable().equals(paramName))
                    .findFirst()
                    .orElse(null);

            if (operation == null ){
                continue;
            }
            ConfigurationValues values = configuration.get(paramName);
            if (values.getValue() != operation.getValue()) {
                notExecutedOperations.add(operation);
            }
        }

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
