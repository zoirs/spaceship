package ru.chernyshev.control.service;

import ru.chernyshev.control.dto.LogMessage;

/**
 * Сервис отправки сообщение
 */
public interface IMessageSender {

    /**
     * Запись сообщений в stderr
     * */
    void stderr(String message);

    /**
     * Запись сообщений в stdout
     * */
    void stdout(LogMessage log);
}
