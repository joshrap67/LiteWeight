package com.joshrap.liteweight.models;

import com.joshrap.liteweight.network.RequestFields;

import java.util.Map;

import lombok.Data;

@Data
public class UserWithWorkout {

    private User user;
    private Workout workout;

    public UserWithWorkout(Map<String, Object> json) {
        this.user = new User((Map<String, Object>) json.get(RequestFields.USER));
        Map<String, Object> workoutJson = (Map<String, Object>) json.get(RequestFields.WORKOUT);
        if (workoutJson.isEmpty()) {
            // would mean the user has no workouts yet
            this.workout = null;
        } else {
            this.workout = new Workout((Map<String, Object>) json.get(RequestFields.WORKOUT));
        }
    }
}
