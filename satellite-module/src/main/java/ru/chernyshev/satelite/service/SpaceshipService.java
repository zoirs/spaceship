package ru.chernyshev.satelite.service;

import org.springframework.stereotype.Service;
import ru.chernyshev.ifaces.dto.ConfigurationValues;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class SpaceshipService {

    private final ConcurrentMap<String, ConfigurationValues> configuration = new ConcurrentHashMap<>();

    public void setConfigurationParam(String key, int value) {
        configuration.merge(key, new ConfigurationValues(value), (oldValue, currValue) -> oldValue.update(value));

        setActualValue(key);

        System.out.println(String.format("key %s was changed %s", key, value));
    }

    public ConfigurationValues getConfiguration(String key) {
        return configuration.get(key);
    }

    public HashMap<String, ConfigurationValues> getConfiguration() {
        return new HashMap<>(configuration);
    }

    /**
     * Для иммитации физической системы делаем задержу в выставлении актуального значения
     */
    private void setActualValue(String key) {
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
