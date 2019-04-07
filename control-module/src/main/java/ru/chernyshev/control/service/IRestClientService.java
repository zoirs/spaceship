package ru.chernyshev.control.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import ru.chernyshev.ifaces.dto.Response;

import java.util.Map;

/**
 * Сервис взаимодействия с restApi
 */
public interface IRestClientService {
    /**
     * Чтение текущих параметров
     *
     * @param params список параметров системы через запятую
     * @return значения переданных параметров
     */
    Response get(String params);

    /**
     * Установка параметров
     *
     * @param param карта ключ-название параметра значение-числовое значение параметра
     * @return значения переданных параметров
     */
    Response send(Map<String, Integer> param) throws JsonProcessingException;

}
