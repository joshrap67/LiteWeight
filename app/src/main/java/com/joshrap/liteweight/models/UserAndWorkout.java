package com.joshrap.liteweight.models;

import com.joshrap.liteweight.models.user.User;
import com.joshrap.liteweight.models.workout.Workout;

import lombok.Data;

@Data
public class UserAndWorkout {

    private User user;
    private Workout workout;
    private boolean workoutPresent;

    public void setWorkout(Workout workout) {
        this.workoutPresent = workout != null;
        this.workout = workout;
    }
}
