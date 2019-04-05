package ru.chernyshev.control.utils;

import ru.chernyshev.control.dto.Operation;
import ru.chernyshev.control.service.ConfigurationParam;

public class OperationValidator {
    public static boolean isValid(Operation operation) {
        return operation.getTimeout() > 0 &&
                operation.getId() > 0 &&
                ConfigurationParam.isValid(operation.getVariable(), operation.getValue());
    }
}
