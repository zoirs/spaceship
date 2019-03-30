package ru.chernyshev.spaceship.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.chernyshev.spaceship.dto.FlyProgramm;
import ru.chernyshev.spaceship.dto.Operation;

import javax.validation.Valid;
import java.time.Instant;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.groupingBy;

@Service
public class SpaceshipService {

    private final RestTemplate restTemplate;
    private final Logger logger = LogManager.getLogger(SpaceshipService.class);

    private final EnumMap<ConfigurationParam, Integer> configuration = new EnumMap<>(ConfigurationParam.class);
    private ScheduledExecutorService executor;

    @Autowired
    public SpaceshipService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.executor = Executors.newScheduledThreadPool(5);
    }

    public void load(@Valid FlyProgramm flyProgramm) {
        Date startupDate = Date.from(Instant.ofEpochSecond(flyProgramm.getStartUp()));

//        Map<Integer, Map<String, Integer>> commandByDelayes = flyProgramm.getOperations()
//                .stream()
//                .collect(groupingBy(Operation::getDeltaT, toMap(Operation::getVariable, Operation::getValue)));

        Map<Integer, List<Operation>> operationsByDelays = flyProgramm.getOperations()
                .stream()
                .collect(groupingBy(Operation::getDeltaT));

        for (Map.Entry<Integer, List<Operation>> entry : operationsByDelays.entrySet()) {
            long delay = getDelay(startupDate, entry.getKey());
            logger.info(delay);

            executor.schedule(
                new Command(restTemplate, entry.getValue()) // todo мб создавать внутри?
            , delay, TimeUnit.MILLISECONDS);
        }
    }

    private long getDelay(Date startupDate, Integer delayAfterStartup) {
        return new Date().getTime() - startupDate.getTime() + delayAfterStartup * 1000;
    }

    public boolean setConfigurationParam(ConfigurationParam key, int value) {
        configuration.put(key, value);
        return true;
    }
}
