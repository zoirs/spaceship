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

@RestController
public class SatelliteController {

    private final SpaceshipService spaceshipService;
    private final ObjectMapper objectMapper;

    @Autowired
    public SatelliteController(SpaceshipService spaceshipService, ObjectMapper objectMapper) {
        this.spaceshipService = spaceshipService;
        this.objectMapper = objectMapper;
    }

    @RequestMapping(value = "/settings", method = RequestMethod.PATCH, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> setSettings(@RequestBody Map<String, Integer> params) throws Exception {
        System.out.println("Get request change setting");

        Response.Builder responseBuilder = Response.newBuilder();

        params.forEach((key, value) -> {
            spaceshipService.setConfigurationParam(key, value);
            responseBuilder.add(key, spaceshipService.getConfiguration(key));
        });

        System.out.println("Change setting successfully");

        return ResponseEntity.ok(objectMapper.writeValueAsString(responseBuilder.build()));
    }

    @RequestMapping(value = "/settings/{keys}", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getSettings(@PathVariable String keys) throws Exception {

        Response.Builder responseBuilder = Response.newBuilder();

        Stream.of(keys.split(",")).forEach(o -> responseBuilder.add(o, spaceshipService.getConfiguration(o)));

        return ResponseEntity.ok(objectMapper.writeValueAsString(responseBuilder.build()));
    }
}
