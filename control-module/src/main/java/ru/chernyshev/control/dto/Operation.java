package ru.chernyshev.control.dto;

public class Operation {

    private Integer id;
    private Integer deltaT;
    private String variable;
    private Integer value;
    private Integer timeout;
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

    public Integer getValue() {
        return value;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public Boolean getCritical() {
        return critical;
    }
}
