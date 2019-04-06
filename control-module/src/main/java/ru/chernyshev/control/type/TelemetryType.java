package ru.chernyshev.control.type;

public enum TelemetryType {
    ERROR("error"),
    VALUES("values");

    private final String key;

    TelemetryType(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
