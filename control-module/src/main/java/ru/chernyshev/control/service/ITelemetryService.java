package ru.chernyshev.control.service;

import ru.chernyshev.control.type.TelemetryType;

/**
 * Сервис телеметрии
 */
public interface ITelemetryService {

    /**
     * Начать передечу телеметрии
     */
    void start();

    /**
     * Отправить сигнал телеметрии
     *
     * @param type    тип телеметрии
     * @param message сообщение для передачи
     * @see TelemetryType
     */
    void send(TelemetryType type, String message);
}
