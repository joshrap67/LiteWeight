package com.joshrap.liteweight.providers;

import com.joshrap.liteweight.models.User;
import com.joshrap.liteweight.models.CurrentUserAndWorkout;
import com.joshrap.liteweight.models.Workout;

import javax.inject.Inject;

public class CurrentUserAndWorkoutProvider {

    private static CurrentUserAndWorkout currentUserAndWorkout;

    @Inject
    public CurrentUserAndWorkoutProvider() {

    }

    public void setCurrentUserAndWorkout(CurrentUserAndWorkout aCurrentUserAndWorkout) {
        currentUserAndWorkout = aCurrentUserAndWorkout;
    }

    public CurrentUserAndWorkout provideCurrentUserAndWorkout() {
        return currentUserAndWorkout;
    }

    public User provideCurrentUser() {
        return currentUserAndWorkout.getUser();
    }

    public Workout provideCurrentWorkout() {
        return currentUserAndWorkout.getWorkout();
    }
}
