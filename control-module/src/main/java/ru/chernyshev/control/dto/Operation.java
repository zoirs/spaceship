package ru.chernyshev.control.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Команды программы полета на установку параметров
 * <p>
 * Формат:
 * {
 * "id": 2,
 * "deltaT": 600,
 * "variable": "radioPowerDbm",
 * "value": 50,
 * "timeout": 1,
 * "critical": false
 * }
 */
@SuppressWarnings("UnusedDeclaration")
public class Operation {

    /**
     * Номер задачи (unique)
     */
    @JsonProperty
    private Integer id;

    /**
     * Разница в секундах между запуском сервиса и началом выполнения задачи
     */
    @JsonProperty
    private Integer deltaT;

    /**
     * Название параметра
     */
    @JsonProperty
    private String variable;

    /**
     * Значение параметра
     */
    @JsonProperty
    private int value;

    /**
     * Время за которое параметр должен достигнуть ожидаемого значения в штатном режиме (seconds)
     */
    @JsonProperty
    private Integer timeout;

    /**
     * Является ли отказ по параметру критическим (optional, default true)
     */
    @JsonProperty
    private boolean critical = true;

    /**
     * @return Номер задачи (unique)
     */
    public Integer getId() {
        return id;
    }

    /**
     * @return Разница в секундах между запуском сервиса и началом выполнения задачи
     */
    public Integer getDeltaT() {
        return deltaT;
    }

    /**
     * @return Название параметра
     */
    public String getVariable() {
        return variable;
    }

    /**
     * @return Значение параметра
     */
    public int getValue() {
        return value;
    }

    /**
     * @return Время за которое параметр должен достигнуть ожидаемого значения в штатном режиме (seconds)
     */
    public Integer getTimeout() {
        return timeout;
    }

    /**
     * @return Является ли отказ по параметру критическим (optional, default true)
     */
    public boolean isCritical() {
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
