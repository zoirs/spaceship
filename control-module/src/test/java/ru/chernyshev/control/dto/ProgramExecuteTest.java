package ru.chernyshev.control.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;
import ru.chernyshev.control.service.MessageSender;
import ru.chernyshev.control.service.ProgramLoader;
import ru.chernyshev.control.service.RestClientService;
import ru.chernyshev.control.service.TelemetryService;
import ru.chernyshev.ifaces.dto.Response;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProgramExecuteTest.ProgramLoadAndExecuteConfiguration.class)
@AutoConfigureMockMvc
public class ProgramExecuteTest {

    @Configuration
    static class ProgramLoadAndExecuteConfiguration {

        @MockBean
        private RestClientService restClientService;
        @MockBean
        private TelemetryService telemetryService;

        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        public MessageSender messageSender(ObjectMapper objectMapper) {
            return new MessageSender(objectMapper);
        }

        @Bean
        public ProgramLoader programLoader(MessageSender messageSender, ObjectMapper objectMapper) throws UnsupportedEncodingException {
            return new ProgramLoaderTest(restClientService, telemetryService, messageSender, objectMapper, "");
        }

        static class ProgramLoaderTest extends ProgramLoader {
            ProgramLoaderTest(RestClientService restClientService, TelemetryService telemetryService, MessageSender messageSender, ObjectMapper objectMapper, String flightProgramPath) {
                super(restClientService, telemetryService, messageSender, objectMapper, flightProgramPath);
            }

            @Override
            public void init() {
                //do nothing
            }
        }
    }

    @Autowired
    private ProgramLoader programLoader;
    @Autowired
    private RestClientService restClientService;

    /**
     * Тест на запуск трех операций одной командой
     */
    @Test
    public void threeOperationAtOneTime() throws JsonProcessingException {
        final CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger countCommand = new AtomicInteger();

        FlyProgram flyProgram = mock(FlyProgram.class);
        when(flyProgram.getStartUp()).thenReturn((int) (new Date().getTime() / 1000));
        List<Operation> operations = new ArrayList<>();

        operations.add(createOperation(1, "param1"));
        operations.add(createOperation(2, "param2"));
        operations.add(createOperation(3, "param3"));

        when(flyProgram.getOperations()).thenReturn(operations);

        programLoader.execute(flyProgram);

        doAnswer((Answer<Response>) invocation -> {
                    latch.countDown();
                    countCommand.getAndIncrement();
                    return mock(Response.class);
                }
        ).when(restClientService).send(Mockito.anyMap());

        try {
            boolean await = latch.await(1L, TimeUnit.SECONDS);
            assertTrue(await);
        } catch (InterruptedException e) {
            fail();
        }
        assertThat(countCommand.intValue(), is(1));
    }

    /**
     * Тест на запуск трех операций тремя командами
     */
    @Test
    public void threeOperationAtDifferentTime() throws JsonProcessingException {
        final CountDownLatch latch = new CountDownLatch(3);
        AtomicInteger countCommand = new AtomicInteger();

        FlyProgram flyProgram = mock(FlyProgram.class);
        when(flyProgram.getStartUp()).thenReturn((int) (new Date().getTime() / 1000));
        List<Operation> operations = new ArrayList<>();

        operations.add(createOperation(1, "param1", 1));
        operations.add(createOperation(2, "param2", 2));
        operations.add(createOperation(3, "param3", 3));

        when(flyProgram.getOperations()).thenReturn(operations);

        programLoader.execute(flyProgram);

        doAnswer((Answer<Response>) invocation -> {
                    latch.countDown();
                    countCommand.getAndIncrement();
                    return mock(Response.class);
                }
        ).when(restClientService).send(Mockito.anyMap());

        try {
            boolean await = latch.await(5L, TimeUnit.SECONDS);
            assertTrue(await);
        } catch (InterruptedException e) {
            fail();
        }
        assertThat(countCommand.intValue(), is(3));
    }

    private Operation createOperation(int id, String paramName) {
        return createOperation(id, paramName, 0);
    }

    private Operation createOperation(int id, String paramName, int deltaT) {
        Operation operation = mock(Operation.class);
        when(operation.getId()).thenReturn(id);
        when(operation.getDeltaT()).thenReturn(deltaT);
        when(operation.getTimeout()).thenReturn(1);
        when(operation.getVariable()).thenReturn(paramName);
        when(operation.getValue()).thenReturn(10);
        return operation;
    }
}
