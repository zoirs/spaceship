package ru.chernyshev.spaceship.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.chernyshev.spaceship.dto.FlyProgramm;
import ru.chernyshev.spaceship.dto.LogDto;
import ru.chernyshev.spaceship.dto.Operation;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.groupingBy;

@Service
public class ProgramLoader {

    private final MessageSender messageSender;
    private final RestTemplate restTemplate;
    private final TelemetryService telemetryService;
    private final ObjectMapper objectMapper;
    private final String flightProgramPath;

    private final ScheduledExecutorService executor;

    private FlyProgramm flyProgram;

    @Autowired
    public ProgramLoader(RestTemplate restTemplate,
                         TelemetryService telemetryService,
                         MessageSender messageSender,
                         ObjectMapper objectMapper,
                         String flightProgramPath) {
        this.restTemplate = restTemplate;
        this.telemetryService = telemetryService;
        this.messageSender = messageSender;
        this.objectMapper = objectMapper;
        this.flightProgramPath = flightProgramPath;

        this.executor = Executors.newScheduledThreadPool(5);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        messageSender.stdout(LogDto.trace("Start program loading"));

        File fileProgram = new File(flightProgramPath);
        if (!fileProgram.exists() || !fileProgram.isFile()) {
            telemetryService.send(TelemetryType.ERROR, "Fly program not found " + flightProgramPath);
            return;
        }

        try {
            flyProgram = objectMapper.readValue(fileProgram, FlyProgramm.class);
        } catch (IOException e) {
            telemetryService.send(TelemetryType.ERROR, "Cant read programm " + e.getMessage());
            return;
        }
        //todo проверить валидность, проверить сущестование файла
        messageSender.stdout(LogDto.trace("Finish program loading"));
        telemetryService.start();
        flyProgram.setStartUp((int) (new Date().getTime()/1000)); // todo убрать, это для теста
        execute(flyProgram);
    }

    public FlyProgramm getFlyProgram() {
        return flyProgram;
    }

    private void execute(@Valid FlyProgramm flyProgramm) {
        messageSender.stdout(LogDto.trace("Start execute program"));

        Date startupDate = Date.from(Instant.ofEpochSecond(flyProgramm.getStartUp()));

        Map<Integer, List<Operation>> operationsByDelays = flyProgramm.getOperations()
                .stream()
                .collect(groupingBy(Operation::getDeltaT));

        for (Map.Entry<Integer, List<Operation>> entry : operationsByDelays.entrySet()) {
            long delay = getDelay(startupDate, entry.getKey());

            executor.schedule(
                    new Command(restTemplate, telemetryService, entry.getValue(), messageSender) // todo мб создавать внутри?
                    , delay, TimeUnit.MILLISECONDS);
        }
        messageSender.stdout(LogDto.trace("Execute program scheduled"));
    }

    private long getDelay(Date startupDate, Integer delayAfterStartup) {
        return new Date().getTime() - startupDate.getTime() + delayAfterStartup * 1000;
    }
}
