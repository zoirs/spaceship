package ru.chernyshev.control.utils;

import ru.chernyshev.control.dto.FlyProgram;
import ru.chernyshev.control.dto.Operation;

import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockProgram {
    public static FlyProgram createFlyProgram(List<Operation> operations) {
        FlyProgram flyProgram = mock(FlyProgram.class);
        when(flyProgram.getStartUp()).thenReturn((int) (new Date().getTime() / 1000));
        when(flyProgram.getOperations()).thenReturn(operations);
        return flyProgram;
    }
}
