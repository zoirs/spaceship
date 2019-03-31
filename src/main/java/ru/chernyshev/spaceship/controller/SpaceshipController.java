package ru.chernyshev.spaceship.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import ru.chernyshev.spaceship.dto.LogDto;
import ru.chernyshev.spaceship.dto.Response;
import ru.chernyshev.spaceship.service.ConfigurationParam;
import ru.chernyshev.spaceship.service.MessageSender;
import ru.chernyshev.spaceship.service.SpaceshipService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static ru.chernyshev.spaceship.service.ConfigurationParam.getValueFor;

@RestController
public class SpaceshipController {

    private final SpaceshipService spaceshipService;
    private final ObjectMapper objectMapper;
    private final MessageSender messageSender;

    @Autowired
    public SpaceshipController(SpaceshipService spaceshipService, ObjectMapper objectMapper, MessageSender messageSender) {
        this.spaceshipService = spaceshipService;
        this.objectMapper = objectMapper;
        this.messageSender = messageSender;
    }

    @RequestMapping(value = "/settings", method = RequestMethod.PATCH, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> setSettings(@RequestBody Map<String, Integer> params) throws Exception {
        messageSender.stdout(LogDto.trace("Get request change setting"));

        Map<String, Integer> invalidParams = params.entrySet()
                .stream()
                .filter(e -> !ConfigurationParam.isValid(e.getKey(), e.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (!CollectionUtils.isEmpty(invalidParams)) {
            messageSender.stdout(LogDto.error("Invalid params " + invalidParams));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(objectMapper.writeValueAsString(invalidParams));
        }

        Response.Builder responseBuilder = Response.newBuilder();

        params.forEach((key, value) -> {
            spaceshipService.setConfigurationParam(getValueFor(key), value);
            responseBuilder.add(key, spaceshipService.getConfiguration(getValueFor(key)));
        });

        messageSender.stdout(LogDto.trace("Change setting successfully"));

        return ResponseEntity.ok(objectMapper.writeValueAsString(responseBuilder.build()));
    }

    @RequestMapping(value = "/settings/{keys}", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getSettings(@PathVariable String keys) throws Exception {
        List<ConfigurationParam> keyList = Stream.of(keys.split(","))
                .filter(ConfigurationParam::isExist)
                .map(ConfigurationParam::getValueFor)
                .collect(Collectors.toList());

        Response.Builder responseBuilder = Response.newBuilder();

        keyList.forEach(o -> responseBuilder.add(o.getKey(), spaceshipService.getConfiguration(o)));

        return ResponseEntity.ok(objectMapper.writeValueAsString(responseBuilder.build()));
    }
}
