package ru.chernyshev.spaceship.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import ru.chernyshev.spaceship.dto.Operation;
import ru.chernyshev.spaceship.dto.SettingsRes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Command implements Runnable {

    private final Logger logger = LogManager.getLogger(Command.class);

    private final List<Operation> operations;
    private final RestTemplate restTemplate;

    public Command(RestTemplate restTemplate, List<Operation> operations) {
        this.restTemplate = restTemplate;
        this.operations = operations;
    }

    @Override
    public void run() {
        logger.info("Извлекаем задачу");

        Map<String, Integer> requestParam = operations.stream()
                .collect(Collectors.toMap(Operation::getVariable, Operation::getValue));

        String response = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String s = objectMapper.writeValueAsString(requestParam);
logger.info("отправляем " + s);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<String>(s,headers);
            restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());


             response = restTemplate.patchForObject("http://localhost:8080/settings", entity, String.class);
        }catch (ResourceAccessException e) {
            logger.error(e);
        }catch (Exception e) {
            logger.error(e);
        }

        logger.info("выставили значение " + response);

    }
}
