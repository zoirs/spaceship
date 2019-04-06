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
import java.net.URL;
import java.net.URLDecoder;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProgramLoadAndExecuteTest.ProgramLoadAndExecuteConfiguration.class)
@AutoConfigureMockMvc
public class ProgramLoadAndExecuteTest {

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
            URL resource = this.getClass().getResource("/programmMoreOperation.json");
            String path = URLDecoder.decode(resource.getFile(), "UTF-8");

            return new ProgramLoader(restClientService, telemetryService, messageSender, objectMapper, path);
        }
    }

    @Autowired
    private RestClientService restClientService;

    /**
     * Тест на запуск трех операций из программы двумя командами
     * */
    @Test
    public void startOperationsTest() throws JsonProcessingException {
        final CountDownLatch latch = new CountDownLatch(2);

        doAnswer((Answer<Response>) invocation -> {
                    latch.countDown();
                    return mock(Response.class);
                }
        ).when(restClientService).send(Mockito.anyMap());

        try {
            boolean await = latch.await(3L, TimeUnit.SECONDS);
            assertTrue(await);
        } catch (InterruptedException e) {
            fail();
        }
    }
}
