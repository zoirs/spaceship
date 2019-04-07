package ru.chernyshev.control.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import ru.chernyshev.control.dto.Operation;
import ru.chernyshev.control.type.ExitCodes;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Обертка для ошибок
 */
public class ErrorWrapper {

    /**
     * Сообщение об ошибке
     */
    private String message;

    /**
     * Является ли ошибка сигналом завершению работы приложения
     */
    private final boolean isCritical;

    /**
     * Код завершения работы приложения в случае критической ошибки
     */
    private final int exitCode;

    private ErrorWrapper(boolean isCritical, ExitCodes exitCode) {
        this.isCritical = isCritical;
        this.exitCode = exitCode.getExitCode();
    }

    /**
     * @return объект ошибки взаимодействия с rest сервисом спутника
     */
    public static ErrorWrapper createApiError(List<Operation> operations, Exception e) {
        ErrorWrapper error = new ErrorWrapper(isCritical(operations), ExitCodes.API_ANSWER_ERROR);
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
    public static ErrorWrapper createOperationError(List<Operation> operations) {
        ErrorWrapper error = new ErrorWrapper(isCritical(operations), ExitCodes.OPERATION_NOT_EXECUTE);

        error.message = String.format("Value was not set. Ids: %s.", getOperationIds(operations));
        return error;
    }

    /**
     * @return текстовое описание ошибки
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return true если программа должна завершиться из за ошибки
     */
    public boolean isCritical() {
        return isCritical;
    }

    /**
     * @return Код завершения работы приложения в случае критической ошибки
     */
    public int getExitCode() {
        return exitCode;
    }

    private static boolean isCritical(List<Operation> errorParams) {
        return errorParams.stream().anyMatch(Operation::isCritical);
    }

    private static String getOperationIds(List<Operation> errorParams) {
        return errorParams.stream().map(o -> o.getId().toString())
                .collect(Collectors.joining(", "));
    }


}
