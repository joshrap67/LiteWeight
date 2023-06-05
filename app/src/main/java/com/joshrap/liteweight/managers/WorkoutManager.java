package com.joshrap.liteweight.managers;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.joshrap.liteweight.models.user.OwnedExercise;
import com.joshrap.liteweight.repositories.self.SelfRepository;
import com.joshrap.liteweight.models.Result;
import com.joshrap.liteweight.models.workout.Routine;
import com.joshrap.liteweight.models.user.User;
import com.joshrap.liteweight.models.UserAndWorkout;
import com.joshrap.liteweight.models.workout.Workout;
import com.joshrap.liteweight.models.user.WorkoutInfo;
import com.joshrap.liteweight.repositories.workouts.WorkoutRepository;

import java.time.Instant;

import javax.inject.Inject;

import lombok.NonNull;

public class WorkoutManager {

    @Inject
    WorkoutRepository workoutRepository;
    @Inject
    SelfRepository selfRepository;
    @Inject
    CurrentUserModule currentUserModule;

    @Inject
    public WorkoutManager(WorkoutRepository workoutRepository, CurrentUserModule currentUserModule, SelfRepository selfRepository) {
        this.currentUserModule = currentUserModule;
        this.workoutRepository = workoutRepository;
        this.selfRepository = selfRepository;
    }

    public Result<UserAndWorkout> createWorkout(@NonNull Routine routine, @NonNull String workoutName) {
        Result<UserAndWorkout> result = new Result<>();

        try {
            User user = currentUserModule.getUser();
            UserAndWorkout currentUserAndWorkout = currentUserModule.getCurrentUserAndWorkout();

            UserAndWorkout response = this.workoutRepository.createWorkout(routine, workoutName, true);
            String newWorkoutId = response.getWorkout().getId();

            user.setCurrentWorkoutId(response.getUser().getCurrentWorkoutId());
            user.addWorkout(response.getUser().getWorkout(newWorkoutId));
            user.updateOwnedExercises(response.getUser().getExercises());

            currentUserAndWorkout.setWorkout(response.getWorkout());
            result.setData(response);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            result.setErrorMessage("There was a problem creating the workout.");
        }
        return result;
    }

    public Result<String> copyWorkout(String workoutId, @NonNull String workoutName) {
        Result<String> result = new Result<>();

        try {
            User user = currentUserModule.getUser();
            UserAndWorkout userAndWorkout = currentUserModule.getCurrentUserAndWorkout();

            // make sure to update progress before switching
            this.workoutRepository.updateWorkoutProgress(
                    currentUserModule.getCurrentWeek(),
                    currentUserModule.getCurrentDay(),
                    userAndWorkout.getWorkout());

            UserAndWorkout copyResponse = this.workoutRepository.copyWorkout(workoutId, workoutName);
            Workout newWorkout = copyResponse.getWorkout();
            this.selfRepository.setCurrentWorkout(newWorkout.getId());

            user.addWorkout(copyResponse.getUser().getWorkout(newWorkout.getId()));
            userAndWorkout.setWorkout(newWorkout);
            user.setCurrentWorkoutId(newWorkout.getId());
            user.updateOwnedExercises(copyResponse.getUser().getExercises());

            result.setData(newWorkout.getId());
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            result.setErrorMessage("There was a problem copying the workout.");
        }

        return result;
    }

    public Result<String> switchWorkout(String oldWorkoutId, @NonNull String nextWorkoutId) {
        Result<String> result = new Result<>();

        try {
            User user = currentUserModule.getUser();
            UserAndWorkout userAndWorkout = currentUserModule.getCurrentUserAndWorkout();

            // make sure to update progress before switching
            this.workoutRepository.updateWorkoutProgress(
                    currentUserModule.getCurrentWeek(),
                    currentUserModule.getCurrentDay(),
                    userAndWorkout.getWorkout());
            Workout workout = this.workoutRepository.getWorkout(nextWorkoutId);
            this.selfRepository.setCurrentWorkout(nextWorkoutId);

            userAndWorkout.setWorkout(workout);
            user.setCurrentWorkoutId(nextWorkoutId);

            String now = Instant.now().toString();
            WorkoutInfo workoutInfo = user.getWorkout(oldWorkoutId);
            workoutInfo.setLastSetAsCurrentUtc(now);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            result.setErrorMessage("There was a problem switching to the workout.");
        }

        return result;
    }

    public Result<String> renameWorkout(@NonNull String workoutId, @NonNull String newWorkoutName) {
        Result<String> result = new Result<>();

        try {
            User user = currentUserModule.getUser();
            Workout currentWorkout = currentUserModule.getCurrentWorkout();

            this.workoutRepository.renameWorkout(workoutId, newWorkoutName);
            for (OwnedExercise exercise : user.getExercises()) {
                exercise.updateWorkoutName(workoutId, newWorkoutName);
            }
            user.getWorkout(currentWorkout.getId()).setWorkoutName(newWorkoutName);
            currentWorkout.setName(newWorkoutName);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            result.setErrorMessage("There was a problem renaming the workout.");
        }
        return result;
    }

    public Result<String> deleteWorkoutThenFetchNext(String workoutId, String nextWorkoutId) {
        Result<String> result = new Result<>();

        try {
            User user = currentUserModule.getUser();
            UserAndWorkout currentUserAndWorkout = currentUserModule.getCurrentUserAndWorkout();

            this.workoutRepository.deleteWorkoutAndSetCurrent(workoutId, nextWorkoutId);
            Workout workout = null;
            if (nextWorkoutId != null) {
                workout = this.workoutRepository.getWorkout(nextWorkoutId);
            }

            user.setCurrentWorkoutId(nextWorkoutId);
            currentUserAndWorkout.setWorkout(workout);
            user.removeWorkout(workoutId);
            for (OwnedExercise exercise : user.getExercises()) {
                exercise.removeWorkout(workoutId);
            }
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            result.setErrorMessage("There was a problem deleting the workout.");
        }

        return result;
    }

    public Result<String> resetWorkoutStatistics(@NonNull String workoutId) {
        Result<String> result = new Result<>();

        User user = currentUserModule.getUser();
        try {
            this.workoutRepository.resetWorkoutStatistics(workoutId);
            WorkoutInfo workoutInfo = user.getWorkout(workoutId);
            workoutInfo.setTimesRestarted(0);
            workoutInfo.setAverageExercisesCompleted(0.0);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            result.setErrorMessage("There was a problem resetting the workout statistics.");
        }
        return result;
    }

    public Result<String> updateWorkoutProgress(int currentWeek, int currentDay, @NonNull Workout workout) {
        Result<String> result = new Result<>();

        try {
            this.workoutRepository.updateWorkoutProgress(currentWeek, currentDay, workout);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            result.setErrorMessage("There was a problem updating the workout.");
        }
        return result;
    }

    public Result<UserAndWorkout> setRoutine(@NonNull String workoutId, @NonNull Routine routine) {
        Result<UserAndWorkout> result = new Result<>();

        try {
            User user = currentUserModule.getUser();
            UserAndWorkout currentUserAndWorkout = currentUserModule.getCurrentUserAndWorkout();

            UserAndWorkout response = this.workoutRepository.setRoutine(workoutId, routine);
            user.updateOwnedExercises(response.getUser().getExercises());
            currentUserAndWorkout.setWorkout(response.getWorkout());

            result.setData(currentUserAndWorkout);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            result.setErrorMessage("There was a problem updating the routine.");
        }
        return result;
    }

    public Result<String> restartWorkout(@NonNull Workout workout) {
        Result<String> result = new Result<>();

        try {
            User user = currentUserModule.getUser();
            Workout currentWorkout = currentUserModule.getCurrentWorkout();

            UserAndWorkout response = this.workoutRepository.restartWorkout(workout);

            // update the statistics for this workout
            WorkoutInfo workoutInfo = user.getWorkout(workout.getId());
            WorkoutInfo newWorkoutInfo = response.getUser().getWorkout(workout.getId());
            workoutInfo.update(newWorkoutInfo);

            // in case any default weights were updated
            user.updateOwnedExercises(response.getUser().getExercises());

            currentWorkout.setRoutine(response.getWorkout().getRoutine());
            workoutInfo.setCurrentDay(0);
            workoutInfo.setCurrentWeek(0);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            result.setErrorMessage("There was a problem restarting the workout.");
        }
        return result;
    }
}
