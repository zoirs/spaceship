package ru.chernyshev.control.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
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

public class ExecuteOperationCommand implements Runnable {

    private final List<Operation> operations;
    private final TelemetryService telemetryService;
    private final MessageSender messageSender;
    private final ScheduledExecutorService executor;
    private final RestClientService restClientService;

    public ExecuteOperationCommand(TelemetryService telemetryService, List<Operation> operations, MessageSender messageSender, RestClientService restClientService) {
        this.telemetryService = telemetryService;
        this.operations = operations;
        this.messageSender = messageSender;
        this.restClientService = restClientService;

        this.executor = Executors.newScheduledThreadPool(5);
    }

    @Override
    public void run() {
        messageSender.stdout(Log.trace("Start execut command: " + getCurrentOperations()));

        Map<String, Integer> requestParam = operations.stream()
                .collect(Collectors.toMap(Operation::getVariable, Operation::getValue));

        messageSender.stdout(Log.info("Execute command with param: " + requestParam));

        Response response = null;
        Error apiError = null;

        try {
            response = restClientService.send(requestParam);
        } catch (Exception e) {
            apiError = Error.createApiError(operations, e);
        }

        if (apiError != null) {
            telemetryService.send(TelemetryType.ERROR, apiError.getMessage());
            if (apiError.isCritical()){
                System.exit(apiError.getExitCode());
            }
        }

        messageSender.stdout(Log.trace("Get response: " + (response != null ? response.getResponse() : "")));

        Map<Integer, List<Operation>> operationsByTimeout = operations
                .stream()
                .collect(groupingBy(Operation::getTimeout));

        for (Map.Entry<Integer, List<Operation>> entry : operationsByTimeout.entrySet()) {
            Integer timeout = entry.getKey();

            executor.schedule(
                    new ExecuteOperationCheckCommand(restClientService, telemetryService, entry.getValue(), messageSender)
                    , timeout, TimeUnit.MILLISECONDS);
        }

        messageSender.stdout(Log.trace("Finish execut command: " + getCurrentOperations()));
    }

    private String getCurrentOperations() {
        return operations.stream().map(o -> o.getId().toString())
                .collect(Collectors.joining(", "));
    }
}
