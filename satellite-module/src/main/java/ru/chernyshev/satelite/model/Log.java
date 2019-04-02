package ru.chernyshev.satelite.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class Log {

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

    private Log(String level, String message) {
        this.level = level;
        this.timestamp = (int) (new Date().getTime() / 1000);
        this.message = message;
    }

    public static Log trace(String message) {
        return new Log(TRACE, message);
    }

    public static Log info(String message) {
        return new Log(INFO, message);
    }

    public static Log warn(String message) {
        return new Log(WARN, message);
    }

    public static Log error(String message) {
        return new Log(ERROR, message);
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
