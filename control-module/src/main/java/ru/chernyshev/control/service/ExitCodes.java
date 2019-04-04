package ru.chernyshev.control.service;

public enum ExitCodes {

    SUCCESS(0),
    PROGRAM_NOT_CORRECT(1),
    API_ANSWER_ERROR(11),
    PARAMS_NOT_MATCH(12);

    private final Integer exitCode;

    ExitCodes(Integer exitCode) {
        this.exitCode = exitCode;
    }

    public Integer getExitCode() {
        return exitCode;
    }
}
