package ru.chernyshev.ifaces.dto;

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

    protected ConfigurationValues() {
    }

    public ConfigurationValues(int set) {
        this.set = set;
    }

    public ConfigurationValues update(int set){
        this.set = set;
        return this;
    }

    public void setActualValue(){
        value = set;
    }

    public int getSet() {
        return set;
    }

    public Integer getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "ConfigurationValues{" +
                "set=" + set +
                ", value=" + value +
                '}';
    }
}
