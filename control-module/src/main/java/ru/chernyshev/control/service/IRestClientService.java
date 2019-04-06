package ru.chernyshev.control.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import ru.chernyshev.ifaces.dto.Response;

import java.util.Map;

public interface IRestClientService {
    Response get(String param);

    Response send(Map<String, Integer> param) throws JsonProcessingException;

}
