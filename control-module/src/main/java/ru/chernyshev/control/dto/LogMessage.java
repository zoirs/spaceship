package ru.chernyshev.control.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Обертка для сообщений лога
 * <p>
 * Формат:
 * {
 * "time":"1961-04-12T06:07:00Z",
 * "level":"info",
 * "message":"Let's go!"
 * }
 **/
public class LogMessage {

    private static final String TRACE = "trace";
    private static final String INFO = "info";
    private static final String WARN = "warn";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Уровень логирования
     */
    @JsonProperty("level")
    private final String level;

    /**
     * Время сообщения лога
     */
    @JsonProperty("timestamp")
    private final String timestamp;

    /**
     * Тескстовое сообщения лога
     */
    @JsonProperty("message")
    private final String message;

    private LogMessage(String level, String message) {
        this.level = level;
        this.timestamp = DATE_FORMAT.format(new Date());
        this.message = message;
    }

    /**
     * @param message сообщение лога
     * @return объект лога уровня trace
     */
    public static LogMessage trace(String message) {
        return new LogMessage(TRACE, message);
    }

    /**
     * @param message сообщение лога
     * @return объект лога уровня info
     */
    public static LogMessage info(String message) {
        return new LogMessage(INFO, message);
    }

    /**
     * @param message сообщение лога
     * @return объект лога уровня warn
     */
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
