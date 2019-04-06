package ru.chernyshev.control;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;
import ru.chernyshev.control.dto.Operation;
import ru.chernyshev.control.mockObject.MockOperation;
import ru.chernyshev.control.utils.OperationValidator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProgramValidatorTest.ConfigurationTest.class)
@AutoConfigureMockMvc
public class ProgramValidatorTest {

    @Configuration
    static class ConfigurationTest {
    }

    @Test
    public void correctTest() {
        Operation operation = MockOperation.createOperation(1, "radioPowerDbm",1);
        assertTrue(OperationValidator.isValid(operation));
    }

    @Test
    public void wrongVariableTest() {
        Operation operation = MockOperation.createOperation(1, "wrong");
        assertFalse(OperationValidator.isValid(operation));
    }

    @Test
    public void wrongTimeout1Test() {
        Operation operation = MockOperation.createOperation(1, "radioPowerDbm", 0);
        assertFalse(OperationValidator.isValid(operation));
    }

    @Test
    public void wrongTimeout2Test() {
        Operation operation = MockOperation.createOperation(1, "radioPowerDbm", -1);
        assertFalse(OperationValidator.isValid(operation));
    }

    @Test
    public void wrongIdTest() {
        Operation operation = MockOperation.createOperation(0, "radioPowerDbm", -1);
        assertFalse(OperationValidator.isValid(operation));
    }
    //todo еще тесты
}
