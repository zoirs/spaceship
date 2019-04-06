package ru.chernyshev.control.service;

import ru.chernyshev.control.dto.LogMessage;

public interface IMessageSender {

    void stderr(String message);

    void stdout(LogMessage log);
}
