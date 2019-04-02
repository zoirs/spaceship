package ru.chernyshev.spaceship.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.chernyshev.spaceship.dto.ConfigurationValues;
import ru.chernyshev.spaceship.dto.LogDto;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class SpaceshipService {

    private final ConcurrentMap<ConfigurationParam, ConfigurationValues> configuration = new ConcurrentHashMap<>();

    private final MessageSender messageSender;

    @Autowired
    public SpaceshipService(MessageSender messageSender) {
        this.messageSender = messageSender;
    }

    public void setConfigurationParam(ConfigurationParam key, int value) {
        configuration.merge(key, new ConfigurationValues(value), (oldValue, currValue) -> oldValue.update(value));

        setActualValue(key);

        String message = String.format("key %s was changed %s", key, value);
        messageSender.stdout(LogDto.trace(message));
    }

    public ConfigurationValues getConfiguration(ConfigurationParam key) {
        return configuration.get(key);
    }

    public HashMap<ConfigurationParam, ConfigurationValues> getConfiguration() {
        return new HashMap<>(configuration);
    }

    /**
     * Для иммитации физической системы делаем задержу в выставлении актуального значения
     */
    private void setActualValue(ConfigurationParam key) {
        new Thread(() -> {
            try {
                Thread.sleep((long) (Math.random() * 1000));
            } catch (InterruptedException ignore) {
            }
            ConfigurationValues param = configuration.get(key);
            param.setActualValue();
            configuration.putIfAbsent(key, param);
        }).start();
    }
}
