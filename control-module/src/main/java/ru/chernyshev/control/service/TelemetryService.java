package ru.chernyshev.control.service;

import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.chernyshev.control.dto.Operation;
import ru.chernyshev.control.dto.TelemetryDto;
import ru.chernyshev.control.model.Log;
import ru.chernyshev.ifaces.dto.ConfigurationValues;
import ru.chernyshev.ifaces.dto.Response;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TelemetryService implements ITelemetryService{

    private final int telemetryFreq;
    private final MessageSender messageSender;
    private final RestClientService restClientService;
    //    private final SpaceshipService spaceshipService;
    private ScheduledExecutorService scheduler;

    @Autowired
    public TelemetryService(int telemetryFreq, MessageSender messageSender, RestClientService restClientService) {//}, SpaceshipService spaceshipService) {
        this.telemetryFreq = telemetryFreq;
        this.messageSender = messageSender;
//        this.spaceshipService = spaceshipService;
        this.restClientService = restClientService;
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public void start() {
        messageSender.stdout(Log.trace("Telemetry send start"));
        scheduler.scheduleAtFixedRate(this::sendTelemetry, telemetryFreq, telemetryFreq, TimeUnit.SECONDS);
    }

    public void send(TelemetryType type, String message) {
        TelemetryDto telemetry = new TelemetryDto(type, message);
        messageSender.stderr(telemetry.toJson());
    }

    private void sendTelemetry() {

        String params = Stream.of(ConfigurationParam.values())
                .filter(ConfigurationParam::isContainsInTelemetry)
                .map(ConfigurationParam::getKey)
                .collect(Collectors.joining(","));

        Response response = restClientService.get(params);

        List<BasicNameValuePair> telemetryParams = response.getResponse().entrySet().stream()
                .map(p -> new BasicNameValuePair(p.getKey(), String.valueOf(p.getValue() == null ? "null" : p.getValue().getValue())))
                .collect(Collectors.toList());

        String format = URLEncodedUtils.format(telemetryParams, "UTF-8");
        send(TelemetryType.VALUES, format);
    }
}
