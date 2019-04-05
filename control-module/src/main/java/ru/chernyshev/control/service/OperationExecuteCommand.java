package ru.chernyshev.control.service;

import ru.chernyshev.control.dto.Operation;
import ru.chernyshev.control.model.Log;
import ru.chernyshev.ifaces.dto.Response;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

public class OperationExecuteCommand implements Runnable {

    private static final String PREFIX_MSG = "Execute operation.";

    private final List<Operation> operations;
    private final TelemetryService telemetryService;
    private final MessageSender messageSender;
    private final ScheduledExecutorService executor;
    private final RestClientService restClientService;


    public OperationExecuteCommand(TelemetryService telemetryService, List<Operation> operations, MessageSender messageSender, RestClientService restClientService) {
        this.telemetryService = telemetryService;
        this.operations = operations;
        this.messageSender = messageSender;
        this.restClientService = restClientService;

        this.executor = Executors.newScheduledThreadPool(5);
    }

    @Override
    public void run() {
        messageSender.stdout(Log.trace(PREFIX_MSG + " Start: " + getCurrentOperations()));

        Map<String, Integer> requestParam = operations.stream()
                .collect(Collectors.toMap(Operation::getVariable, Operation::getValue));

        messageSender.stdout(Log.info(PREFIX_MSG + " Params: " + requestParam));

        Response response = null;
        Error apiError = null;

        try {
            response = restClientService.send(requestParam);
        } catch (Exception e) {
            apiError = Error.createApiError(operations, e);
        }

        if (apiError != null) {
            telemetryService.send(TelemetryType.ERROR, PREFIX_MSG + apiError.getMessage());
            if (apiError.isCritical()) {
                System.exit(apiError.getExitCode());
            }
        }

        messageSender.stdout(Log.trace(PREFIX_MSG + " Get response: " + (response != null ? response.getResponse() : "")));

        Map<Integer, List<Operation>> operationsByTimeout = operations
                .stream()
                .collect(groupingBy(Operation::getTimeout));

        for (Map.Entry<Integer, List<Operation>> entry : operationsByTimeout.entrySet()) {
            Integer timeout = entry.getKey();

            executor.schedule(
                    new OperationExecutingCheckCommand(restClientService, telemetryService, entry.getValue(), messageSender)
                    , timeout, TimeUnit.MILLISECONDS);
        }

        messageSender.stdout(Log.trace(PREFIX_MSG + "Complete for ids: " + getCurrentOperations()));
    }

    private String getCurrentOperations() {
        return operations.stream().map(o -> o.getId().toString())
                .collect(Collectors.joining(", "));
    }
}