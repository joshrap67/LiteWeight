package com.joshrap.liteweight.network.repos;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joshrap.liteweight.models.ApiResponse;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.User;
import com.joshrap.liteweight.network.ApiGateway;

import java.util.HashMap;
import java.util.Map;

public class UserRepository {

    public static final String getUserAction = "getUserData";

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
}
