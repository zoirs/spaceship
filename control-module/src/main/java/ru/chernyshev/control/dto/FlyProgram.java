package ru.chernyshev.control.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "startUp",
        "operations"
})
public class FlyProgram {

    @JsonProperty("startUp")
    private Integer startUp;

    @JsonProperty("operations")
    private List<Operation> operations = null;

    public Integer getStartUp() {
        return startUp;
    }

    //todo для теста
    public void setStartUp(Integer startUp) {
        this.startUp = startUp;
    }

    public List<Operation> getOperations() {
        return operations;
    }

}
