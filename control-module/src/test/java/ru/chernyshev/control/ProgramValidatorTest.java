package ru.chernyshev.control;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;
import ru.chernyshev.control.dto.FlyProgram;
import ru.chernyshev.control.dto.Operation;
import ru.chernyshev.control.utils.OperationValidator;
import ru.chernyshev.control.type.ProgramErrorType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static ru.chernyshev.control.utils.MockOperation.createOperation;
import static ru.chernyshev.control.utils.MockProgram.createFlyProgram;
import static ru.chernyshev.control.utils.OperationValidator.findWrongOperation;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProgramValidatorTest.ConfigurationTest.class)
@AutoConfigureMockMvc
public class ProgramValidatorTest {

    @Configuration
    static class ConfigurationTest {
    }

    @Test
    public void correctTest() {
        Operation operation = createOperation(1, "coolingSystemPowerPct",1);
        assertTrue(OperationValidator.isValid(operation));
    }

    @Test
    public void wrongVariableTest() {
        Operation operation = createOperation(1, "wrong");
        assertFalse(OperationValidator.isValid(operation));
    }

    @Test
    public void wrongTimeout1Test() {
        Operation operation = createOperation(1, "radioPowerDbm", 0);
        assertFalse(OperationValidator.isValid(operation));
    }

    @Test
    public void wrongTimeout2Test() {
        Operation operation = createOperation(1, "radioPowerDbm", -1);
        assertFalse(OperationValidator.isValid(operation));
    }

    @Test
    public void wrongIdTest() {
        Operation operation = createOperation(0, "radioPowerDbm", -1);
        assertFalse(OperationValidator.isValid(operation));
    }

    @Test
    public void notUniqueIdsTest() {
        List<Operation> operations = new ArrayList<>();
        operations.add(createOperation(1, "coolingSystemPowerPct", 1));
        operations.add(createOperation(1, "mainEngineFuelPct", 1));
        FlyProgram flyProgram = createFlyProgram(operations);
        Map<ProgramErrorType, List<Operation>> wrongOperation = findWrongOperation(flyProgram);
        assertThat(wrongOperation.size(), is(1));
        assertTrue(wrongOperation.containsKey(ProgramErrorType.NOT_UNIQUE_ID));
    }

    @Test
    public void correctProgramTest() {
        List<Operation> operations = new ArrayList<>();
        operations.add(createOperation(1, "coolingSystemPowerPct", 1));
        operations.add(createOperation(2, "mainEngineFuelPct", 1));
        FlyProgram flyProgram = createFlyProgram(operations);
        Map<ProgramErrorType, List<Operation>> wrongOperation = findWrongOperation(flyProgram);
        assertThat(wrongOperation.size(), is(0));
    }

    @Test
    public void dublicateTest() {
        List<Operation> operations = new ArrayList<>();
        operations.add(createOperation(1, "coolingSystemPowerPct", 1, 2));
        operations.add(createOperation(2, "coolingSystemPowerPct", 3,2));
        FlyProgram flyProgram = createFlyProgram(operations);
        Map<ProgramErrorType, List<Operation>> wrongOperation = findWrongOperation(flyProgram);
        assertThat(wrongOperation.size(), is(1));
        assertTrue(wrongOperation.containsKey(ProgramErrorType.DUBLICATE));
    }

    @Test
    public void wrongValuesTest() {
        List<Operation> operations = new ArrayList<>();
        operations.add(createOperation(1, "wrong", 1, 2));
        operations.add(createOperation(2, "coolingSystemPowerPct", 3,2));
        FlyProgram flyProgram = createFlyProgram(operations);
        Map<ProgramErrorType, List<Operation>> wrongOperation = findWrongOperation(flyProgram);
        assertThat(wrongOperation.size(), is(1));
        assertTrue(wrongOperation.containsKey(ProgramErrorType.WRONG_VALUES));
    }

    @Test
    public void emptyProgramTest() {
        List<Operation> operations = new ArrayList<>();
        FlyProgram flyProgram = createFlyProgram(operations);
        Map<ProgramErrorType, List<Operation>> wrongOperation = findWrongOperation(flyProgram);
        assertThat(wrongOperation.size(), is(1));
        assertTrue(wrongOperation.containsKey(ProgramErrorType.IS_EMPTY));
    }
}
