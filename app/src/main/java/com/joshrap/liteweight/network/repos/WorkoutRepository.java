package com.joshrap.liteweight.network.repos;

import com.joshrap.liteweight.helpers.JsonParser;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.Routine;
import com.joshrap.liteweight.models.User;
import com.joshrap.liteweight.models.UserWithWorkout;
import com.joshrap.liteweight.models.Workout;
import com.joshrap.liteweight.network.ApiGateway;
import com.joshrap.liteweight.network.RequestFields;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import lombok.NonNull;

public class WorkoutRepository {

    private static final String newWorkoutAction = "newWorkout";
    private static final String switchWorkoutAction = "switchWorkout";
    private static final String copyWorkoutAction = "copyWorkout";
    private static final String renameWorkoutAction = "renameWorkout";
    private static final String deleteWorkoutThenFetchAction = "deleteWorkoutThenFetch";
    private static final String resetWorkoutStatisticsAction = "resetWorkoutStatistics";
    private static final String editWorkoutAction = "editWorkout";
    private static final String syncWorkoutAction = "syncWorkout";
    private static final String restartWorkoutAction = "restartWorkout";

    private ApiGateway apiGateway;

    @Inject
    public WorkoutRepository(ApiGateway apiGateway) {
        this.apiGateway = apiGateway;
    }

    public ResultStatus<UserWithWorkout> createWorkout(@NonNull Routine routine, @NonNull String workoutName) {
        ResultStatus<UserWithWorkout> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(Workout.ROUTINE, routine.asMap());
        requestBody.put(Workout.WORKOUT_NAME, workoutName);

        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(newWorkoutAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            try {
                resultStatus.setData(new UserWithWorkout(JsonParser.deserialize(apiResponse.getData())));
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("Unable to create workout. 2");
            }
        } else if (apiResponse.isNetworkError()) {
            resultStatus.setErrorMessage("Network error. Unable to create workout. Check internet connection.");
        } else {
            resultStatus.setErrorMessage("Unable to create workout. 3");
        }
        return resultStatus;
    }

    public ResultStatus<UserWithWorkout> copyWorkout(@NonNull Workout workout, @NonNull String workoutName) {
        ResultStatus<UserWithWorkout> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(RequestFields.WORKOUT, workout.asMap());
        requestBody.put(Workout.WORKOUT_NAME, workoutName);

        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(copyWorkoutAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            try {
                resultStatus.setData(new UserWithWorkout(JsonParser.deserialize(apiResponse.getData())));
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("Unable to copy workout. 2");
            }
        } else if (apiResponse.isNetworkError()) {
            resultStatus.setErrorMessage("Network error. Unable to copy workout. Check internet connection.");
        } else {
            resultStatus.setErrorMessage("Unable to copy workout. 3");
        }
        return resultStatus;
    }

    public ResultStatus<UserWithWorkout> switchWorkout(@NonNull Workout oldWorkout, @NonNull String workoutId) {
        ResultStatus<UserWithWorkout> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(RequestFields.WORKOUT, oldWorkout.asMap());
        requestBody.put(Workout.WORKOUT_ID, workoutId);

        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(switchWorkoutAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            try {
                resultStatus.setData(new UserWithWorkout(JsonParser.deserialize(apiResponse.getData())));
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("Unable to switch workout. 2");
            }
        } else if (apiResponse.isNetworkError()) {
            resultStatus.setErrorMessage("Network error. Unable to switch workout. Check internet connection.");
        } else {
            resultStatus.setErrorMessage("Unable to switch workout. 3");
        }
        return resultStatus;
    }

    public ResultStatus<User> renameWorkout(@NonNull String workoutId, @NonNull String workoutName) {
        ResultStatus<User> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(Workout.WORKOUT_ID, workoutId);
        requestBody.put(Workout.WORKOUT_NAME, workoutName);

        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(renameWorkoutAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            try {
                resultStatus.setData(new User(JsonParser.deserialize(apiResponse.getData())));
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("Unable to copy workout. 2");
            }
        } else if (apiResponse.isNetworkError()) {
            resultStatus.setErrorMessage("Network error. Unable to copy workout. Check internet connection.");
        } else {
            resultStatus.setErrorMessage("Unable to copy workout. 3");
        }
        return resultStatus;
    }

    public ResultStatus<UserWithWorkout> popWorkout(String workoutId, String nextWorkoutId) {
        ResultStatus<UserWithWorkout> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(Workout.WORKOUT_ID, workoutId);
        requestBody.put(RequestFields.NEXT_WORKOUT_ID, nextWorkoutId);

        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(deleteWorkoutThenFetchAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            try {
                resultStatus.setData(new UserWithWorkout(JsonParser.deserialize(apiResponse.getData())));
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("Unable to delete workout. 2");
            }
        } else if (apiResponse.isNetworkError()) {
            resultStatus.setErrorMessage("Network error. Unable to delete workout. Check internet connection.");
        } else {
            resultStatus.setErrorMessage("Unable to delete workout. 3");
        }
        return resultStatus;
    }

    public ResultStatus<User> resetWorkoutStatistics(@NonNull String workoutId) {
        ResultStatus<User> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(Workout.WORKOUT_ID, workoutId);

        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(resetWorkoutStatisticsAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            try {
                resultStatus.setData(new User(JsonParser.deserialize(apiResponse.getData())));
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("Unable to copy workout. 2");
            }
        } else if (apiResponse.isNetworkError()) {
            resultStatus.setErrorMessage("Network error. Unable to copy workout. Check internet connection.");
        } else {
            resultStatus.setErrorMessage("Unable to copy workout. 3");
        }
        return resultStatus;
    }

    public ResultStatus<UserWithWorkout> editWorkout(@NonNull String workoutId, @NonNull Workout workout) {
        ResultStatus<UserWithWorkout> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(Workout.WORKOUT_ID, workoutId);
        requestBody.put(RequestFields.WORKOUT, workout.asMap());

        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(editWorkoutAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            try {
                resultStatus.setData(new UserWithWorkout(JsonParser.deserialize(apiResponse.getData())));
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("Unable to edit workout. 2");
            }
        } else if (apiResponse.isNetworkError()) {
            resultStatus.setErrorMessage("Network error. Unable to edit workout. Check internet connection.");
        } else {
            resultStatus.setErrorMessage("Unable to edit workout. 3");
        }
        return resultStatus;
    }

    public ResultStatus<String> syncWorkout(@NonNull Workout workout) {
        ResultStatus<String> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(RequestFields.WORKOUT, workout.asMap());

        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(syncWorkoutAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            try {
                resultStatus.setData(apiResponse.getData());
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("Unable to sync workout. 2");
            }
        } else if (apiResponse.isNetworkError()) {
            resultStatus.setErrorMessage("Network error. Unable to sync workout. Check internet connection.");
        } else {
            resultStatus.setErrorMessage("Unable to sync workout. 3");
        }
        return resultStatus;
    }

    public ResultStatus<UserWithWorkout> restartWorkout(@NonNull Workout workout) {
        ResultStatus<UserWithWorkout> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(RequestFields.WORKOUT, workout.asMap());

        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(restartWorkoutAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            try {
                resultStatus.setData(new UserWithWorkout(JsonParser.deserialize(apiResponse.getData())));
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("Unable to restart workout. 2");
            }
        } else if (apiResponse.isNetworkError()) {
            resultStatus.setErrorMessage("Network error. Unable to restart workout. Check internet connection.");
        } else {
            resultStatus.setErrorMessage("Unable to restart workout. 3");
        }
        return resultStatus;
    }
}
