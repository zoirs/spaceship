package ru.chernyshev.spaceship.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.chernyshev.spaceship.dto.ConfigurationValues;
import ru.chernyshev.spaceship.dto.LogDto;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.util.stream.Collectors.toMap;

@Service
public class SpaceshipService {

    private final ConcurrentMap<ConfigurationParam, ConfigurationValues> configuration = new ConcurrentHashMap<>();

    private final MessageSender messageSender;

    @Autowired
    public SpaceshipService(MessageSender messageSender) {
        this.messageSender = messageSender;
    }

    public boolean setConfigurationParam(ConfigurationParam key, int value) {

        setNewValue(key, value);

        setActualValue(key);

        String message = String.format("key %s was changed %s", key, value);
        messageSender.stdout(LogDto.trace(message));
        return true;
    }

    private void setNewValue(ConfigurationParam key, int value) {
        ConfigurationValues param = configuration.get(key);
        if (param == null) {
            configuration.put(key, new ConfigurationValues(value));
        } else {
            param.update(value);
            configuration.put(key, param);
        }
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

    public ConfigurationValues getConfiguration(ConfigurationParam key) {
        return configuration.get(key);
    }

    public Map<ConfigurationParam, Integer> getConfiguration() {
        return configuration.entrySet()
                .stream()
                .collect(toMap(Map.Entry::getKey, o-> o.getValue().getValue()));
    }
}
