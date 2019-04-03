package ru.chernyshev.control;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@SpringBootApplication
public class SpaceshipApplication {

    @Value("${timeout}")
    private int timeout;

    public static void main(String[] args) {
        SpringApplication.run(SpaceshipApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(timeout))
                .setReadTimeout(Duration.ofMillis(timeout))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build();
    }

    @Bean
    public Integer telemetryFreq(@Value("${TELEMETRY_FREQ:10}") int telemetryFreqDefault) {
        String telemetryFreq = System.getenv("TELEMETRY_FREQ");
        if (Strings.isEmpty(telemetryFreq)) {
            return telemetryFreqDefault;
        }
        try {
            return Integer.valueOf(telemetryFreq);
        } catch (NumberFormatException ignore) {
        }

        return telemetryFreqDefault;
    }

    @Bean
    public String flightProgramPath(@Value("${FLIGHT_PROGRAM}") String flightProgramDefault) {
        String flightProgram = System.getenv("FLIGHT_PROGRAM");
        if (Strings.isEmpty(flightProgram)) {
            return flightProgramDefault;
        }
        return flightProgram;
    }
}
