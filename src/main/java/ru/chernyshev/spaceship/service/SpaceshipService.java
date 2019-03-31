package ru.chernyshev.spaceship.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.chernyshev.spaceship.dto.LogDto;

import java.util.EnumMap;

@Service
public class SpaceshipService {

    private final EnumMap<ConfigurationParam, Integer> configuration = new EnumMap<>(ConfigurationParam.class);

    private final MessageSender messageSender;

    @Autowired
    public SpaceshipService(MessageSender messageSender) {
        this.messageSender = messageSender;
    }

    public boolean setConfigurationParam(ConfigurationParam key, int value) {
        configuration.put(key, value);
        String message = String.format("key %s was changed %s", key, value);
        messageSender.stdout(LogDto.trace(message));
        return true;
    }

    public EnumMap<ConfigurationParam, Integer> getConfiguration() {
        return configuration;
    }
}
