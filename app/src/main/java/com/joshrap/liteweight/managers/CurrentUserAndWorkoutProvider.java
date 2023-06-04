package com.joshrap.liteweight.managers;

import com.joshrap.liteweight.models.user.User;
import com.joshrap.liteweight.models.UserAndWorkout;
import com.joshrap.liteweight.models.user.WorkoutInfo;
import com.joshrap.liteweight.models.workout.Workout;

import javax.inject.Inject;

// rename module
public class CurrentUserAndWorkoutProvider {

    private static UserAndWorkout currentUserAndWorkout;

    @Inject
    public CurrentUserAndWorkoutProvider() {
    }

    void setCurrentUserAndWorkout(UserAndWorkout aCurrentUserAndWorkout) {
        currentUserAndWorkout = aCurrentUserAndWorkout;
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

    public UserAndWorkout provideCurrentUserAndWorkout() {
        return currentUserAndWorkout;
    }

    public User provideCurrentUser() {
        if (currentUserAndWorkout == null) {
            return null;
        } else {
            return currentUserAndWorkout.getUser();
        }
    }

    public Workout provideCurrentWorkout() {
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
