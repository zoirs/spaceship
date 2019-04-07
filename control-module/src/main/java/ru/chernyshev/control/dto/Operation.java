package ru.chernyshev.control.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("UnusedDeclaration")
public class Operation {

    @JsonProperty
    private Integer deltaT;
    @JsonProperty
    private Integer id;
    @JsonProperty
    private String variable;
    @JsonProperty
    private int value;
    @JsonProperty
    private Integer timeout;
    @JsonProperty
    private boolean critical;

    public Integer getId() {
        return id;
    }

    public Integer getDeltaT() {
        return deltaT;
    }

    public String getVariable() {
        return variable;
    }

    public int getValue() {
        return value;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public Boolean getCritical() {
        return critical;
    }

    @Override
    public String toString() {
        return "Operation{" +
                "id=" + id +
                ", deltaT=" + deltaT +
                ", variable='" + variable + '\'' +
                ", value=" + value +
                ", timeout=" + timeout +
                ", critical=" + critical +
                '}';
    }
}
