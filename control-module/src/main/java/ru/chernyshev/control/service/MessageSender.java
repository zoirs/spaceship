package ru.chernyshev.control.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.chernyshev.control.model.Log;

@Service
public class MessageSender implements IMessageSender {

    private final ObjectMapper objectMapper;

    @Autowired
    public MessageSender(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void stderr(String message) {
        System.err.println(message);
    }

    public void stdout(Log log) {
        System.out.println(convertToString(log));
    }

    private String convertToString(Log log) {
        try {
            return objectMapper.writeValueAsString(log);
        } catch (JsonProcessingException ignore) {
        }
        return log.toString();
    }
}
