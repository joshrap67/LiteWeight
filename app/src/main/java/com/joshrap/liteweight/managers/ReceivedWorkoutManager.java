package com.joshrap.liteweight.managers;

import static com.joshrap.liteweight.utils.NetworkUtils.getLiteWeightError;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.joshrap.liteweight.models.ErrorTypes;
import com.joshrap.liteweight.models.LiteWeightNetworkException;
import com.joshrap.liteweight.models.Result;
import com.joshrap.liteweight.models.UserAndWorkout;
import com.joshrap.liteweight.models.receivedWorkout.ReceivedWorkout;
import com.joshrap.liteweight.models.user.OwnedExercise;
import com.joshrap.liteweight.models.user.OwnedExerciseWorkout;
import com.joshrap.liteweight.models.user.User;
import com.joshrap.liteweight.models.workout.Workout;
import com.joshrap.liteweight.repositories.self.SelfRepository;
import com.joshrap.liteweight.repositories.receivedWorkouts.ReceivedWorkoutRepository;
import com.joshrap.liteweight.repositories.receivedWorkouts.responses.AcceptWorkoutResponse;
import com.joshrap.liteweight.repositories.users.UsersRepository;
import com.joshrap.liteweight.repositories.users.responses.SearchByUsernameResponse;
import com.joshrap.liteweight.repositories.workouts.WorkoutRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

public class ReceivedWorkoutManager {

    @Inject
    WorkoutRepository workoutRepository;
    @Inject
    ReceivedWorkoutRepository receivedWorkoutRepository;
    @Inject
    UsersRepository usersRepository;
    @Inject
    SelfRepository selfRepository;
    @Inject
    CurrentUserModule currentUserModule;

    @Inject
    public ReceivedWorkoutManager(WorkoutRepository workoutRepository, CurrentUserModule currentUserModule,
                                  ReceivedWorkoutRepository receivedWorkoutRepository, UsersRepository usersRepository, SelfRepository selfRepository) {
        this.currentUserModule = currentUserModule;
        this.workoutRepository = workoutRepository;
        this.receivedWorkoutRepository = receivedWorkoutRepository;
        this.usersRepository = usersRepository;
        this.selfRepository = selfRepository;
    }

    public Result<String> sendWorkoutByUsername(String recipientUsername, String workoutId) {
        Result<String> result = new Result<>();

        try {
            User user = currentUserModule.getUser();

            SearchByUsernameResponse searchResult = this.usersRepository.searchByUsername(recipientUsername);
            this.usersRepository.sendWorkout(searchResult.getId(), workoutId);
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

    public Result<String> sendWorkoutByUserId(String recipientId, String workoutId) {
        Result<String> result = new Result<>();

        try {
            User user = currentUserModule.getUser();

            this.usersRepository.sendWorkout(recipientId, workoutId);
            user.setWorkoutsSent(user.getWorkoutsSent() + 1);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            result.setErrorMessage("There was a problem sharing the workout.");
        }
        return result;
    }

    public Result<ReceivedWorkout> getReceivedWorkout(final String receivedWorkoutId) {
        Result<ReceivedWorkout> result = new Result<>();

        try {
            ReceivedWorkout receivedWorkout = this.receivedWorkoutRepository.getReceivedWorkout(receivedWorkoutId);
            result.setData(receivedWorkout);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            result.setErrorMessage("There was a problem getting the workout.");
        }
        return result;
    }

    public Result<String> acceptReceivedWorkout(final String receivedWorkoutId, final String optionalName) {
        Result<String> result = new Result<>();

        try {
            User user = currentUserModule.getUser();
            UserAndWorkout currentUserAndWorkout = currentUserModule.getCurrentUserAndWorkout();

            AcceptWorkoutResponse response = this.receivedWorkoutRepository.acceptReceivedWorkout(receivedWorkoutId, optionalName);
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
            Map<String, OwnedExercise> idToExercise = user.getExercises().stream().collect(Collectors.toMap(OwnedExercise::getId, x -> x));
            for (OwnedExercise ownedExercise : exercises) {
                OwnedExercise alreadyOwnedExercise = idToExercise.get(ownedExercise.getId());
                if (alreadyOwnedExercise != null) {
                    // only the workouts would have changed from accepting
                    ownedExercise.getWorkouts().add(new OwnedExerciseWorkout(response.getNewWorkoutInfo().getWorkoutId(), response.getNewWorkoutInfo().getWorkoutName()));
                } else {
                    // newly added exercise from accepting the workout
                    user.addExercise(ownedExercise);
                }
            }
            user.removeReceivedWorkout(receivedWorkoutId);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            result.setErrorMessage("There was a problem accepting the workout.");
        }
        return result;
    }

    public Result<String> declineReceivedWorkout(String receivedWorkoutId) {
        Result<String> result = new Result<>();

        try {
            User user = currentUserModule.getUser();

            this.receivedWorkoutRepository.declineReceivedWorkout(receivedWorkoutId);
            user.removeReceivedWorkout(receivedWorkoutId);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            result.setErrorMessage("There was a problem declining the workout.");
        }
        return result;
    }
}
