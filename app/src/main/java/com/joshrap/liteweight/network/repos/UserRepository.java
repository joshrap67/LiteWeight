package com.joshrap.liteweight.network.repos;

import com.joshrap.liteweight.utils.JsonUtils;
import com.joshrap.liteweight.models.OwnedExercise;
import com.joshrap.liteweight.models.Friend;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.User;
import com.joshrap.liteweight.models.UserPreferences;
import com.joshrap.liteweight.models.UserWithWorkout;
import com.joshrap.liteweight.network.ApiGateway;
import com.joshrap.liteweight.network.RequestFields;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class UserRepository {

    private static final String getUserWorkoutAction = "getUserWorkout";
    private static final String updateExerciseAction = "updateExercise";
    private static final String newExerciseAction = "newExercise";
    private static final String deleteExerciseAction = "deleteExercise";
    private static final String updateProfilePictureAction = "updateIcon";
    private static final String updateEndpointIdAction = "updateEndpointId";
    private static final String removeEndpointIdAction = "removeEndpointId";
    private static final String sendFriendRequestAction = "sendFriendRequest";
    private static final String cancelFriendRequestAction = "cancelFriendRequest";
    private static final String setAllRequestsSeenAction = "setAllRequestsSeen";
    private static final String updateUserPreferencesAction = "updateUserPreferences";
    private static final String acceptFriendRequestAction = "acceptFriendRequest";
    private static final String removeFriendAction = "removeFriend";
    private static final String declineFriendRequestAction = "declineFriendRequest";
    private static final String blockUserAction = "blockUser";
    private static final String unblockUserAction = "unblockUser";

    private final ApiGateway apiGateway;

    @Inject
    public UserRepository(ApiGateway apiGateway) {
        this.apiGateway = apiGateway;
    }

    public ResultStatus<UserWithWorkout> getUserAndCurrentWorkout() {
        ResultStatus<UserWithWorkout> resultStatus = new ResultStatus<>();

        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(getUserWorkoutAction, new HashMap<>(), true);

        if (apiResponse.isSuccess()) {
            try {
                resultStatus.setData(new UserWithWorkout(JsonUtils.deserialize(apiResponse.getData())));
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("Unable to parse user data and workout.");
            }
        } else {
            resultStatus.setErrorMessage("Unable to load user data and workout.");
        }
        return resultStatus;
    }

    public ResultStatus<User> updateExercise(String exerciseId, OwnedExercise ownedExercise) {
        ResultStatus<User> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        if (ownedExercise != null) {
            requestBody.put(RequestFields.EXERCISE, ownedExercise);
        }
        requestBody.put(RequestFields.EXERCISE_ID, exerciseId);

        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(updateExerciseAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            try {
                resultStatus.setData(new User(JsonUtils.deserialize(apiResponse.getData())));
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("Unable to parse user data.");
            }
        } else {
            resultStatus.setErrorMessage("Unable to update exercise.");
        }
        return resultStatus;
    }

    public ResultStatus<OwnedExercise> newExercise(String exerciseName, List<String> focuses) {
        ResultStatus<OwnedExercise> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        if (exerciseName != null) {
            requestBody.put(OwnedExercise.EXERCISE_NAME, exerciseName);
        }
        requestBody.put(OwnedExercise.FOCUSES, focuses);

        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(newExerciseAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            try {
                resultStatus.setData(new OwnedExercise(JsonUtils.deserialize(apiResponse.getData())));
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("Unable to parse user data.");
            }
        } else {
            resultStatus.setErrorMessage("Unable to update exercise.");
        }
        return resultStatus;
    }

    public ResultStatus<String> deleteExercise(String exerciseId) {
        ResultStatus<String> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        if (exerciseId != null) {
            requestBody.put(RequestFields.EXERCISE_ID, exerciseId);
        }
        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(deleteExerciseAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            resultStatus.setData(apiResponse.getData());
            resultStatus.setSuccess(true);
        } else {
            resultStatus.setErrorMessage("Unable to update exercise.");
        }
        return resultStatus;
    }

    public ResultStatus<String> updateProfilePicture(String pictureData) {
        ResultStatus<String> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        if (pictureData != null) {
            requestBody.put(User.ICON, pictureData);
        }
        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(updateProfilePictureAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            resultStatus.setData(apiResponse.getData());
            resultStatus.setSuccess(true);
        } else {
            resultStatus.setErrorMessage("Unable to update icon.");
        }
        return resultStatus;
    }

    public ResultStatus<String> updateEndpointId(String tokenId) {
        ResultStatus<String> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        if (tokenId != null) {
            requestBody.put(RequestFields.PUSH_ENDPOINT_ARN, tokenId);
        }
        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(updateEndpointIdAction, requestBody, true);
        if (apiResponse.isSuccess()) {
            resultStatus.setData(apiResponse.getData());
            resultStatus.setSuccess(true);
        } else {
            resultStatus.setErrorMessage("Unable to update endpoint id.");
        }
        return resultStatus;
    }

    public ResultStatus<String> removeEndpointId() {
        ResultStatus<String> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(removeEndpointIdAction, requestBody, true);
        if (apiResponse.isSuccess()) {
            resultStatus.setData(apiResponse.getData());
            resultStatus.setSuccess(true);
        } else {
            resultStatus.setErrorMessage("Unable to remove endpoint id.");
        }
        return resultStatus;
    }

    public ResultStatus<Friend> sendFriendRequest(String username) {
        ResultStatus<Friend> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        if (username != null) {
            requestBody.put(User.USERNAME, username);
        }
        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(sendFriendRequestAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            try {
                resultStatus.setData(new Friend(JsonUtils.deserialize(apiResponse.getData())));
                resultStatus.setSuccess(true);
            } catch (IOException ioe) {
                resultStatus.setErrorMessage("Could not parse friend.");
            }
        } else {
            // todo probably want to actually use backend messages here...
            resultStatus.setErrorMessage("Unable to send friend request.");
        }
        return resultStatus;
    }

    public ResultStatus<String> cancelFriendRequest(String username) {
        ResultStatus<String> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        if (username != null) {
            requestBody.put(User.USERNAME, username);
        }
        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(cancelFriendRequestAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            resultStatus.setData(apiResponse.getData());
            resultStatus.setSuccess(true);
        } else {
            resultStatus.setErrorMessage("Unable to cancel friend request.");
        }
        return resultStatus;
    }

    public ResultStatus<String> setAllRequestsSeen() {
        ResultStatus<String> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(setAllRequestsSeenAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            resultStatus.setData(apiResponse.getData());
            resultStatus.setSuccess(true);
        } else {
            resultStatus.setErrorMessage("Unable to set all requests seen.");
        }
        return resultStatus;
    }

    public ResultStatus<String> updateUserPreferences(UserPreferences userPreferences) {
        ResultStatus<String> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        if (userPreferences != null) {
            requestBody.put(User.USER_PREFERENCES, userPreferences.asMap());
        }
        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(updateUserPreferencesAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            resultStatus.setData(apiResponse.getData());
            resultStatus.setSuccess(true);
        } else {
            resultStatus.setErrorMessage("Unable to set update preferences.");
        }
        return resultStatus;
    }

    public ResultStatus<String> acceptFriendRequest(String username) {
        ResultStatus<String> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        if (username != null) {
            requestBody.put(User.USERNAME, username);
        }
        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(acceptFriendRequestAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            resultStatus.setData(apiResponse.getData());
            resultStatus.setSuccess(true);
        } else {
            // todo probably want to actually use backend messages here...
            resultStatus.setErrorMessage("Unable to accept friend request.");
        }
        return resultStatus;
    }

    public ResultStatus<String> removeFriend(String username) {
        ResultStatus<String> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        if (username != null) {
            requestBody.put(User.USERNAME, username);
        }
        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(removeFriendAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            resultStatus.setData(apiResponse.getData());
            resultStatus.setSuccess(true);
        } else {
            resultStatus.setErrorMessage("Unable to remove friend.");
        }
        return resultStatus;
    }

    public ResultStatus<String> declineFriendRequest(String username) {
        ResultStatus<String> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        if (username != null) {
            requestBody.put(User.USERNAME, username);
        }
        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(declineFriendRequestAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            resultStatus.setData(apiResponse.getData());
            resultStatus.setSuccess(true);
        } else {
            resultStatus.setErrorMessage("Unable to decline friend request.");
        }
        return resultStatus;
    }

    public ResultStatus<String> blockUser(String username) {
        ResultStatus<String> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        if (username != null) {
            requestBody.put(User.USERNAME, username);
        }
        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(blockUserAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            try {
                resultStatus.setData(JsonUtils.deserialize(apiResponse.getData()).get(User.ICON).toString());
            } catch (IOException e) {
                resultStatus.setErrorMessage("Could not parse blocked user icon.");
            }
            resultStatus.setSuccess(true);
        } else {
            resultStatus.setErrorMessage("Unable to block user.");
        }
        return resultStatus;
    }

    public ResultStatus<String> unblockUser(String username) {
        ResultStatus<String> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        if (username != null) {
            requestBody.put(User.USERNAME, username);
        }
        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(unblockUserAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            resultStatus.setData(apiResponse.getData());
            resultStatus.setSuccess(true);
        } else {
            resultStatus.setErrorMessage("Unable to unblock user.");
        }
        return resultStatus;
    }
}
