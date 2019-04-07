package ru.chernyshev.control.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.chernyshev.ifaces.dto.Response;

import java.util.Map;

/**
 * Сервис взаимодействия с restApi
 */
@Service
public class RestClientService implements IRestClientService {

    /**
     * URI REST API модуля обмена информацией
     */
    private final String exchangeUri;

    /**
     * Контроллер REST API модуля обмена информацией
     */
    private final String controller = "/settings/";

    /**
     * Клиент управления http запросами
     * */
    private final RestTemplate restTemplate;

    @Autowired
    public RestClientService(RestTemplate restTemplate, String exchangeUri) {
        this.restTemplate = restTemplate;
        this.exchangeUri = exchangeUri;
    }

    /**
     * Чтение текущих параметров
     *
     * @param params список параметров системы через запятую
     * @return значения переданных параметров
     */
    public Response get(String params) {
        return restTemplate.getForObject(exchangeUri + controller + params, Response.class);
    }

    /**
     * Установка параметров
     *
     * @param param карта ключ-название параметра значение-числовое значение параметра
     * @return значения переданных параметров
     */
    public Response send(Map<String, Integer> param) throws JsonProcessingException {
        HttpEntity<String> entity = createRequest(param);
        return restTemplate.patchForObject(exchangeUri + controller, entity, Response.class);
    }

    private HttpEntity<String> createRequest(Map<String, Integer> requestParam) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();

        String jsonParams = objectMapper.writeValueAsString(requestParam);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(jsonParams, headers);
    }
}
