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
import ru.chernyshev.satelite.service.SpaceshipService;

import java.util.HashMap;
import java.util.StringJoiner;

import static org.junit.Assert.assertTrue;

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
        public SpaceshipService spaceshipService() {
            return new SpaceshipService();
        }

        @Bean
        public SatelliteController orderService(SpaceshipService spaceshipService, ObjectMapper objectMapper) {
            return new SatelliteController(spaceshipService, objectMapper);
        }
    }

    @Autowired
    private SatelliteController satelliteController;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void getValidParam() throws Exception {
        String param = "param3";
        ResponseEntity<String> responseEntity = satelliteController.getSettings(param);
        HashMap response = objectMapper.readValue(responseEntity.getBody(), HashMap.class);
        assertTrue(response.containsKey(param));
    }

    @Test
    public void getSomeParam() throws Exception {
        String param1 = "param1";
        String param2 = "param2";

        String path = new StringJoiner(",")
                .add(param1)
                .add(param2)
                .toString();

        ResponseEntity<String> responseEntity = satelliteController.getSettings(path);
        HashMap response = objectMapper.readValue(responseEntity.getBody(), HashMap.class);
        assertTrue(response.containsKey(param1));
        assertTrue(response.containsKey(param2));
    }
}
