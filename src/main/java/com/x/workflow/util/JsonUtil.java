package com.x.workflow.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {

    public static <T> T parse(String object, TypeReference<T> clazz) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(object, clazz);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
