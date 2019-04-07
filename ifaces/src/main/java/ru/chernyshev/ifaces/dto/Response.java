package ru.chernyshev.ifaces.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Dto объект получаемый в ответ от rest api
 * <p>
 * Формат:
 * {
 * "orientationZenithAngleDeg": {"set": 180, "value": 180},
 * "orientationAzimuthAngleDeg": {"set": 0, "value": 10}
 * }
 */
public class Response {

    private final Map<String, ConfigurationValues> response;

    private Response() {
        this.response = new HashMap<>();
    }

    @JsonValue
    public Map<String, ConfigurationValues> getResponse() {
        return response;
    }

    @JsonAnySetter
    @SuppressWarnings("WeakerAccess")
    public void add(String key, ConfigurationValues value) {
        response.put(key, value);
    }

    public static Builder newBuilder() {
        return new Response().new Builder();
    }

    /**
     * Вспомогательный объект для создания объекта
     *
     * @see Response
     */
    public class Builder {

        private Builder() {
        }

        public Builder add(String key, ConfigurationValues values) {
            Response.this.add(key, values);
            return this;
        }

        public Builder add(String key, int set) {
            ConfigurationValues values = new ConfigurationValues(set);
            values.setActualValue();
            return add(key, values);
        }

        public Response build() {
            return Response.this;
        }
    }
}
