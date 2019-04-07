package ru.chernyshev.control.type;

/**
 * Коды завершения работы приложения
 */
public enum ExitCodes {

    SUCCESS(0),
    WRONG_PROGRAM(1),
    API_ANSWER_ERROR(11),
    OPERATION_NOT_EXECUTE(12);

    private final Integer exitCode;

    ExitCodes(Integer exitCode) {
        this.exitCode = exitCode;
    }

    /**
     * @return код завершения работы приложения
     */
    public Integer getExitCode() {
        return exitCode;
    }
}
