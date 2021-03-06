package ru.chernyshev.control.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import ru.chernyshev.control.dto.FlyProgram;
import ru.chernyshev.control.dto.Operation;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProgramLoaderTest.ProgramLoaderTestConfiguration.class)
@AutoConfigureMockMvc
public class ProgramLoaderTest {

    @Configuration
    static class ProgramLoaderTestConfiguration {

        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        public IRestClientService restClientService(RestTemplate restTemplate) {
            return new RestClientService(restTemplate, "");
        }

        @Bean
        public RestTemplate restTemplate() {
            return new RestTemplate();
        }

        @Bean
        public IMessageSender messageSender(ObjectMapper objectMapper) {
            return new MessageSender(objectMapper);
        }

        @Bean
        public IProgramLoader programLoader(RestClientService restClientService, MessageSender messageSender, ObjectMapper objectMapper) throws UnsupportedEncodingException {
            URL resource = this.getClass().getResource("/programOneOperation.json");
            String path = URLDecoder.decode(resource.getFile(), "UTF-8");
            return new ProgramLoaderImpl(restClientService, mock(TelemetryService.class), messageSender, objectMapper, path);
        }
    }

    @Autowired
    private IProgramLoader programLoader;

    @Test
    public void loadOnStartTest() {
        FlyProgram flyProgram = programLoader.getFlyProgram();
        assertNotNull(flyProgram);
    }

    @Test
    public void readProgramTest() {
        FlyProgram flyProgram = programLoader.getFlyProgram();
        assertThat(flyProgram.getOperations().size(), is(1));
    }

    @Test
    public void readOperationTest() {
        FlyProgram flyProgram = programLoader.getFlyProgram();
        Operation operation = flyProgram.getOperations().get(0);
        assertThat(operation.getId(), is(1));
        assertThat(operation.getDeltaT(), is(0));
        assertThat(operation.getVariable(), is("coolingSystemPowerPct"));
        assertThat(operation.getValue(), is(30));
        assertThat(operation.getTimeout(), is(1));
        assertFalse(operation.isCritical());
    }
}
