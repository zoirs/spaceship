package ru.chernyshev.control.service;

import ru.chernyshev.control.dto.FlyProgram;

/**
 * Сервис загрузки программы полета и запуска ее на выполенние
 */
public interface IProgramLoader {

    /**
     * Запустить программу полета на выполнение
     */
    void execute(FlyProgram flyProgram);

    /**
     * @return программу полета
     */
    FlyProgram getFlyProgram();
}
