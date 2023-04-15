package com.joshrap.liteweight.providers;

import com.joshrap.liteweight.models.User;
import com.joshrap.liteweight.models.UserAndWorkout;
import com.joshrap.liteweight.models.Workout;

import javax.inject.Inject;

public class UserAndWorkoutProvider {

    private static UserAndWorkout userAndWorkout;

    public void setUserAndWorkout(UserAndWorkout aUserAndWorkout) {
        userAndWorkout = aUserAndWorkout;
    }

    public UserAndWorkout provideUserAndWorkout() {
        return userAndWorkout;
    }

    public User provideUser() {
        return userAndWorkout.getUser();
    }

    public Workout provideCurrentWorkout() {
        return userAndWorkout.getWorkout();
    }

    @Inject
    public UserAndWorkoutProvider() {

    }
}
