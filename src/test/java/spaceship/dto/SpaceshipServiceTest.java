package spaceship.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import ru.chernyshev.spaceship.dto.ConfigurationValues;
import ru.chernyshev.spaceship.service.ConfigurationParam;
import ru.chernyshev.spaceship.service.MessageSender;
import ru.chernyshev.spaceship.service.SpaceshipService;

import java.util.HashMap;
import java.util.StringJoiner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpaceshipServiceTest.SpaceshipServiceTestConfiguration.class)
@AutoConfigureMockMvc
public class SpaceshipServiceTest {

    @Configuration
    static class SpaceshipServiceTestConfiguration {

        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        public MessageSender messageSender(ObjectMapper objectMapper) {
            return new MessageSender(objectMapper);
        }

        @Bean
        public SpaceshipService spaceshipService(MessageSender messageSender) {
            return new SpaceshipService(messageSender);
        }
    }

    @Autowired
    private SpaceshipService spaceshipService;

    @Test
    public void setOneParamTest() {
        ConfigurationParam param = ConfigurationParam.MAIN_ENGINE_FUEL_PCT;
        spaceshipService.setConfigurationParam(param, 100);
        assertThat(spaceshipService.getConfiguration().size(), is(1));
    }

    @Test
    public void setOneParamValueTest() {
        ConfigurationParam param = ConfigurationParam.MAIN_ENGINE_FUEL_PCT;
        spaceshipService.setConfigurationParam(param, 100);

        ConfigurationValues configuration = spaceshipService.getConfiguration(param);

        assertNotNull(configuration);
        assertThat(configuration.getSet(), is(100));
    }

}
