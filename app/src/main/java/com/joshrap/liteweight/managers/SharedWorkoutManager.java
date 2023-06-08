package com.joshrap.liteweight.managers;

import static com.joshrap.liteweight.utils.NetworkUtils.getLiteWeightError;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.joshrap.liteweight.models.ErrorTypes;
import com.joshrap.liteweight.models.LiteWeightNetworkException;
import com.joshrap.liteweight.models.Result;
import com.joshrap.liteweight.models.UserAndWorkout;
import com.joshrap.liteweight.models.sharedWorkout.SharedWorkout;
import com.joshrap.liteweight.models.user.OwnedExercise;
import com.joshrap.liteweight.models.user.OwnedExerciseWorkout;
import com.joshrap.liteweight.models.user.User;
import com.joshrap.liteweight.models.workout.Workout;
import com.joshrap.liteweight.repositories.self.SelfRepository;
import com.joshrap.liteweight.repositories.sharedWorkouts.SharedWorkoutRepository;
import com.joshrap.liteweight.repositories.sharedWorkouts.responses.AcceptWorkoutResponse;
import com.joshrap.liteweight.repositories.users.UsersRepository;
import com.joshrap.liteweight.repositories.users.responses.SearchByUsernameResponse;
import com.joshrap.liteweight.repositories.workouts.WorkoutRepository;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

public class SharedWorkoutManager {

    @Inject
    WorkoutRepository workoutRepository;
    @Inject
    SharedWorkoutRepository sharedWorkoutRepository;
    @Inject
    UsersRepository usersRepository;
    @Inject
    SelfRepository selfRepository;
    @Inject
    CurrentUserModule currentUserModule;

    @Inject
    public SharedWorkoutManager(WorkoutRepository workoutRepository, CurrentUserModule currentUserModule,
                                SharedWorkoutRepository sharedWorkoutRepository, UsersRepository usersRepository, SelfRepository selfRepository) {
        this.currentUserModule = currentUserModule;
        this.workoutRepository = workoutRepository;
        this.sharedWorkoutRepository = sharedWorkoutRepository;
        this.usersRepository = usersRepository;
        this.selfRepository = selfRepository;
    }

    public Result<String> shareWorkoutByUsername(String recipientUsername, String workoutId) {
        Result<String> result = new Result<>();

        try {
            User user = currentUserModule.getUser();

            SearchByUsernameResponse searchResult = this.usersRepository.searchByUsername(recipientUsername);
            this.usersRepository.shareWorkout(searchResult.getId(), workoutId);
            user.setWorkoutsSent(user.getWorkoutsSent() + 1);
        } catch (Exception e) {
            if (e instanceof LiteWeightNetworkException) {
                if (getLiteWeightError((LiteWeightNetworkException) e).equals(ErrorTypes.userNotFound)) {
                    result.setErrorMessage("User does not exist.");
                }
            } else {
                FirebaseCrashlytics.getInstance().recordException(e);
                result.setErrorMessage("There was a problem sharing the workout.");
            }
        }

        return result;
    }

    public Result<String> shareWorkoutByUserId(String recipientId, String workoutId) {
        Result<String> result = new Result<>();

        try {
            User user = currentUserModule.getUser();

            this.usersRepository.shareWorkout(recipientId, workoutId);
            user.setWorkoutsSent(user.getWorkoutsSent() + 1);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            result.setErrorMessage("There was a problem sharing the workout.");
        }
        return result;
    }

    public Result<SharedWorkout> getReceivedWorkout(final String sharedWorkoutId) {
        Result<SharedWorkout> result = new Result<>();

        try {
            SharedWorkout sharedWorkout = this.sharedWorkoutRepository.getReceivedWorkout(sharedWorkoutId);
            result.setData(sharedWorkout);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            result.setErrorMessage("There was a problem getting the workout.");
        }
        return result;
    }

    public Result<String> acceptReceivedWorkout(final String sharedWorkoutId, final String optionalName) {
        Result<String> result = new Result<>();

        try {
            User user = currentUserModule.getUser();
            UserAndWorkout currentUserAndWorkout = currentUserModule.getCurrentUserAndWorkout();

            AcceptWorkoutResponse response = this.sharedWorkoutRepository.acceptReceivedWorkout(sharedWorkoutId, optionalName);
            if (user.getCurrentWorkoutId() == null) {
                // this newly accepted workout is the only workout the user now owns, so make it the current one
                String newWorkoutId = response.getNewWorkoutInfo().getWorkoutId();
                Workout workout = this.workoutRepository.getWorkout(newWorkoutId);
                this.selfRepository.setCurrentWorkout(newWorkoutId);
                user.setCurrentWorkoutId(newWorkoutId);
                currentUserAndWorkout.setWorkout(workout);
            }

            user.addWorkout(response.getNewWorkoutInfo());
            List<OwnedExercise> exercises = response.getUserExercises();
            for (OwnedExercise ownedExercise : exercises) {
                Optional<OwnedExercise> alreadyOwnedExercise = user.getExercises().stream().filter(x -> x.getId().equals(ownedExercise.getId())).findFirst();
                if (alreadyOwnedExercise.isPresent()) {
                    // only the workouts would have changed from accepting
                    ownedExercise.getWorkouts().add(new OwnedExerciseWorkout(response.getNewWorkoutInfo().getWorkoutId(), response.getNewWorkoutInfo().getWorkoutName()));
                } else {
                    user.addExercise(ownedExercise);
                }
            }
            user.removeReceivedWorkout(sharedWorkoutId);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            result.setErrorMessage("There was a problem accepting the workout.");
        }
        return result;
    }

    public Result<String> declineReceivedWorkout(String sharedWorkoutId) {
        Result<String> result = new Result<>();

        try {
            User user = currentUserModule.getUser();

            this.sharedWorkoutRepository.declineReceivedWorkout(sharedWorkoutId);
            user.removeReceivedWorkout(sharedWorkoutId);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            result.setErrorMessage("There was a problem declining the workout.");
        }
        return result;
    }
}
