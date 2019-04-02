package spaceship.dto;

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
import ru.chernyshev.spaceship.dto.FlyProgramm;
import ru.chernyshev.spaceship.dto.Operation;
import ru.chernyshev.spaceship.service.MessageSender;
import ru.chernyshev.spaceship.service.ProgramLoader;
import ru.chernyshev.spaceship.service.TelemetryService;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
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
        public RestTemplate restTemplate() {
            return new RestTemplate();
        }

        @Bean
        public MessageSender messageSender(ObjectMapper objectMapper) {
            return new MessageSender(objectMapper);
        }

        @Bean
        public ProgramLoader programLoader(RestTemplate restTemplate, MessageSender messageSender, ObjectMapper objectMapper) throws UnsupportedEncodingException {
            URL resource = this.getClass().getResource("/programm.json");
            String path = URLDecoder.decode(resource.getFile(), "UTF-8");
            return new ProgramLoader(restTemplate, mock(TelemetryService.class), messageSender, objectMapper, path);
        }
    }

    @Autowired
    private ProgramLoader programLoader;

    @Test
    public void loadOnStartTest() {
        FlyProgramm flyProgram = programLoader.getFlyProgram();
        assertNotNull(flyProgram);
    }

    @Test
    public void readProgramTest() {
        FlyProgramm flyProgram = programLoader.getFlyProgram();
        assertThat(flyProgram.getOperations().size(), is(1));
    }

    @Test
    public void readOperationTest() {
        FlyProgramm flyProgram = programLoader.getFlyProgram();
        Operation operation = flyProgram.getOperations().get(0);
        assertThat(operation.getId(), is(1));
        assertThat(operation.getDeltaT(), is(0));
        assertThat(operation.getVariable(), is("coolingSystemPowerPercent"));
        assertThat(operation.getValue(), is(30));
        assertThat(operation.getTimeout(), is(1));
    }
}
