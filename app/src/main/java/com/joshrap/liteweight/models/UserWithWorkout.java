package com.joshrap.liteweight.models;

import com.joshrap.liteweight.network.RequestFields;

import java.util.Map;

public class UserWithWorkout {

    private User user;
    private Workout workout;

    public UserWithWorkout(Map<String, Object> json) {
        this.user = new User((Map<String, Object>) json.get(RequestFields.USER));
        this.workout = new Workout((Map<String, Object>) json.get(RequestFields.WORKOUT));
    }
}
