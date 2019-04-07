package ru.chernyshev.control.service;

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
import ru.chernyshev.control.dto.FlyProgram;
import ru.chernyshev.control.dto.Operation;
import ru.chernyshev.ifaces.dto.Response;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static ru.chernyshev.control.utils.MockOperation.createOperation;
import static ru.chernyshev.control.utils.MockProgram.createFlyProgram;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProgramExecuteTest.ProgramLoadAndExecuteConfiguration.class)
@AutoConfigureMockMvc
public class ProgramExecuteTest {

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

        @Bean
        public ProgramLoader programLoader(MessageSender messageSender, ObjectMapper objectMapper) {
            return new ProgramLoaderTest(restClientService, telemetryService, messageSender, objectMapper);
        }

        static class ProgramLoaderTest extends ProgramLoaderImpl {
            ProgramLoaderTest(IRestClientService restClientService, ITelemetryService telemetryService, IMessageSender messageSender, ObjectMapper objectMapper) {
                super(restClientService, telemetryService, messageSender, objectMapper, "");
            }

            @Override
            public void init() {
                //do nothing
            }
        }
    }

    @Autowired
    private IProgramLoader programLoader;
    @Autowired
    private IRestClientService restClientService;

    /**
     * Тест на запуск трех операций одной командой
     */
    @Test
    public void threeOperationAtOneTime() throws JsonProcessingException {
        final CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger countCommand = new AtomicInteger();

        List<Operation> operations = new ArrayList<>();

        operations.add(createOperation(1, "param1"));
        operations.add(createOperation(2, "param2"));
        operations.add(createOperation(3, "param3"));

        programLoader.execute(createFlyProgram(operations));

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

        operations.add(createOperation(1, "param1", 0, 1));
        operations.add(createOperation(2, "param2", 0, 2));
        operations.add(createOperation(3, "param3", 0, 3));

        when(flyProgram.getOperations()).thenReturn(operations);

        doAnswer((Answer<Response>) invocation -> {
                    latch.countDown();
                    countCommand.getAndIncrement();
                    return mock(Response.class);
                }
        ).when(restClientService).send(Mockito.anyMap());

        programLoader.execute(flyProgram);

        try {
            boolean await = latch.await(5L, TimeUnit.SECONDS);
            assertTrue(await);
        } catch (InterruptedException e) {
            fail();
        }

        assertThat(countCommand.intValue(), is(3));
    }
}
