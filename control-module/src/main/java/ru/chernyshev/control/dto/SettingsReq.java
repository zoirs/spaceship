package ru.chernyshev.control.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

//todo возможо нет надобности
public class SettingsReq {
    private String key;
    private Integer value;

    @JsonProperty("key")
    public String getKey() {
        return key;
    }

    @JsonProperty("value")
    public Integer getValue() {
        return value;
    }
}
