package ru.chernyshev.spaceship.service;

import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.chernyshev.spaceship.dto.TelemetryDto;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class TelemetryService {

    private final int telemetryFreq;
    private final MessageSender messageSender;
    private final SpaceshipService spaceshipService;
    private ScheduledExecutorService scheduler;

    @Autowired
    public TelemetryService(int telemetryFreq, MessageSender messageSender, SpaceshipService spaceshipService) {
        this.telemetryFreq = telemetryFreq;
        this.messageSender = messageSender;
        this.spaceshipService = spaceshipService;
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::sendTelemetry, telemetryFreq, telemetryFreq, TimeUnit.SECONDS);
    }

    public void send(TelemetryType type, String message) {
        TelemetryDto telemetry = new TelemetryDto(type, message);
        messageSender.stderr(telemetry.toJson());
    }

    private void sendTelemetry() {
        List<BasicNameValuePair> telemetryParams = spaceshipService.getConfiguration().entrySet().stream()
                .filter(p -> p.getKey().isContainsInTelemetry())
                .map(p -> new BasicNameValuePair(p.getKey().name(), String.valueOf(p.getValue())))
                .collect(Collectors.toList());
        String format = URLEncodedUtils.format(telemetryParams, "UTF-8");
        send(TelemetryType.VALUES, format);
    }

//    public void send(TelemetryType type, Map<String, Integer> params) {
//        List<BasicNameValuePair> collect = params.entrySet().stream()
//                .map(p -> new BasicNameValuePair(p.getKey(), p.getValue().toString()))
//                .collect(Collectors.toList());
//        String format = URLEncodedUtils.format(collect, "UTF-8");
//
//        TelemetryDto telemetry = new TelemetryDto(type, format);
//        messageSender.stderr(telemetry.toJson());
//    }
}
