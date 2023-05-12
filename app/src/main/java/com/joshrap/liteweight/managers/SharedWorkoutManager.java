package com.joshrap.liteweight.managers;

import com.joshrap.liteweight.models.ErrorTypes;
import com.joshrap.liteweight.models.LiteWeightNetworkException;
import com.joshrap.liteweight.models.Result;
import com.joshrap.liteweight.models.UserAndWorkout;
import com.joshrap.liteweight.models.sharedWorkout.SharedWorkout;
import com.joshrap.liteweight.models.user.User;
import com.joshrap.liteweight.models.workout.Workout;
import com.joshrap.liteweight.repositories.currentUser.CurrentUserRepository;
import com.joshrap.liteweight.repositories.sharedWorkouts.SharedWorkoutRepository;
import com.joshrap.liteweight.repositories.sharedWorkouts.responses.AcceptWorkoutResponse;
import com.joshrap.liteweight.repositories.users.UsersRepository;
import com.joshrap.liteweight.repositories.workouts.WorkoutRepository;
import com.joshrap.liteweight.providers.CurrentUserAndWorkoutProvider;

import java.io.IOException;

import javax.inject.Inject;

public class SharedWorkoutManager {

    @Inject
    WorkoutRepository workoutRepository;
    @Inject
    SharedWorkoutRepository sharedWorkoutRepository;
    @Inject
    UsersRepository usersRepository;
    @Inject
    CurrentUserRepository currentUserRepository;
    @Inject
    CurrentUserAndWorkoutProvider currentUserAndWorkoutProvider;

    @Inject
    public SharedWorkoutManager(WorkoutRepository workoutRepository, CurrentUserAndWorkoutProvider currentUserAndWorkoutProvider,
                                SharedWorkoutRepository sharedWorkoutRepository, UsersRepository usersRepository, CurrentUserRepository currentUserRepository) {
        this.currentUserAndWorkoutProvider = currentUserAndWorkoutProvider;
        this.workoutRepository = workoutRepository;
        this.sharedWorkoutRepository = sharedWorkoutRepository;
        this.usersRepository = usersRepository;
        this.currentUserRepository = currentUserRepository;
    }

    public Result<String> shareWorkout(String recipientUsername, String workoutId) {
        Result<String> result = new Result<>();

        try {
            User user = currentUserAndWorkoutProvider.provideCurrentUser();

            this.usersRepository.shareWorkout(recipientUsername, workoutId);
            user.setWorkoutsSent(user.getWorkoutsSent() + 1);
        } catch (IOException e) {
            result.setErrorMessage("There was a problem sharing the workout.");
        } catch (LiteWeightNetworkException e) {
            if (e.getErrorType().equals(ErrorTypes.userNotFound)) {
                result.setErrorMessage("User does not exist.");
            } else {
                result.setErrorMessage("There was a problem sharing the workout.");
            }
        }
        return result;
    }

    public Result<SharedWorkout> getReceivedWorkout(final String sharedWorkoutId) {
        Result<SharedWorkout> result = new Result<>();

        try {
            SharedWorkout sharedWorkout = this.sharedWorkoutRepository.getReceivedWorkout(sharedWorkoutId);
            result.setData(sharedWorkout);
        } catch (Exception e) {
            result.setErrorMessage("There was a problem getting the workout.");
        }
        return result;
    }

    public Result<String> acceptReceivedWorkout(final String sharedWorkoutId, final String optionalName) {
        Result<String> result = new Result<>();

        try {
            User user = currentUserAndWorkoutProvider.provideCurrentUser();
            UserAndWorkout currentUserAndWorkout = currentUserAndWorkoutProvider.provideCurrentUserAndWorkout();

            AcceptWorkoutResponse response = this.sharedWorkoutRepository.acceptReceivedWorkout(sharedWorkoutId, optionalName);
            if (user.getCurrentWorkoutId() == null) {
                // this newly accepted workout is the only workout the user now owns, so make it the current one
                String newWorkoutId = response.getNewWorkoutInfo().getWorkoutId();
                Workout workout = this.workoutRepository.getWorkout(newWorkoutId);
                this.currentUserRepository.setCurrentWorkout(newWorkoutId);
                user.setCurrentWorkoutId(newWorkoutId);
                currentUserAndWorkout.setWorkout(workout);
            }

            user.addWorkout(response.getNewWorkoutInfo());
            user.addNewExercises(response.getNewExercises());
            user.removeReceivedWorkout(sharedWorkoutId);
        } catch (Exception e) {
            result.setErrorMessage("There was a problem accepting the workout.");
        }
        return result;
    }

    public Result<String> declineReceivedWorkout(String sharedWorkoutId) {
        Result<String> result = new Result<>();

        try {
            User user = currentUserAndWorkoutProvider.provideCurrentUser();

            this.sharedWorkoutRepository.declineReceivedWorkout(sharedWorkoutId);
            user.removeReceivedWorkout(sharedWorkoutId);
        } catch (Exception e) {
            result.setErrorMessage("There was a problem declining the workout.");
        }
        return result;
    }
}
