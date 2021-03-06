package ru.chernyshev.control.service;

import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.chernyshev.control.dto.LogMessage;
import ru.chernyshev.control.dto.TelemetryDto;
import ru.chernyshev.control.type.ConfigurationParam;
import ru.chernyshev.control.type.TelemetryType;
import ru.chernyshev.ifaces.dto.Response;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Сервис телеметрии
 */
@Service
public class TelemetryService implements ITelemetryService {

    /**
     * Частота отправки телеметрии в секундах
     */
    private final int telemetryFreq;

    /**
     * Сервис отправки сообщение
     */
    private final IMessageSender messageSender;

    /**
     * Сервис взаимодействия с restApi
     */
    private final IRestClientService restClientService;

    private final ScheduledExecutorService scheduler;

    @Autowired
    public TelemetryService(int telemetryFreq, IMessageSender messageSender, RestClientService restClientService) {//}, SpaceshipService spaceshipService) {
        this.telemetryFreq = telemetryFreq;
        this.messageSender = messageSender;
        this.restClientService = restClientService;
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    /**
     * Начать передечу телеметрии
     */
    public void start() {
        messageSender.stdout(LogMessage.trace("Telemetry send start"));
        new Thread(this::sendTelemetry).start();
        scheduler.scheduleAtFixedRate(this::sendTelemetry, telemetryFreq, telemetryFreq, TimeUnit.SECONDS);
    }

    /**
     * Отправить сигнал телеметрии
     *
     * @param type    тип телеметрии
     * @param message сообщение для передачи
     * @see TelemetryType
     */
    public void send(TelemetryType type, String message) {
        TelemetryDto telemetry = new TelemetryDto(type, message);
        messageSender.stderr(telemetry.toJson());
    }

    private void sendTelemetry() {

        String params = Stream.of(ConfigurationParam.values())
                .filter(ConfigurationParam::isContainsInTelemetry)
                .map(ConfigurationParam::getKey)
                .collect(Collectors.joining(","));

        Response response;

        try {
            response = restClientService.get(params);
        } catch (Exception e) {
            messageSender.stdout(LogMessage.warn("Cant get telemetry " + e.toString()));
            return;
        }

        List<BasicNameValuePair> telemetryParams = response.getResponse().entrySet().stream()
                .map(p -> new BasicNameValuePair(p.getKey(), String.valueOf(p.getValue() == null ? "null" : p.getValue().getValue())))
                .collect(Collectors.toList());

        String format = URLEncodedUtils.format(telemetryParams, "UTF-8");
        send(TelemetryType.VALUES, format);
    }
}
