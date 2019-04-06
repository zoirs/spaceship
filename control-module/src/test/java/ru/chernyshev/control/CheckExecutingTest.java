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
import ru.chernyshev.control.mockObject.MockTelemetryService;
import ru.chernyshev.control.service.*;
import ru.chernyshev.ifaces.dto.Response;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static ru.chernyshev.control.mockObject.MockOperation.createOperation;

/**
 * Тесты на верификацию выставленных значений
 * **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = CheckExecutingTest.ProgramLoadAndExecuteConfiguration.class)
@AutoConfigureMockMvc
public class CheckExecutingTest {

    @Configuration
    static class ProgramLoadAndExecuteConfiguration {

        @MockBean
        private RestClientService restClientService;

        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        public ITelemetryService telemetryService() {
            return new MockTelemetryService();
        }

        @Bean
        public MessageSender messageSender(ObjectMapper objectMapper) {
            return new MessageSender(objectMapper);
        }
    }

    @Autowired
    private MockTelemetryService telemetryService;
    @Autowired
    private MessageSender messageSender;
    @Autowired
    private RestClientService restClientService;

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
        assertThat(telemetryService.getMessages().size(), is(0));
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
        assertThat(telemetryService.getMessages().get(0), is("Check operation result.Value was not set. Ids: 1."));
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
        assertThat(telemetryService.getMessages().get(0), is("Check operation result.Value was not set. Ids: 1."));
    }

    /**
     * Выход если пришедшее неверное значение критическое
     * */
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
