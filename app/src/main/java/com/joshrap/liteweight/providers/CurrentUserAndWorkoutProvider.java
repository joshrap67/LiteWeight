package com.joshrap.liteweight.providers;

import com.joshrap.liteweight.models.User;
import com.joshrap.liteweight.models.UserAndWorkout;
import com.joshrap.liteweight.models.Workout;

import javax.inject.Inject;

public class CurrentUserAndWorkoutProvider {

    private static UserAndWorkout currentUserAndWorkout;

    @Inject
    public CurrentUserAndWorkoutProvider() {

    }

    public void setCurrentUserAndWorkout(UserAndWorkout aCurrentUserAndWorkout) {
        currentUserAndWorkout = aCurrentUserAndWorkout;
    }

    public UserAndWorkout provideCurrentUserAndWorkout() {
        return currentUserAndWorkout;
    }

    public User provideCurrentUser() {
        return currentUserAndWorkout.getUser();
    }

    public Workout provideCurrentWorkout() {
        return currentUserAndWorkout.getWorkout();
    }
}
