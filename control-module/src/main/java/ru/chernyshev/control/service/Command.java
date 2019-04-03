package ru.chernyshev.control.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import ru.chernyshev.control.dto.Operation;
import ru.chernyshev.control.model.Log;
import ru.chernyshev.ifaces.dto.Response;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Command implements Runnable {

    private final List<Operation> operations;
    private final RestTemplate restTemplate;
    private final TelemetryService telemetryService;
    private final MessageSender messageSender;

    public Command(RestTemplate restTemplate, TelemetryService telemetryService, List<Operation> operations, MessageSender messageSender) {
        this.restTemplate = restTemplate;
        this.telemetryService = telemetryService;
        this.operations = operations;
        this.messageSender = messageSender;
    }

    @Override
    public void run() {
        messageSender.stdout(Log.trace("Start execut command: " + getCurrentOperations()));

        HttpEntity<String> entity = createRequest();

        if (entity == null) {
            String errorMessage = String.format("Command not executed. Ids: %s.", getCurrentOperations());
            telemetryService.send(TelemetryType.ERROR, errorMessage);
            return;
        }

        try {
            Response response = restTemplate.patchForObject("http://localhost:9090/settings", entity, Response.class);
            messageSender.stdout(Log.trace("Get response: " + (response != null ? response.getResponse() : "")));
        } catch (ResourceAccessException e) {
            String errorMessage = String.format("Cant get resource. Ids: %s. Error: %s ", getCurrentOperations(), e.getMessage());
            telemetryService.send(TelemetryType.ERROR, errorMessage);
        } catch (HttpClientErrorException.BadRequest e) {
            String errorMessage = String.format("Incorrect parameters. Ids: %s. Params: %s ", getCurrentOperations(), e.getResponseBodyAsString());
            telemetryService.send(TelemetryType.ERROR, errorMessage);
        } catch (Exception e) {
            String errorMessage = String.format("Error. Ids: %s. Error: %s ", getCurrentOperations(), e.getMessage());
            telemetryService.send(TelemetryType.ERROR, errorMessage);
        }

        //todo через operation.timeout проверить что значение совпадает с выставленным
        messageSender.stdout(Log.trace("Finish execut command: " + getCurrentOperations()));
    }

    private String getCurrentOperations() {
        return operations.stream().map(o -> o.getId().toString())
                .collect(Collectors.joining(", "));
    }

    private HttpEntity<String> createRequest() {
        Map<String, Integer> requestParam = operations.stream()
                .collect(Collectors.toMap(Operation::getVariable, Operation::getValue));

        ObjectMapper objectMapper = new ObjectMapper();

        String jsonParams;
        try {
            jsonParams = objectMapper.writeValueAsString(requestParam);
        } catch (JsonProcessingException e) {
            messageSender.stdout(Log.error("Cant create json: " + e.getMessage()));
            return null;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(jsonParams, headers);
    }
}
