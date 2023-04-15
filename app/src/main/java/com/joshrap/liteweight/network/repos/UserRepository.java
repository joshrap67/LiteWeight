package com.joshrap.liteweight.network.repos;

import com.joshrap.liteweight.models.SharedWorkout;
import com.joshrap.liteweight.utils.JsonUtils;
import com.joshrap.liteweight.models.OwnedExercise;
import com.joshrap.liteweight.models.Friend;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.User;
import com.joshrap.liteweight.models.UserPreferences;
import com.joshrap.liteweight.models.CurrentUserAndWorkout;
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
    private static final String setAllFriendRequestsSeenAction = "setAllFriendRequestsSeen";
    private static final String updateUserPreferencesAction = "updateUserPreferences";
    private static final String acceptFriendRequestAction = "acceptFriendRequest";
    private static final String removeFriendAction = "removeFriend";
    private static final String declineFriendRequestAction = "declineFriendRequest";
    private static final String blockUserAction = "blockUser";
    private static final String unblockUserAction = "unblockUser";
    private static final String sendFeedbackAction = "sendFeedback";
    private static final String setAllReceivedWorkoutsSeenAction = "setAllReceivedWorkoutsSeen";
    private static final String setReceivedWorkoutSeenAction = "setReceivedWorkoutSeen";


    private final ApiGateway apiGateway;

    @Inject
    public UserRepository(ApiGateway apiGateway) {
        this.apiGateway = apiGateway;
    }

    public ResultStatus<CurrentUserAndWorkout> getUserAndCurrentWorkout() {
        ResultStatus<CurrentUserAndWorkout> resultStatus = new ResultStatus<>();

        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(getUserWorkoutAction, new HashMap<>(), true);

        if (apiResponse.isSuccess()) {
            try {
                resultStatus.setData(new CurrentUserAndWorkout(JsonUtils.deserialize(apiResponse.getData())));
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("There was a problem loading your data.");
            }
        } else if (apiResponse.isOutDatedVersion()) {
            resultStatus.setErrorMessage("You are using an outdated version of LiteWeight. Please upgrade your application to continue.");
        } else {
            resultStatus.setErrorMessage("There was a problem loading your data.");
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
                resultStatus.setErrorMessage("There was a problem updating the exercise.");
            }
        } else {
            resultStatus.setErrorMessage("There was a problem updating the exercise.");
        }
        return resultStatus;
    }

    public ResultStatus<OwnedExercise> newExercise(String exerciseName, List<String> focuses, double weight, int sets, int reps, String details, String videoURL) {
        ResultStatus<OwnedExercise> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(OwnedExercise.EXERCISE_NAME, exerciseName);
        requestBody.put(OwnedExercise.FOCUSES, focuses);
        requestBody.put(OwnedExercise.DEFAULT_WEIGHT, weight);
        requestBody.put(OwnedExercise.DEFAULT_SETS, sets);
        requestBody.put(OwnedExercise.DEFAULT_REPS, reps);
        requestBody.put(OwnedExercise.DEFAULT_DETAILS, details);
        requestBody.put(OwnedExercise.VIDEO_URL, videoURL);

        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(newExerciseAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            try {
                resultStatus.setData(new OwnedExercise(JsonUtils.deserialize(apiResponse.getData())));
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("There was a problem creating the exercise.");
            }
        } else {
            resultStatus.setErrorMessage("There was a problem creating the exercise.");
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
            resultStatus.setErrorMessage("There was a problem deleting the exercise.");
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
            resultStatus.setErrorMessage("There was a problem updating the profile picture.");
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
                resultStatus.setErrorMessage("There was a problem sending the friend request.");
            }
        } else {
            resultStatus.setErrorMessage(apiResponse.getErrorMessage());
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
            resultStatus.setErrorMessage("There was a problem canceling the friend request.");
        }
        return resultStatus;
    }

    public ResultStatus<String> setAllRequestsSeen() {
        ResultStatus<String> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(setAllFriendRequestsSeenAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            resultStatus.setData(apiResponse.getData());
            resultStatus.setSuccess(true);
        } else {
            resultStatus.setErrorMessage("There was a problem setting the friend requests as seen.");
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
            resultStatus.setErrorMessage("There was a problem updating the user preferences.");
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
            resultStatus.setErrorMessage(apiResponse.getErrorMessage());
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
            resultStatus.setErrorMessage("There was a problem removing the friend.");
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
            resultStatus.setErrorMessage("There was a problem declining the friend request.");
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
                resultStatus.setErrorMessage("There was a problem loading the blocked user's icon.");
            }
            resultStatus.setSuccess(true);
        } else {
            resultStatus.setErrorMessage("There was a blocking the user.");
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
            resultStatus.setErrorMessage("There was a problem unblocking the user.");
        }
        return resultStatus;
    }

    public ResultStatus<String> sendFeedback(String feedback, String feedbackTime) {
        ResultStatus<String> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(RequestFields.FEEDBACK, feedback);
        requestBody.put(RequestFields.FEEDBACK_TIME, feedbackTime);
        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(sendFeedbackAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            resultStatus.setData(apiResponse.getData());
            resultStatus.setSuccess(true);
        } else {
            resultStatus.setErrorMessage("There was a problem sending your feedback. Please try again later.");
        }
        return resultStatus;
    }

    public ResultStatus<String> setAllReceivedWorkoutsSeen() {
        ResultStatus<String> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(setAllReceivedWorkoutsSeenAction, requestBody, true);
        if (apiResponse.isSuccess()) {
            resultStatus.setData(apiResponse.getData());
            resultStatus.setSuccess(true);
        } else {
            resultStatus.setErrorMessage("There was a problem trying to set workouts as seen.");
        }
        return resultStatus;
    }

    public ResultStatus<String> setReceivedWorkoutSeen(String sharedWorkoutId) {
        ResultStatus<String> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(SharedWorkout.SHARED_WORKOUT_ID, sharedWorkoutId);

        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(setReceivedWorkoutSeenAction, requestBody, true);
        if (apiResponse.isSuccess()) {
            resultStatus.setData(apiResponse.getData());
            resultStatus.setSuccess(true);
        } else {
            resultStatus.setErrorMessage("There was a problem trying to set workout as seen.");
        }
        return resultStatus;
    }
}
