package ru.chernyshev.spaceship.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "startUp",
        "operations"
})
public class FlyProgramm {

    @JsonProperty("startUp")
    private Integer startUp;
    @JsonProperty("operations")
    private List<Operation> operations = null;

//    @JsonProperty("startUp")
    public Integer getStartUp() {
        return startUp;
    }

//    @JsonProperty("startUp")
    public void setStartUp(Integer startUp) {
        this.startUp = startUp;
    }

//    @JsonProperty("operations")
    public List<Operation> getOperations() {
        return operations;
    }

//    @JsonProperty("operations")
    public void setOperations(List<Operation> operations) {
        this.operations = operations;
    }
}
