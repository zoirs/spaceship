package ru.chernyshev.control.service;

import com.fasterxml.jackson.databind.ObjectMapper;

class ProgramLoaderImpl extends ProgramLoader {
    ProgramLoaderImpl(IRestClientService restClientService, ITelemetryService telemetryService, IMessageSender messageSender, ObjectMapper objectMapper, String flightProgramPath) {
        super(restClientService, telemetryService, messageSender, objectMapper, flightProgramPath);
    }

    @Override
    void waitExecution(int commandCount) {
        //do nothing
    }
}
