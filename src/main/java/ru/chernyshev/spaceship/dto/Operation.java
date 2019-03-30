package ru.chernyshev.spaceship.dto;

//@JsonInclude(JsonInclude.Include.NON_NULL)
////@JsonPropertyOrder({
//        "id",
//        "deltaT",
//        "variable",
//        "value",
//        "timeout",
//        "critical"
//})
public class Operation {

//    //@JsonProperty("id")
    private Integer id;
//    //@JsonProperty("deltaT")
    private Integer deltaT;
//    //@JsonProperty("variable")
    private String variable;
//    //@JsonProperty("value")
    private Integer value;
//    //@JsonProperty("timeout")
    private Integer timeout;
//    //@JsonProperty("critical")
    private boolean critical;
//    @JsonIgnore
//    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

//    //@JsonProperty("id")
    public Integer getId() {
        return id;
    }

//    //@JsonProperty("id")
    public void setId(Integer id) {
        this.id = id;
    }

//    //@JsonProperty("deltaT")
    public Integer getDeltaT() {
        return deltaT;
    }

    //@JsonProperty("deltaT")
    public void setDeltaT(Integer deltaT) {
        this.deltaT = deltaT;
    }

    //@JsonProperty("variable")
    public String getVariable() {
        return variable;
    }

    //@JsonProperty("variable")
    public void setVariable(String variable) {
        this.variable = variable;
    }

    //@JsonProperty("value")
    public Integer getValue() {
        return value;
    }

    //@JsonProperty("value")
    public void setValue(Integer value) {
        this.value = value;
    }

    //@JsonProperty("timeout")
    public Integer getTimeout() {
        return timeout;
    }

    //@JsonProperty("timeout")
    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    //@JsonProperty("critical")
    public Boolean getCritical() {
        return critical;
    }

    //@JsonProperty("critical")
    public void setCritical(Boolean critical) {
        this.critical = critical;
    }

//    @JsonAnyGetter
//    public Map<String, Object> getAdditionalProperties() {
//        return this.additionalProperties;
//    }
//
//    @JsonAnySetter
//    public void setAdditionalProperty(String name, Object value) {
//        this.additionalProperties.put(name, value);
//    }

}
