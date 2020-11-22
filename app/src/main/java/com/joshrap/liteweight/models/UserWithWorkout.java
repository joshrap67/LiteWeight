package com.joshrap.liteweight.models;

import com.joshrap.liteweight.network.RequestFields;

import java.util.Map;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
public class UserWithWorkout {

    //    @Setter(AccessLevel.PRIVATE)
    private User user;
    private Workout workout;
    private boolean workoutPresent;

    public UserWithWorkout(Map<String, Object> json) {
        this.user = new User((Map<String, Object>) json.get(RequestFields.USER));
        Map<String, Object> workoutJson = (Map<String, Object>) json.get(RequestFields.WORKOUT);
        if (workoutJson.isEmpty()) {
            // would mean the user has no workouts yet
            this.workoutPresent = false;
            this.workout = null;
        } else {
            this.workout = new Workout((Map<String, Object>) json.get(RequestFields.WORKOUT));
        }
    }

    public UserWithWorkout(final User user, final Workout workout) {
        this.user = user;
        this.workoutPresent = workout != null;
        this.workout = workout;
    }

    public void setWorkout(Workout workout) {
        this.workoutPresent = workout != null;
        this.workout = workout;
    }
}
