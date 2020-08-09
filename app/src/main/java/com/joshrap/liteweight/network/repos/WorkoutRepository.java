package com.joshrap.liteweight.network.repos;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joshrap.liteweight.models.ApiResponse;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.Routine;
import com.joshrap.liteweight.models.UserWithWorkout;
import com.joshrap.liteweight.models.Workout;
import com.joshrap.liteweight.network.ApiGateway;

import java.util.HashMap;
import java.util.Map;

import lombok.NonNull;

public class WorkoutRepository {

    private static final String newWorkoutAction = "newWorkout";
    // TODO handle if user deletes day that the curentDay is currently on

    public static ResultStatus<UserWithWorkout> createWorkout(@NonNull Routine routine, @NonNull String workoutName) {
        ResultStatus<UserWithWorkout> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(Workout.ROUTINE, routine.asMap());
        requestBody.put(Workout.WORKOUT_NAME, workoutName);

        ResultStatus<Map<String, Object>> apiResponse = ApiGateway.makeRequest(newWorkoutAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            try {
                ApiResponse apiResponseBody = new ApiResponse(apiResponse.getData());
                if (apiResponseBody.isSuccess()) {
                    resultStatus.setData(new UserWithWorkout(new ObjectMapper().readValue(apiResponseBody.getJsonString(), Map.class)));
                    resultStatus.setSuccess(true);
                } else {
                    resultStatus.setErrorMessage("Unable to create workout. 1" + apiResponseBody.getJsonString());
                }
            } catch (Exception e) {
                resultStatus.setErrorMessage("Unable to create workout. 2");
            }
        } else if (apiResponse.isNetworkError()) {
            resultStatus.setErrorMessage("Network error. Unable to create workout. Check internet connection.");
        } else {
            resultStatus.setErrorMessage("Unable to create workout.. 3");
        }
        return resultStatus;
    }
}
