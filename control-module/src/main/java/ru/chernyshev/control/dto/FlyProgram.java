package ru.chernyshev.control.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

/**
 * Программа полета
 *
 * Формат:
 * {
 *   "startUp": 1555016400,
 *   "operations": [
 *     {
 *       "id": 1,
 *       "deltaT": 0,
 *       "variable": "coolingSystemPowerPercent",
 *       "value": 30,
 *       "timeout": 1
 *     }
 *     ...
 *    ]
 * }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "startUp",
        "operations"
})
@SuppressWarnings("unused")
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
    private List<Operation> operations;

    /**
     * @return Время когда сервис был запущен (Unix timestamp, seconds)
     * */
    public Integer getStartUp() {
        return startUp;
    }

    /**
     * @return Массив задач
     * */
    public List<Operation> getOperations() {
        return operations;
    }

}
