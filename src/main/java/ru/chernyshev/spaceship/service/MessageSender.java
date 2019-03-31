package ru.chernyshev.spaceship.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.chernyshev.spaceship.dto.LogDto;

@Service
public class MessageSender {

    private final ObjectMapper objectMapper;

    @Autowired
    public MessageSender(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void stderr(String message) {
        System.err.println(message);
    }

    public void stdout(LogDto logDto) {
        System.out.println(convertToString(logDto));
    }

    private String convertToString(LogDto logDto) {
        try {
            return objectMapper.writeValueAsString(logDto);
        } catch (JsonProcessingException ignore) {
        }
        return logDto.toString();
    }
}
