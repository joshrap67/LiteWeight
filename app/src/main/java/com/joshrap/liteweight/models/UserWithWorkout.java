package com.joshrap.liteweight.models;

import com.joshrap.liteweight.interfaces.Model;
import com.joshrap.liteweight.network.RequestFields;

import java.util.HashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
public class UserWithWorkout implements Model {

    @Setter(AccessLevel.PRIVATE)
    private User user;
    private Workout workout;
    private boolean workoutPresent;

    public UserWithWorkout(Map<String, Object> json) {
        this.user = new User((Map<String, Object>) json.get(RequestFields.USER));
        this.workoutPresent = (boolean) json.get(RequestFields.WORKOUT_PRESENT);
        if (!workoutPresent) {
            // would mean the user has no workouts yet
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

    @Override
    public Map<String, Object> asMap() {
        Map<String, Object> retVal = new HashMap<>();
        retVal.put(RequestFields.USER, this.user.asMap());
        if (this.workoutPresent) {
            retVal.put(RequestFields.WORKOUT, this.workout.asMap());
        } else {
            // in case the user has no workout, just return an empty map
            retVal.put(RequestFields.WORKOUT, new HashMap<>());
        }
        retVal.put(RequestFields.WORKOUT_PRESENT, this.workoutPresent);
        return retVal;
    }
}
