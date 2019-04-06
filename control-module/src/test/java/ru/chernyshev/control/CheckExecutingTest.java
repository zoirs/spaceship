package ru.chernyshev.control;

import com.fasterxml.jackson.databind.ObjectMapper;
import mockit.Expectations;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;
import ru.chernyshev.control.dto.Operation;
import ru.chernyshev.control.service.*;
import ru.chernyshev.ifaces.dto.Response;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;
import static ru.chernyshev.control.utils.MockOperation.createOperation;

/**
 * Тесты на верификацию выставленных значений
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = CheckExecutingTest.ProgramLoadAndExecuteConfiguration.class)
@AutoConfigureMockMvc
public class CheckExecutingTest {

    @Configuration
    static class ProgramLoadAndExecuteConfiguration {

        @MockBean
        private IRestClientService restClientService;

        @MockBean
        private ITelemetryService telemetryService;

        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        public IMessageSender messageSender(ObjectMapper objectMapper) {
            return new MessageSender(objectMapper);
        }
    }

    @Autowired
    private ITelemetryService telemetryService;
    @Autowired
    private IMessageSender messageSender;
    @Autowired
    private IRestClientService restClientService;

    /**
     * Тест при проверке выставления параметра пришло верное значение
     */
    @Test
    public void operationExecuteCorrectTest() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        String paramName = "param1";

        List<Operation> operations = new ArrayList<>();
        operations.add(createOperation(1, paramName));

        doAnswer((Answer<Response>) invocation -> Response.newBuilder().add(paramName, 10).build())
                .when(restClientService)
                .get(paramName);

        new OperationExecutingCheckCommand(restClientService, telemetryService, operations, messageSender).run();

        latch.await(1L, TimeUnit.SECONDS);
        verify(telemetryService, never()).send(any(), anyString());
    }

    /**
     * Тест при проверке выставления параметра пришел пустой список
     */
    @Test
    public void nonOperationSetTest() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        String paramName = "param1";

        List<Operation> operations = new ArrayList<>();
        operations.add(createOperation(1, paramName));

        doAnswer((Answer<Response>) invocation -> Response.newBuilder().build())
                .when(restClientService)
                .get(paramName);

        new OperationExecutingCheckCommand(restClientService, telemetryService, operations, messageSender).run();

        latch.await(1L, TimeUnit.SECONDS);
        verify(telemetryService, times(1))
                .send(TelemetryType.ERROR, "Check operation result.Value was not set. Ids: 1.");
    }

    /**
     * Тест при проверке выставления параметра пришел не верное значение
     */
    @Test
    public void nonCorrectValueOperationSetTest() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        String paramName = "param1";

        List<Operation> operations = new ArrayList<>();
        operations.add(createOperation(1, paramName));

        doAnswer((Answer<Response>) invocation -> Response.newBuilder().add(paramName, 1).build())
                .when(restClientService)
                .get(paramName);

        new OperationExecutingCheckCommand(restClientService, telemetryService, operations, messageSender).run();

        latch.await(1L, TimeUnit.SECONDS);
        verify(telemetryService, times(1))
                .send(TelemetryType.ERROR, "Check operation result.Value was not set. Ids: 1.");
    }

    /**
     * Тест при проверке выставления параметра пришло одно верное и одно не верное значение
     */
    @Test
    public void diferentValueOperationSetTest() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        String paramName1 = "param1";
        String paramName2 = "param2";

        List<Operation> operations = new ArrayList<>();
        operations.add(createOperation(1, paramName1));
        operations.add(createOperation(2, paramName2));

        doAnswer(
                (Answer<Response>) invocation ->
                        Response.newBuilder()
                                .add(paramName1, 10)
                                .add(paramName2, 1)
                                .build())
                .when(restClientService)
                .get(paramName1 + "," + paramName2);

        new OperationExecutingCheckCommand(restClientService, telemetryService, operations, messageSender).run();

        latch.await(1L, TimeUnit.SECONDS);
        verify(telemetryService, times(1)).
                send(TelemetryType.ERROR, "Check operation result.Value was not set. Ids: 2.");
    }

    /**
     * Выход если пришедшее неверное значение критическое
     */
    @Test(expected = EOFException.class)
    public void checkOperationAtOneTimeTestdsds() throws InterruptedException {
        new Expectations(System.class) {{
            System.exit(anyInt);
            result = new EOFException();
        }};

        final CountDownLatch latch = new CountDownLatch(1);
        String paramName = "param1";

        List<Operation> operations = new ArrayList<>();
        operations.add(createOperation(1, paramName, true));

        doAnswer((Answer<Response>) invocation -> Response.newBuilder().build())
                .when(restClientService)
                .get(paramName);

        new OperationExecutingCheckCommand(restClientService, telemetryService, operations, messageSender).run();

        latch.await(1L, TimeUnit.SECONDS);
    }
}
