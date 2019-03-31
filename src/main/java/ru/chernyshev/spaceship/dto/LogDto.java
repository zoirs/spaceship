package ru.chernyshev.spaceship.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class LogDto {

    private static final String TRACE = "trace";
    private static final String INFO = "info";
    private static final String WARN = "warn";
    private static final String ERROR = "error";

    @JsonProperty("level")
    private String level;

    @JsonProperty("timestamp")
    private int timestamp;

    @JsonProperty("message")
    private String message;

    private LogDto(String level, String message) {
        this.level = level;
        this.timestamp = (int) (new Date().getTime() / 1000);
        this.message = message;
    }

    public static LogDto trace(String message) {
        return new LogDto(TRACE, message);
    }

    public static LogDto info(String message) {
        return new LogDto(INFO, message);
    }

    public static LogDto warn(String message) {
        return new LogDto(WARN, message);
    }

    public static LogDto error(String message) {
        return new LogDto(ERROR, message);
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
