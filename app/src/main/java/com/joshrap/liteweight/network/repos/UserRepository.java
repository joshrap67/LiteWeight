package com.joshrap.liteweight.network.repos;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joshrap.liteweight.models.ApiResponse;
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

        ResultStatus<Map<String, Object>> apiResponse = ApiGateway.makeRequest(getUserAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            try {
                ApiResponse apiResponseBody = new ApiResponse(apiResponse.getData());
                if (apiResponseBody.isSuccess()) {
                    resultStatus.setData(new User(new ObjectMapper().readValue(apiResponseBody.getJsonString(), Map.class)));
                    resultStatus.setSuccess(true);
                } else {
                    resultStatus.setErrorMessage("Unable to load user data. 1");
                }
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

    public static ResultStatus<UserWithWorkout> getUserAndCurrentWorkout() {
        ResultStatus<UserWithWorkout> resultStatus = new ResultStatus<>();

        ResultStatus<Map<String, Object>> apiResponse = ApiGateway.makeRequest(getUserWorkoutAction, new HashMap<>(), true);

        if (apiResponse.isSuccess()) {
            try {
                ApiResponse apiResponseBody = new ApiResponse(apiResponse.getData());
                if (apiResponseBody.isSuccess()) {
                    resultStatus.setData(new UserWithWorkout(new ObjectMapper().readValue(apiResponseBody.getJsonString(), Map.class)));
                    resultStatus.setSuccess(true);
                } else {
                    resultStatus.setErrorMessage("Unable to load user data and workout. 1");
                }
            } catch (Exception e) {
                resultStatus.setErrorMessage("Unable to load user data and workout. 2");
            }
        } else if (apiResponse.isNetworkError()) {
            resultStatus.setErrorMessage("Network error. Unable to load user data. Check internet connection.");
        } else {
            resultStatus.setErrorMessage("Unable to load user data and workout. 3");
        }
        return resultStatus;
    }

    public static ResultStatus<User> newUser(String username) {
        ResultStatus<User> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        if (username != null) {
            requestBody.put(User.USERNAME, username);
        }

        ResultStatus<Map<String, Object>> apiResponse = ApiGateway.makeRequest(newUserAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            try {
                ApiResponse apiResponseBody = new ApiResponse(apiResponse.getData());
                if (apiResponseBody.isSuccess()) {
                    resultStatus.setData(new User(new ObjectMapper().readValue(apiResponseBody.getJsonString(), Map.class)));
                    resultStatus.setSuccess(true);
                } else {
                    resultStatus.setErrorMessage("Unable to load user data. 1");
                }
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
