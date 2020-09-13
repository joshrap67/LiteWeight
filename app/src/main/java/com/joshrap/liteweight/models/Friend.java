package com.joshrap.liteweight.models;

import com.joshrap.liteweight.interfaces.Model;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Friend implements Model {

    public static final String CONFIRMED = "confirmed";

    private String icon;
    private boolean confirmed;
    private String username;

    public Friend(Map<String, Object> json, String username) {
        this.username = username;
        this.icon = (String) json.get(User.ICON);
        this.confirmed = (boolean) json.get(CONFIRMED);
    }

    public Friend(Map<String, Object> json) {
        this.username = (String) json.get(User.USERNAME);
        this.icon = (String) json.get(User.ICON);
        this.confirmed = (boolean) json.get(CONFIRMED);
    }

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> retVal = new HashMap<>();
        retVal.put(User.ICON, this.icon);
        retVal.put(CONFIRMED, this.confirmed);
        return retVal;
    }
}
