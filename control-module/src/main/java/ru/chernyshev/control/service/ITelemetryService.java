package ru.chernyshev.control.service;

public interface ITelemetryService {

    void start();

    void send(TelemetryType type, String message);
}
