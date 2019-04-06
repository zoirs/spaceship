import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;
import ru.chernyshev.ifaces.dto.Response;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DtoTest.DtoTestConfiguration.class)
@AutoConfigureMockMvc
public class DtoTest {

    @Configuration
    static class DtoTestConfiguration {

        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void responseParseTest() throws IOException {
        String json = "{\"coolingSystemPowerPct\":{\"set\":30,\"value\":11}}";
        Response response = objectMapper.readValue(json, Response.class);
        assertThat(response.getResponse().size(), is(1));
    }
}
