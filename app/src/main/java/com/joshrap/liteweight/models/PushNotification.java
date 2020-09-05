package com.joshrap.liteweight.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.joshrap.liteweight.helpers.JsonParser;
import com.joshrap.liteweight.interfaces.Model;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class PushNotification implements Model {
    public static final String PAYLOAD = "payload";
    public static final String ACTION = "action";

    private String jsonPayload;
    private String action;

    public PushNotification(Map<String, Object> json) throws JsonProcessingException {
        this.jsonPayload = JsonParser.serializeMap((Map<String, Object>) json.get(PAYLOAD));
        this.action = (String) json.get(ACTION);
    }

    @Override
    public Map<String, Object> asMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(ACTION, this.action);
        map.put(PAYLOAD, this.jsonPayload);
        return map;
    }
}
