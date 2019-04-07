package ru.chernyshev.control.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

/**
 * Обертка для сообщений лога
 *
 * Формат:
 * {
 *   "time":"1961-04-12T06:07:00Z",
 *   "level":"info",
 *   "message":"Let's go!"
 * }
 **/
public class LogMessage {

    private static final String TRACE = "trace";
    private static final String INFO = "info";
    private static final String WARN = "warn";

    @JsonProperty("level")
    private final String level;

    @JsonProperty("timestamp")
    private final int timestamp;

    @JsonProperty("message")
    private final String message;

    private LogMessage(String level, String message) {
        this.level = level;
        this.timestamp = (int) (new Date().getTime() / 1000);
        this.message = message;
    }

    public static LogMessage trace(String message) {
        return new LogMessage(TRACE, message);
    }

    public static LogMessage info(String message) {
        return new LogMessage(INFO, message);
    }

    public static LogMessage warn(String message) {
        return new LogMessage(WARN, message);
    }

    @Override
    public String toString() {
        return "{" +
                "level='" + level + '\'' +
                ", timestamp=" + timestamp +
                ", message='" + message + '\'' +
                '}';
    }
}
