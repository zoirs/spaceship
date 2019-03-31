package ru.chernyshev.spaceship.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ru.chernyshev.spaceship.dto.LogDto;
import ru.chernyshev.spaceship.dto.ResponseBuilder;
import ru.chernyshev.spaceship.service.ConfigurationParam;
import ru.chernyshev.spaceship.service.MessageSender;
import ru.chernyshev.spaceship.service.SpaceshipService;

import java.util.Map;
import java.util.stream.Collectors;

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
    public ResponseEntity<String> set(@RequestBody Map<String, Integer> param) throws Exception {
        messageSender.stdout(LogDto.trace("Get request change setting"));

        Map<String, Integer> invalidParams = param.entrySet()
                .stream()
                .filter(e -> !ConfigurationParam.isValid(e.getKey(), e.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (!CollectionUtils.isEmpty(invalidParams)) {
            messageSender.stdout(LogDto.error("Invalid params " + invalidParams));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(objectMapper.writeValueAsString(invalidParams));
        }

        ResponseBuilder responseBuilder = new ResponseBuilder();

        for (Map.Entry<String, Integer> p : param.entrySet()) {
            ConfigurationParam key = getValueFor(p.getKey());
            Integer oldValue = spaceshipService.getConfiguration(key);
            Integer newValue = p.getValue();

            spaceshipService.setConfigurationParam(key, newValue);
            responseBuilder.add(p.getKey(), newValue, oldValue);
        }
        messageSender.stdout(LogDto.trace("Change setting successfully"));

        return ResponseEntity.ok(objectMapper.writeValueAsString(responseBuilder));

    }
}
