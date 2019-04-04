package ru.chernyshev.control.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import ru.chernyshev.control.dto.Operation;

import java.util.List;
import java.util.stream.Collectors;

public class Error {

    private String message;
    private boolean isCritical;
    private int exitCode;

    private Error(boolean isCritical, ExitCodes exitCode) {
        this.isCritical = isCritical;
        this.exitCode = exitCode.getExitCode();
    }

    /**
     * Создать объект ошибки взаимодействия с rest сервисом спутника
     */
    public static Error createApiError(List<Operation> operations, Exception e) {
        Error error = createError(operations);
        String operationIds = getOperationIds(operations);

        if (e == null) {
            error.message = String.format("Unknown error. Ids: %s.", operationIds);
        } else if (e instanceof JsonProcessingException) {
            error.message = String.format("Cant process to json. Ids: %s. Error: %s ", operationIds, e.getMessage());
        } else if (e instanceof ResourceAccessException) {
            error.message = String.format("Cant get resource. Ids: %s. Error: %s ", operationIds, e.getMessage());
        } else if (e instanceof HttpClientErrorException.BadRequest) {
            String responseBody = ((HttpClientErrorException.BadRequest) e).getResponseBodyAsString();
            error.message = String.format("Incorrect parameters. Ids: %s. Params: %s ", operationIds, responseBody);
        } else {
            error.message = String.format("Error. Ids: %s. Error: %s ", operationIds, e.getMessage());
        }
        return error;
    }

    /**
     * Создать объект ошибки не выполения команды программы
     */
    public static Error createOperationError(List<Operation> operations) {
        Error error = createError(operations);
        String operationIds = getOperationIds(operations);

        error.message = String.format("Time to set a parameter is out. Ids: %s.", operationIds);

        return error;
    }

    private static Error createError(List<Operation> errorParams) {
        boolean isCritical = errorParams.stream().anyMatch(Operation::getCritical);
        return new Error(isCritical, ExitCodes.API_ANSWER_ERROR);
    }

    private static String getOperationIds(List<Operation> errorParams) {
        return errorParams.stream().map(o -> o.getId().toString())
                .collect(Collectors.joining(", "));
    }

    public String getMessage() {
        return message;
    }

    public boolean isCritical() {
        return isCritical;
    }

    public int getExitCode() {
        return exitCode;
    }

}
