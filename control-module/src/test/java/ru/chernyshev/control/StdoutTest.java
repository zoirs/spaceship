package ru.chernyshev.control;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;
import ru.chernyshev.control.dto.LogMessage;
import ru.chernyshev.control.service.IMessageSender;
import ru.chernyshev.control.service.MessageSender;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = StdoutTest.StdoutTestConfiguration.class)
@AutoConfigureMockMvc
public class StdoutTest {
        @Configuration
        static class StdoutTestConfiguration {

        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        public IMessageSender messageSender(ObjectMapper objectMapper) {
            return new MessageSender(objectMapper);
        }
    }

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @Autowired
    private IMessageSender messageSender;

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @After
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    public void out() {
        LogMessage testMessage = LogMessage.trace("test message");
        messageSender.stdout(testMessage);
        assertTrue(outContent.toString().contains("test message"));
    }

    @Test
    public void err() {
        messageSender.stderr("test message");
        assertTrue(errContent.toString().contains("test message"));
    }
}
