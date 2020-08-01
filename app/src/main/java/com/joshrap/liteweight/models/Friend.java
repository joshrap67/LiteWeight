package com.joshrap.liteweight.models;

import com.joshrap.liteweight.interfaces.Model;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class Friend implements Model {

    public static final String CONFIRMED = "confirmed";

    private String icon;
    private boolean confirmed;

    public Friend(Map<String, Object> json) {
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
