package ru.chernyshev.control.service;

import ru.chernyshev.control.type.TelemetryType;

public interface ITelemetryService {

    void start();

    void send(TelemetryType type, String message);
}
