package ru.chernyshev.control.service;

import org.springframework.stereotype.Service;
import ru.chernyshev.control.dto.FlyProgram;

@Service
public interface IProgramLoader {

    void execute(FlyProgram flyProgram);

    FlyProgram getFlyProgram();
}
