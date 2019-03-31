package ru.chernyshev.spaceship.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Установленные и фактические значения конфигурации
 */
public class ConfigurationValues {

    /**
     * Установленное значение параметра
     */
    @JsonProperty("set")
    private int set;

    /**
     * Фактическое значение параметра
     */
    @JsonProperty("value")
    private Integer value;

    public ConfigurationValues(int set, int value) {
        this.set = set;
        this.value = value;
    }
    public ConfigurationValues(int set) {
        this.set = set;
    }

    public void update(int set){
        this.set = set;
    }

    public void setActualValue(){
        value = set;
    }

    public int getSet() {
        return set;
    }

    public int getValue() {
        return value;
    }
}
