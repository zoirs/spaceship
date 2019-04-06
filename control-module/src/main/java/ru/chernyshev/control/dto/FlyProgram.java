package ru.chernyshev.control.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

/**
 * Программа полета
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "startUp",
        "operations"
})
public class FlyProgram {

    /**
     * Время когда сервис был запущен (Unix timestamp, seconds)
     */
    @JsonProperty("startUp")
    private Integer startUp;

    /**
     * Массив задач
     */
    @JsonProperty("operations")
    private List<Operation> operations = null;

    /**
     * @return Время когда сервис был запущен (Unix timestamp, seconds)
     * */
    public Integer getStartUp() {
        return startUp;
    }

    //todo для теста
    public void setStartUp(Integer startUp) {
        this.startUp = startUp;
    }

    /**
     * @return Массив задач
     * */
    public List<Operation> getOperations() {
        return operations;
    }

}
