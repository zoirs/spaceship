package ru.chernyshev.spaceship.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public class ResponseBuilder {

    private Map<String, SettingsValues> response;

    public ResponseBuilder() {
        this.response = new HashMap<>();
    }

    public void add(String name, int set, int value) {
        response.put(name, new SettingsValues(set, value));
    }

    @JsonValue
    public Map<String, SettingsValues> getResponse() {
        return response;
    }

    private class SettingsValues {
        @JsonProperty("set")
        private int set;
        @JsonProperty("value")
        private int value;

        SettingsValues(int set, int value) {
            this.set = set;
            this.value = value;
        }
    }
}
