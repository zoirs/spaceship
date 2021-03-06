package ru.chernyshev.control.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.chernyshev.control.type.TelemetryType;

import java.util.Date;

/**
 * Объект телеметрии передаваемой на землю
 * <p>
 * Формат:
 * {
 * "type": "error",
 * "timestamp": 1555019400,
 * "message": "..."
 * }
 */
public class TelemetryDto {

    /**
     * Тип в текстовом формате. Допустимые типы
     *
     * @see TelemetryType
     */
    @JsonProperty("type")
    private final String type;


    /**
     * Текущее время в формате Unix time
     */
    @JsonProperty("timestamp")
    private final int timestamp;

    /**
     * Сообщение об ошибке в свободной форме.
     */
    @JsonProperty("message")
    private final String message;

    public TelemetryDto(TelemetryType type, String message) {
        this.type = type.getKey();
        this.timestamp = (int) (new Date().getTime() / 1000);
        this.message = message;
    }

    /**
     * @return объект телеметрии в формате JSON
     */
    public String toJson() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return this.toString();
        }
    }

    @Override
    public String toString() {
        return "{" +
                "type='" + type + '\'' +
                ", timestamp=" + timestamp +
                ", message='" + message + '\'' +
                '}';
    }
}
