package ru.chernyshev.control.type;

/**
 * Типы сообщений телеметрии
 * */
public enum TelemetryType {
    ERROR("error"),
    VALUES("values");

    private final String key;

    TelemetryType(String key) {
        this.key = key;
    }

    /**
     * @return наименование парамтера типа телеметрии
     * */
    public String getKey() {
        return key;
    }
}
