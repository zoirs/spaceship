package ru.chernyshev.control.service;

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
import ru.chernyshev.control.dto.Operation;
import ru.chernyshev.ifaces.dto.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static ru.chernyshev.control.utils.MockOperation.createOperation;
import static ru.chernyshev.control.utils.MockProgram.createFlyProgram;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProgramCheckTest.ProgramLoadAndExecuteConfiguration.class)
@AutoConfigureMockMvc
public class ProgramCheckTest {

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
            return new ProgramCheckTest.ProgramLoadAndExecuteConfiguration.ProgramLoaderTest(restClientService, telemetryService, messageSender, objectMapper);
        }

        private static class ProgramLoaderTest extends ProgramLoaderImpl {
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
    private ITelemetryService telemetryService;
    @Autowired
    private IMessageSender messageSender;
    @Autowired
    private IRestClientService restClientService;

    /**
     * Тест на вызов команды проверки установки параметров
     */
    @Test
    public void checkOperationAtOneTimeTest() {

        final CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger countCommand = new AtomicInteger();

        List<Operation> operations = new ArrayList<>();

        operations.add(createOperation(1, "param1"));
        operations.add(createOperation(2, "param2"));
        operations.add(createOperation(3, "param3"));

        doAnswer((Answer<Response>) invocation -> {
                    latch.countDown();
                    countCommand.getAndIncrement();
                    return mock(Response.class);
                }
        ).when(restClientService).get(Mockito.anyString());

        programLoader.execute(createFlyProgram(operations));

        try {
            boolean await = latch.await(1L, TimeUnit.SECONDS);
            assertTrue(await);
        } catch (InterruptedException e) {
            fail();
        }
        assertThat(countCommand.intValue(), is(1));
    }

    /**
     * Тест на вызов трех команд проверки установки параметров
     */
    @Test
    public void checkOperationAtDifferentTimeTest() {

        final CountDownLatch latch = new CountDownLatch(3);
        AtomicInteger countCommand = new AtomicInteger();

        List<Operation> operations = new ArrayList<>();

        operations.add(createOperation(1, "param1", 1));
        operations.add(createOperation(2, "param2", 2));
        operations.add(createOperation(3, "param3", 3));

        doAnswer((Answer<Response>) invocation -> {
                    latch.countDown();
                    countCommand.getAndIncrement();
                    return mock(Response.class);
                }
        ).when(restClientService).get(Mockito.anyString());

        programLoader.execute(createFlyProgram(operations));

        try {
            boolean await = latch.await(4L, TimeUnit.SECONDS);
            assertTrue(await);
        } catch (InterruptedException e) {
            fail();
        }
        assertThat(countCommand.intValue(), is(3));
    }
}
