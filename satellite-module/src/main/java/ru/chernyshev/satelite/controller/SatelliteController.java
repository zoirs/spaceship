package ru.chernyshev.satelite.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.chernyshev.ifaces.dto.Response;
import ru.chernyshev.satelite.service.SpaceshipService;

import java.util.Map;
import java.util.stream.Stream;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Контроллер эмулятора модуля, используется для отладки
 */
@RestController
public class SatelliteController {

    private final SpaceshipService spaceshipService;
    private final ObjectMapper objectMapper;

    @Autowired
    public SatelliteController(SpaceshipService spaceshipService, ObjectMapper objectMapper) {
        this.spaceshipService = spaceshipService;
        this.objectMapper = objectMapper;
    }

    /**
     * Установка параметров
     * <p>
     * Формат параметров и значений:
     * <p>
     * {
     * "orientationZenithAngleDeg": 180,
     * "orientationAzimuthAngleDeg": 0
     * }
     *
     * @return JSON-объект в формате:
     * <p>
     * {
     * "orientationZenithAngleDeg": {"set": 180, "value": 180},
     * "orientationAzimuthAngleDeg": {"set": 0, "value": 10}
     * }
     */
    @RequestMapping(value = "/settings", method = RequestMethod.PATCH, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> setSettings(@RequestBody Map<String, Integer> params) throws Exception {
        Response.Builder responseBuilder = Response.newBuilder();

        params.forEach((key, value) -> {
            spaceshipService.setConfigurationParam(key, value);
            responseBuilder.add(key, spaceshipService.getConfiguration(key));
        });

        return ResponseEntity.ok(objectMapper.writeValueAsString(responseBuilder.build()));
    }

    /**
     * Чтение параметров
     * Формат:
     * orientationZenithAngleDeg,orientationAzimuthAngleDeg
     *
     * @return JSON-объект в формате:
     * <p>
     * {
     * "orientationZenithAngleDeg": {"set": 180, "value": 180},
     * "orientationAzimuthAngleDeg": {"set": 0, "value": 10}
     * }
     */
    @RequestMapping(value = "/settings/{keys}", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getSettings(@PathVariable String keys) throws Exception {

        Response.Builder responseBuilder = Response.newBuilder();

        Stream.of(keys.split(",")).forEach(o -> responseBuilder.add(o, spaceshipService.getConfiguration(o)));

        return ResponseEntity.ok(objectMapper.writeValueAsString(responseBuilder.build()));
    }
}
