package com.joshrap.liteweight.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

public class JsonParser {

    public static String serializeMap(Map<String, Object> map)
            throws JsonProcessingException {
        String retVal;
        ObjectMapper mapper = new ObjectMapper();
        retVal = mapper.writeValueAsString(map);
        return retVal;
    }

    public static Map<String, Object> deserialize(String jsonString) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {
        });
    }
}
