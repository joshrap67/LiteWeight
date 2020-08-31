package com.joshrap.liteweight.models;

import com.joshrap.liteweight.interfaces.Model;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class PushNotification implements Model {
    public static final String PAYLOAD = "payload";
    public static final String ACTION = "action";

    private Map<String, Object> payload;
    private String action;

    public PushNotification(Map<String, Object> json) {
        this.payload = (Map<String, Object>) json.get(PAYLOAD);
        this.action = (String) json.get(ACTION);
    }

    @Override
    public Map<String, Object> asMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(ACTION, this.action);
        map.put(PAYLOAD, this.payload);
        return map;
    }
}
