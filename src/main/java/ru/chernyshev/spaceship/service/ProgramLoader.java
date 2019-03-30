package ru.chernyshev.spaceship.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import ru.chernyshev.spaceship.dto.FlyProgramm;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Date;

@Service
public class ProgramLoader {

    private final Logger logger = LogManager.getLogger(ProgramLoader.class);

    private FlyProgramm flyProgramm;
    private SpaceshipService spaceshipService;

    @Autowired
    public ProgramLoader(SpaceshipService spaceshipService) {
        this.spaceshipService = spaceshipService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        logger.info("Программа считываем");

        ObjectMapper objectMapper = new ObjectMapper();

        Resource resource = new ClassPathResource("programm.json");
        try {
            flyProgramm = objectMapper.readValue(resource.getFile(), FlyProgramm.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //todo проверить валидность, проверить сущестование файла
        logger.info("Программа загружена");
        flyProgramm.setStartUp((int) (new Date().getTime()/1000));
        spaceshipService.load(flyProgramm);
    }
}
