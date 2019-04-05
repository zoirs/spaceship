package ru.chernyshev.satelite;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;
import ru.chernyshev.ifaces.dto.ConfigurationValues;
import ru.chernyshev.satelite.service.SpaceshipService;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

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
        public SpaceshipService spaceshipService() {
            return new SpaceshipService();
        }
    }

    @Autowired
    private SpaceshipService spaceshipService;

    @Test
    public void setOneParamTest() {
        spaceshipService.setConfigurationParam("mainEngineFuelPct", 100);
        assertThat(spaceshipService.getConfiguration().size(), is(1));
    }

    @Test
    public void setOneParamValueTest() {
        spaceshipService.setConfigurationParam("mainEngineFuelPct", 100);

        ConfigurationValues configuration = spaceshipService.getConfiguration("mainEngineFuelPct");

        assertNotNull(configuration);
        assertThat(configuration.getSet(), is(100));
    }

}
