package com.joshrap.liteweight.models;

import com.joshrap.liteweight.interfaces.Model;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class FriendRequest implements Model {

    public static final String ICON = "icon";
    public static final String SEEN = "seen";
    public static final String REQUEST_TIME_STAMP = "timeStamp";

    private String icon;
    private boolean seen;
    private String requestTimeStamp;
    private String username;

    public FriendRequest(Map<String, Object> jsonMap, String username) {
        this.setUsername(username);
        this.setIcon((String) jsonMap.get(ICON));
        this.setSeen((Boolean) jsonMap.get(SEEN));
        this.setRequestTimeStamp((String) jsonMap.get(REQUEST_TIME_STAMP));
    }

    public FriendRequest(Map<String, Object> jsonMap) {
        this.setUsername((String) jsonMap.get(User.USERNAME));
        this.setIcon((String) jsonMap.get(ICON));
        this.setSeen((Boolean) jsonMap.get(SEEN));
        this.setRequestTimeStamp((String) jsonMap.get(REQUEST_TIME_STAMP));
    }

    @Override
    public Map<String, Object> asMap() {
        Map<String, Object> retVal = new HashMap<>();
        retVal.put(ICON, this.getIcon());
        retVal.put(SEEN, this.isSeen());
        retVal.put(REQUEST_TIME_STAMP, this.getRequestTimeStamp());
        return retVal;
    }
}