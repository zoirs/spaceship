package ru.chernyshev.control.mockObject;

import org.springframework.stereotype.Service;
import ru.chernyshev.control.service.ITelemetryService;
import ru.chernyshev.control.service.TelemetryType;

import java.util.ArrayList;
import java.util.List;

@Service
public class MockTelemetryService implements ITelemetryService {

    private List<String> messages = new ArrayList<>();

    @Override
    public void start() {
        //do nothing
    }

    @Override
    public void send(TelemetryType type, String message) {
        messages.add(message);
    }

    public List<String> getMessages() {
        return messages;
    }
}
