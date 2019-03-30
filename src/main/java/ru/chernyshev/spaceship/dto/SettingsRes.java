package ru.chernyshev.spaceship.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public class SettingsRes {

    private Map<String, SettingsValues> result;

    public SettingsRes(String name, String set, String value) {

        this.result = new HashMap<>();
        this.result.put(name, new SettingsValues(set, value));
        this.result.put(name + "1", new SettingsValues(set, value));
    }

    @JsonValue
    public Map<String, SettingsValues> getResult() {
        return result;
    }

    private class SettingsValues {
        @JsonProperty("set")
        String set;
        @JsonProperty("value")
        String value;

        SettingsValues(String set, String value) {
            this.set = set;
            this.value = value;
        }
    }
}
