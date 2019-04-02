package ru.chernyshev.satelite.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import ru.chernyshev.ifaces.dto.Response;
import ru.chernyshev.satelite.model.Log;
import ru.chernyshev.satelite.service.ConfigurationParam;
import ru.chernyshev.satelite.service.MessageSender;
import ru.chernyshev.satelite.service.SpaceshipService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

//import ru.chernyshev.control.service.MessageSender;
//import ru.chernyshev.control.service.SpaceshipService;
//import ru.chernyshev.control.dto.Log;
//import ru.chernyshev.control.dto.Response;
//import ru.chernyshev.control.service.ConfigurationParam;
//import static ru.chernyshev.control.service.ConfigurationParam.getValueFor;

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
        messageSender.stdout(Log.trace("Get request change setting"));

        Map<String, Integer> invalidParams = params.entrySet()
                .stream()
                .filter(e -> !ConfigurationParam.isValid(e.getKey(), e.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (!CollectionUtils.isEmpty(invalidParams)) {
            messageSender.stdout(Log.error("Invalid params " + invalidParams));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(objectMapper.writeValueAsString(invalidParams));
        }

        Response.Builder responseBuilder = Response.newBuilder();

        params.forEach((key, value) -> {
            spaceshipService.setConfigurationParam(ConfigurationParam.getValueFor(key), value);
            responseBuilder.add(key, spaceshipService.getConfiguration(ConfigurationParam.getValueFor(key)));
        });

        messageSender.stdout(Log.trace("Change setting successfully"));

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
