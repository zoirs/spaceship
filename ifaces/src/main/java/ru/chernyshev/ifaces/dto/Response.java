package ru.chernyshev.ifaces.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public class Response {

    private Map<String, ConfigurationValues> response;

    private Response() {
        this.response = new HashMap<>();
    }

    @JsonValue
    public Map<String, ConfigurationValues> getResponse() {
        return response;
    }

    @JsonAnySetter
    public void add(String key, ConfigurationValues value) {
        response.put(key, value);
    }

    public static Builder newBuilder() {
        return new Response().new Builder();
    }

    public class Builder {

        private Builder() {
        }

        public void add(String key, ConfigurationValues values) {
            Response.this.add(key, values);
        }

        public Response build() {
            return Response.this;
        }
    }
}
