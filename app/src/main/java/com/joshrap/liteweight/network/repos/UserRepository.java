package com.joshrap.liteweight.network.repos;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joshrap.liteweight.models.ExerciseUser;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.User;
import com.joshrap.liteweight.models.UserWithWorkout;
import com.joshrap.liteweight.network.ApiGateway;
import com.joshrap.liteweight.network.RequestFields;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class UserRepository {

    private static final String newUserAction = "newUser";
    private static final String getUserWorkoutAction = "getUserWorkout";
    private static final String updateExerciseAction = "updateExercise";
    private static final String newExerciseAction = "newExercise";

    private ApiGateway apiGateway;

    @Inject
    public UserRepository(ApiGateway apiGateway){
        this.apiGateway = apiGateway;
    }

    public ResultStatus<UserWithWorkout> getUserAndCurrentWorkout() {
        ResultStatus<UserWithWorkout> resultStatus = new ResultStatus<>();

        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(getUserWorkoutAction, new HashMap<>(), true);

        if (apiResponse.isSuccess()) {
            try {
                resultStatus.setData(new UserWithWorkout(new ObjectMapper().readValue(apiResponse.getData(), Map.class)));
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("Unable to parse user data and workout.");
            }
        } else if (apiResponse.isNetworkError()) {
            resultStatus.setErrorMessage("Network error. Unable to load user data. Check internet connection.");
        } else {
            resultStatus.setErrorMessage("Unable to load user data and workout.");
        }
        return resultStatus;
    }

    public ResultStatus<User> newUser(String username) {
        ResultStatus<User> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        if (username != null) {
            requestBody.put(User.USERNAME, username);
        }

        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(newUserAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            try {
                resultStatus.setData(new User(new ObjectMapper().readValue(apiResponse.getData(), Map.class)));
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("Unable to load user data. 2");
            }
        } else if (apiResponse.isNetworkError()) {
            resultStatus.setErrorMessage("Network error. Unable to load user data. Check internet connection.");
        } else {
            resultStatus.setErrorMessage("Unable to load user data. 3");
        }
        return resultStatus;
    }

    public ResultStatus<User> updateExercise(String exerciseId, ExerciseUser exerciseUser) {
        ResultStatus<User> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        if (exerciseUser != null) {
            requestBody.put(RequestFields.EXERCISE, exerciseUser);
        }
        requestBody.put(RequestFields.EXERCISE_ID, exerciseId);

        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(updateExerciseAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            try {
                resultStatus.setData(new User(new ObjectMapper().readValue(apiResponse.getData(), Map.class)));
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("Unable to parse user data.");
            }
        } else if (apiResponse.isNetworkError()) {
            resultStatus.setErrorMessage("Network error. Unable to update exercise. Check internet connection.");
        } else {
            resultStatus.setErrorMessage("Unable to update exercise.");
        }
        return resultStatus;
    }

    public ResultStatus<ExerciseUser> newExercise(String exerciseName, List<String> focuses) {
        ResultStatus<ExerciseUser> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        if (exerciseName != null) {
            requestBody.put(ExerciseUser.EXERCISE_NAME, exerciseName);
        }
        requestBody.put(ExerciseUser.FOCUSES, focuses);

        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(newExerciseAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            try {
                resultStatus.setData(new ExerciseUser(new ObjectMapper().readValue(apiResponse.getData(), Map.class)));
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("Unable to parse user data.");
            }
        } else if (apiResponse.isNetworkError()) {
            resultStatus.setErrorMessage("Network error. Unable to update exercise. Check internet connection.");
        } else {
            resultStatus.setErrorMessage("Unable to update exercise.");
        }
        return resultStatus;
    }
}
