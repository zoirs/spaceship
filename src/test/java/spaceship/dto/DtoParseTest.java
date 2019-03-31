package spaceship.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import ru.chernyshev.spaceship.dto.ConfigurationValues;
import ru.chernyshev.spaceship.dto.FlyProgramm;
import ru.chernyshev.spaceship.dto.Response;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
public class DtoParseTest {

    @Test
    public void test() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        URL resource = this.getClass().getResource("/programm.json");
        FlyProgramm flyProgramm = objectMapper.readValue(resource, FlyProgramm.class);

        assertNotNull(flyProgramm);
        assertThat(flyProgramm.getOperations().size(), is(4));
    }


    @Test
    public void test2() throws IOException, InterruptedException {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);
        scheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                System.out.println("11111111");
            }
        },100, TimeUnit.MILLISECONDS);

        Thread.sleep(200);
    }
    @Test
    public void test1() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        Response.Builder responseBuilder = Response.newBuilder();
        responseBuilder.add("orientationZenithAngleDeg", new ConfigurationValues(180));
        String s = objectMapper.writeValueAsString(responseBuilder .build());

        System.out.println("");
        System.out.println(s);
        System.out.println("");
    }

}
