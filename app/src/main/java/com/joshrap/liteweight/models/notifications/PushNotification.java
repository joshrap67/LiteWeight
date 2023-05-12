package com.joshrap.liteweight.models.notifications;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.joshrap.liteweight.utils.JsonUtils;

import java.util.Map;

import lombok.Data;

@Data
public class PushNotification {
    public static final String PAYLOAD = "payload";
    public static final String ACTION = "action";

    private String jsonPayload;
    private String action;

    public PushNotification(Map<String, Object> json) throws JsonProcessingException {
        this.jsonPayload = JsonUtils.serializeMap((Map<String, Object>) json.get(PAYLOAD));
        this.action = (String) json.get(ACTION);
    }
}
