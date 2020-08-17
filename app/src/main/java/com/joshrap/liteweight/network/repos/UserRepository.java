package com.joshrap.liteweight.network.repos;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.User;
import com.joshrap.liteweight.models.UserWithWorkout;
import com.joshrap.liteweight.network.ApiGateway;

import java.util.HashMap;
import java.util.Map;

public class UserRepository {

    private static final String getUserAction = "getUserData";
    private static final String newUserAction = "newUser";
    private static final String getUserWorkoutAction = "getUserWorkout";

    public static ResultStatus<User> getUser(String username) {
        ResultStatus<User> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        if (username != null) {
            requestBody.put(User.USERNAME, username);
        }

        ResultStatus<String> apiResponse = ApiGateway.makeRequest(getUserAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            try {
                resultStatus.setData(new User(new ObjectMapper().readValue(apiResponse.getData(), Map.class)));
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("Unable to parse user data.");
            }
        } else if (apiResponse.isNetworkError()) {
            resultStatus.setErrorMessage("Network error. Unable to load user data. Check internet connection.");
        } else {
            resultStatus.setErrorMessage("Unable to load user data." + apiResponse.getErrorMessage());
        }
        return resultStatus;
    }

    public static ResultStatus<UserWithWorkout> getUserAndCurrentWorkout() {
        ResultStatus<UserWithWorkout> resultStatus = new ResultStatus<>();

        ResultStatus<String> apiResponse = ApiGateway.makeRequest(getUserWorkoutAction, new HashMap<>(), true);

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

    public static ResultStatus<User> newUser(String username) {
        ResultStatus<User> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        if (username != null) {
            requestBody.put(User.USERNAME, username);
        }

        ResultStatus<String> apiResponse = ApiGateway.makeRequest(newUserAction, requestBody, true);

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
}
