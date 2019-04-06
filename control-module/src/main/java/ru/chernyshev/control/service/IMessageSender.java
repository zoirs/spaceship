package ru.chernyshev.control.service;

import ru.chernyshev.control.model.Log;

public interface IMessageSender {

    void stderr(String message);

    void stdout(Log log);
}
