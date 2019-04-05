package ru.chernyshev.control.dto;

public class Operation {

    private Integer id;
    private Integer deltaT;
    private String variable;
    private int value;
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
