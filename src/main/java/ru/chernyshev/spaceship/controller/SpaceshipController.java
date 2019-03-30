package ru.chernyshev.spaceship.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.chernyshev.spaceship.service.Command;
import ru.chernyshev.spaceship.service.ConfigurationParam;
import ru.chernyshev.spaceship.service.SpaceshipService;

import java.util.Map;

@RestController
public class SpaceshipController {
    private final Logger logger = LogManager.getLogger(Command.class);

    private final SpaceshipService spaceshipService;

    @Autowired
    public SpaceshipController(SpaceshipService spaceshipService) {
        this.spaceshipService = spaceshipService;
    }

    @RequestMapping(value = "/settings", method = RequestMethod.PATCH, produces = "application/json")
//    public String set(@RequestParam Object param) {
    public String set(@RequestBody Map<String, Integer> param) {
        logger.info("выход в контроллер");
        System.out.println(param);
        for (Map.Entry<String, Integer> p : param.entrySet()) {
            Integer value = p.getValue();
            String key = p.getKey();
            spaceshipService.setConfigurationParam(ConfigurationParam.getValueFor(key), value);
        }
        return param.toString();
    }
}
