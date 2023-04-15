package com.joshrap.liteweight.managers;

import com.joshrap.liteweight.models.AcceptWorkoutResponse;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.Routine;
import com.joshrap.liteweight.models.SharedWorkout;
import com.joshrap.liteweight.models.SharedWorkoutMeta;
import com.joshrap.liteweight.models.User;
import com.joshrap.liteweight.models.UserAndWorkout;
import com.joshrap.liteweight.models.Workout;
import com.joshrap.liteweight.models.WorkoutMeta;
import com.joshrap.liteweight.network.repos.WorkoutRepository;
import com.joshrap.liteweight.providers.UserAndWorkoutProvider;

import java.util.List;

import javax.inject.Inject;

import lombok.NonNull;

public class WorkoutManager {

    @Inject
    WorkoutRepository workoutRepository;
    @Inject
    UserAndWorkoutProvider userAndWorkoutProvider;

    @Inject
    public WorkoutManager(WorkoutRepository workoutRepository, UserAndWorkoutProvider userAndWorkoutProvider) {
        this.userAndWorkoutProvider = userAndWorkoutProvider;
        this.workoutRepository = workoutRepository;
    }

    public ResultStatus<UserAndWorkout> createWorkout(@NonNull Routine routine, @NonNull String workoutName) {
        User user = userAndWorkoutProvider.provideUser();
        UserAndWorkout userAndWorkout = userAndWorkoutProvider.provideUserAndWorkout();
        ResultStatus<UserAndWorkout> resultStatus = this.workoutRepository.createWorkout(routine, workoutName);
        if (resultStatus.isSuccess()) {
            String newWorkoutId = resultStatus.getData().getWorkout().getWorkoutId();
            user.setCurrentWorkout(resultStatus.getData().getUser().getCurrentWorkout());
            user.putWorkout(resultStatus.getData().getUser().getWorkout(newWorkoutId));
            user.updateOwnedExercises(resultStatus.getData().getUser().getOwnedExercises());

            userAndWorkout.setWorkout(resultStatus.getData().getWorkout());
        }
        return resultStatus;
    }

    public ResultStatus<UserAndWorkout> copyWorkout(@NonNull Workout workout, @NonNull String workoutName) {
        User user = userAndWorkoutProvider.provideUser();
        UserAndWorkout userAndWorkout = userAndWorkoutProvider.provideUserAndWorkout();

        ResultStatus<UserAndWorkout> resultStatus = this.workoutRepository.copyWorkout(workout, workoutName);
        if (resultStatus.isSuccess()) {
            Workout newWorkout = resultStatus.getData().getWorkout();
            userAndWorkout.setWorkout(newWorkout);

            user.setCurrentWorkout(resultStatus.getData().getUser().getCurrentWorkout());
            user.putWorkout(resultStatus.getData().getUser().getWorkout(newWorkout.getWorkoutId()));
        }
        return resultStatus;
    }

    public ResultStatus<UserAndWorkout> switchWorkout(@NonNull Workout oldWorkout, @NonNull String workoutId) {
        User user = userAndWorkoutProvider.provideUser();
        UserAndWorkout userAndWorkout = userAndWorkoutProvider.provideUserAndWorkout();

        ResultStatus<UserAndWorkout> resultStatus = this.workoutRepository.switchWorkout(oldWorkout, workoutId);
        if (resultStatus.isSuccess()) {
            userAndWorkout.setWorkout(resultStatus.getData().getWorkout());
            user.setCurrentWorkout(resultStatus.getData().getWorkout().getWorkoutId());
            user.putWorkout(resultStatus.getData().getUser().getWorkout(workoutId));
        }
        return resultStatus;
    }

    public ResultStatus<User> renameWorkout(@NonNull String workoutId, @NonNull String newWorkoutName) {
        User user = userAndWorkoutProvider.provideUser();
        Workout currentWorkout = userAndWorkoutProvider.provideCurrentWorkout();
        ResultStatus<User> resultStatus = this.workoutRepository.renameWorkout(workoutId, newWorkoutName);
        if (resultStatus.isSuccess()) {
            user.updateOwnedExercises(resultStatus.getData().getOwnedExercises());
            user.getWorkout(currentWorkout.getWorkoutId()).setWorkoutName(newWorkoutName);
            currentWorkout.setWorkoutName(newWorkoutName);
        }
        return resultStatus;
    }

    public ResultStatus<UserAndWorkout> deleteWorkoutThenFetchNext(String workoutId, String nextWorkoutId) {
        User user = userAndWorkoutProvider.provideUser();
        UserAndWorkout userAndWorkout = userAndWorkoutProvider.provideUserAndWorkout();
        ResultStatus<UserAndWorkout> resultStatus = this.workoutRepository.deleteWorkoutThenFetchNext(workoutId, nextWorkoutId);
        if (resultStatus.isSuccess()) {
            user.setCurrentWorkout(resultStatus.getData().getUser().getCurrentWorkout());
            user.updateUserWorkouts(resultStatus.getData().getUser().getWorkoutMetas());
            user.updateOwnedExercises(resultStatus.getData().getUser().getOwnedExercises());

            userAndWorkout.setWorkout(resultStatus.getData().getWorkout());
        }
        return resultStatus;
    }

    public ResultStatus<WorkoutMeta> resetWorkoutStatistics(@NonNull String workoutId) {
        User user = userAndWorkoutProvider.provideUser();
        ResultStatus<WorkoutMeta> resultStatus = this.workoutRepository.resetWorkoutStatistics(workoutId);
        if (resultStatus.isSuccess()) {
            user.putWorkout(resultStatus.getData());
        }
        return resultStatus;
    }

    public ResultStatus<UserAndWorkout> editWorkout(@NonNull String workoutId, @NonNull Workout workout) {
        User user = userAndWorkoutProvider.provideUser();
        UserAndWorkout userAndWorkout = userAndWorkoutProvider.provideUserAndWorkout();
        ResultStatus<UserAndWorkout> resultStatus = this.workoutRepository.editWorkout(workoutId, workout);
        if (resultStatus.isSuccess()) {
            user.updateOwnedExercises(resultStatus.getData().getUser().getOwnedExercises());
            userAndWorkout.setWorkout(resultStatus.getData().getWorkout());
        }
        return resultStatus;
    }

    public ResultStatus<UserAndWorkout> restartWorkout(@NonNull Workout workout) {
        User user = userAndWorkoutProvider.provideUser();
        Workout currentWorkout = userAndWorkoutProvider.provideCurrentWorkout();
        ResultStatus<UserAndWorkout> resultStatus = this.workoutRepository.restartWorkout(workout);
        if (resultStatus.isSuccess()) {
            // update the statistics for this workout
            user.putWorkout(resultStatus.getData().getUser().getWorkout(workout.getWorkoutId()));
            // in case any default weights were updated
            user.updateOwnedExercises(resultStatus.getData().getUser().getOwnedExercises());

            currentWorkout.setRoutine(resultStatus.getData().getWorkout().getRoutine());
            currentWorkout.setCurrentDay(0);
            currentWorkout.setCurrentWeek(0);
        }
        return resultStatus;
    }

    public ResultStatus<String> sendWorkout(String recipientUsername, String workoutId) {
        User user = userAndWorkoutProvider.provideUser();
        ResultStatus<String> resultStatus = this.workoutRepository.sendWorkout(recipientUsername, workoutId);
        if (resultStatus.isSuccess()) {
            user.setWorkoutsSent(user.getWorkoutsSent() + 1);
        }
        return resultStatus;
    }

    public ResultStatus<SharedWorkout> getReceivedWorkout(final String sharedWorkoutId) {
        return this.workoutRepository.getReceivedWorkout(sharedWorkoutId);
    }

    public ResultStatus<List<SharedWorkoutMeta>> getReceivedWorkouts(final int batchNumber) {
        User user = userAndWorkoutProvider.provideUser();

        ResultStatus<List<SharedWorkoutMeta>> resultStatus = this.workoutRepository.getReceivedWorkouts(batchNumber);
        if (resultStatus.isSuccess()) {
            for (SharedWorkoutMeta sharedWorkoutMeta : resultStatus.getData()) {
                user.putReceivedWorkout(sharedWorkoutMeta);
            }
        }
        return resultStatus;
    }

    public ResultStatus<AcceptWorkoutResponse> acceptReceivedWorkout(final String sharedWorkoutId, final String optionalName) {
        ResultStatus<AcceptWorkoutResponse> resultStatus = this.workoutRepository.acceptReceivedWorkout(sharedWorkoutId, optionalName);
        User user = userAndWorkoutProvider.provideUser();
        UserAndWorkout userAndWorkout = userAndWorkoutProvider.provideUserAndWorkout();

        if (resultStatus.isSuccess()) {
            AcceptWorkoutResponse response = resultStatus.getData();
            if (user.getCurrentWorkout() == null) {
                // this newly accepted workout is the only workout the user now owns, so make it the current one
                user.setCurrentWorkout(response.getWorkoutId());
                userAndWorkout.setWorkout(response.getWorkout());
            }

            user.putWorkout(response.getWorkoutMeta());
            user.setTotalReceivedWorkouts(user.getTotalReceivedWorkouts() - 1);
            user.addNewExercises(response.getExercises());
            if (!user.getReceivedWorkout(sharedWorkoutId).isSeen()) {
                // this workout was not seen, so make sure to decrease the unseen count since it is no longer in the list
                user.setUnseenReceivedWorkouts(user.getUnseenReceivedWorkouts() - 1);
            }
            user.removeReceivedWorkout(sharedWorkoutId);
        }
        return resultStatus;
    }

    public ResultStatus<String> declineReceivedWorkout(String sharedWorkoutId) {
        ResultStatus<String> resultStatus = this.workoutRepository.declineReceivedWorkout(sharedWorkoutId);

        User user = userAndWorkoutProvider.provideUser();
        if (resultStatus.isSuccess()) {
            SharedWorkoutMeta sharedWorkoutMeta = user.getReceivedWorkout(sharedWorkoutId);
            user.getReceivedWorkouts().remove(sharedWorkoutMeta.getWorkoutId());
            if (!sharedWorkoutMeta.isSeen()) {
                // if it was unread, then we need to make sure to decrease unseen count
                user.setUnseenReceivedWorkouts(user.getUnseenReceivedWorkouts() - 1);
            }
            user.setTotalReceivedWorkouts(user.getTotalReceivedWorkouts() - 1);
        }
        return resultStatus;
    }
}
