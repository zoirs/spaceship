package ru.chernyshev.control.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import ru.chernyshev.control.dto.FlyProgram;
import ru.chernyshev.control.dto.Operation;

import java.util.List;
import java.util.stream.Collectors;

//todo валидатор?
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
        Error error = new Error(isCritical(operations), ExitCodes.API_ANSWER_ERROR);
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
        Error error = new Error(isCritical(operations), ExitCodes.OPERATION_NOT_EXECUTE);

        error.message = String.format("Value was not set. Ids: %s.", getOperationIds(operations));
        return error;
    }

    public static Error createProgramLoadError(FlyProgram program, Exception e) {
        Error error = new Error(true, ExitCodes.WRONG_PROGRAM);
        error.message = String.format("Program not correct. Ids: %s.", e);
        return error;
    }

    private static boolean isCritical(List<Operation> errorParams) {
        return errorParams.stream().anyMatch(Operation::getCritical);
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
