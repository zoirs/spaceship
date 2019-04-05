package ru.chernyshev.satelite;

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
import ru.chernyshev.satelite.controller.SatelliteController;
import ru.chernyshev.satelite.service.ConfigurationParam;
import ru.chernyshev.satelite.service.MessageSender;
import ru.chernyshev.satelite.service.SpaceshipService;

import java.util.HashMap;
import java.util.StringJoiner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SatelliteControllerTest.SpaceshipControllerTestConfiguration.class)
@AutoConfigureMockMvc
public class SatelliteControllerTest {

    @Configuration
    static class SpaceshipControllerTestConfiguration {

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

        @Bean
        public SatelliteController orderService(SpaceshipService spaceshipService, ObjectMapper objectMapper, MessageSender messageSender) {
            return new SatelliteController(spaceshipService, objectMapper, messageSender);
        }
    }

    @Autowired
    private SatelliteController satelliteController;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void getValidParam() throws Exception {
        ConfigurationParam param = ConfigurationParam.COOLING_SYSTEM_POWER_PCT;
        ResponseEntity<String> responseEntity = satelliteController.getSettings(param.getKey());
        HashMap response = objectMapper.readValue(responseEntity.getBody(), HashMap.class);
        assertTrue(response.containsKey(param.getKey()));
    }

    @Test
    public void getInvalidParam() throws Exception {
        String param = "incorrectParam";
        ResponseEntity<String> responseEntity = satelliteController.getSettings(param);
        HashMap response = objectMapper.readValue(responseEntity.getBody(), HashMap.class);
        assertFalse(response.containsKey(param));
    }

    @Test
    public void getSomeParam() throws Exception {
        ConfigurationParam param1 = ConfigurationParam.COOLING_SYSTEM_POWER_PCT;
        ConfigurationParam param2 = ConfigurationParam.MAIN_ENGINE_FUEL_PCT;

        String path = new StringJoiner(",")
                .add(param1.getKey())
                .add(param2.getKey())
                .toString();

        ResponseEntity<String> responseEntity = satelliteController.getSettings(path);
        HashMap response = objectMapper.readValue(responseEntity.getBody(), HashMap.class);
        assertTrue(response.containsKey(param1.getKey()));
        assertTrue(response.containsKey(param2.getKey()));
    }

    @Test
    public void getSomeDiferentParam() throws Exception {
        ConfigurationParam param1 = ConfigurationParam.COOLING_SYSTEM_POWER_PCT;
        String param2 = "incorrectParam";
        ConfigurationParam param3 = ConfigurationParam.MAIN_ENGINE_FUEL_PCT;

        String path = new StringJoiner(",")
                .add(param1.getKey())
                .add(param2)
                .add(param3.getKey())
                .toString();

        ResponseEntity<String> responseEntity = satelliteController.getSettings(path);
        HashMap response = objectMapper.readValue(responseEntity.getBody(), HashMap.class);
        assertThat(response.size(), is(2));
    }
}
