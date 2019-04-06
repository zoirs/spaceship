package ru.chernyshev.control.utils;

import ru.chernyshev.control.dto.Operation;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockOperation {

    public static Operation createOperation(int id, String paramName) {
        return createOperation(id, paramName, 0);
    }

    public static Operation createOperation(int id, String paramName, boolean isCritical) {
        return getOperation(id, paramName, 0, isCritical, 0);
    }

    public static Operation createOperation(int id, String paramName, int timeout) {
        return getOperation(id, paramName, timeout, false, 0);
    }

    public static Operation createOperation(int id, String paramName, int timeout, int deltaT) {
        return getOperation(id, paramName, timeout, false, deltaT);
    }

    private static Operation getOperation(int id, String paramName, int timeout, boolean isCritical, int deltaT) {
        Operation operation = mock(Operation.class);
        when(operation.getId()).thenReturn(id);
        when(operation.getDeltaT()).thenReturn(deltaT);
        when(operation.getTimeout()).thenReturn(timeout);
        when(operation.getVariable()).thenReturn(paramName);
        when(operation.getValue()).thenReturn(10);
        when(operation.getCritical()).thenReturn(isCritical);
        return operation;
    }
}
