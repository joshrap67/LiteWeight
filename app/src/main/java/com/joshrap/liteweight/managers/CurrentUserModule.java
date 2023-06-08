package com.joshrap.liteweight.managers;

import com.joshrap.liteweight.models.user.User;
import com.joshrap.liteweight.models.UserAndWorkout;
import com.joshrap.liteweight.models.user.WorkoutInfo;
import com.joshrap.liteweight.models.workout.Workout;

import javax.inject.Inject;

public class CurrentUserModule {

    private static UserAndWorkout currentUserAndWorkout;

    @Inject
    public CurrentUserModule() {
    }

    void setCurrentUserAndWorkout(UserAndWorkout aCurrentUserAndWorkout) {
        currentUserAndWorkout = aCurrentUserAndWorkout;
    }

    public boolean isWorkoutPresent() {
        return currentUserAndWorkout != null && currentUserAndWorkout.isWorkoutPresent();
    }

    public void setCurrentWeekAndDay(int currentWeek, int currentDay) {
        WorkoutInfo workoutInfo = currentUserAndWorkout.getUser().getWorkout(currentUserAndWorkout.getWorkout().getId());
        workoutInfo.setCurrentWeek(currentWeek);
        workoutInfo.setCurrentDay(currentDay);
    }

    public int getCurrentWeek() {
        WorkoutInfo workoutInfo = currentUserAndWorkout.getUser().getWorkout(currentUserAndWorkout.getWorkout().getId());
        return workoutInfo.getCurrentWeek();
    }

    public int getCurrentDay() {
        WorkoutInfo workoutInfo = currentUserAndWorkout.getUser().getWorkout(currentUserAndWorkout.getWorkout().getId());
        return workoutInfo.getCurrentDay();
    }

    public UserAndWorkout getCurrentUserAndWorkout() {
        return currentUserAndWorkout;
    }

    public User getUser() {
        if (currentUserAndWorkout == null) {
            return null;
        } else {
            return currentUserAndWorkout.getUser();
        }
    }

    public Workout getCurrentWorkout() {
        if (currentUserAndWorkout == null) {
            return null;
        } else {
            return currentUserAndWorkout.getWorkout();
        }
    }

    public void clear() {
        currentUserAndWorkout = null;
    }
}
