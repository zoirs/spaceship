package ru.chernyshev.spaceship.dto;

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

    public static Builder newBuilder() {
        return new Response().new Builder();
    }

    public class Builder {

        private Builder() {
        }

        public void add(String key, ConfigurationValues values) {
            Response.this.response.put(key, values);
        }

        public Response build() {
            return Response.this;
        }
    }
}
