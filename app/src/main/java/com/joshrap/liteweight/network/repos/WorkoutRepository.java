package com.joshrap.liteweight.network.repos;

import com.joshrap.liteweight.helpers.JsonParser;
import com.joshrap.liteweight.models.AcceptWorkoutResponse;
import com.joshrap.liteweight.models.ReceivedWorkoutMeta;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.Routine;
import com.joshrap.liteweight.models.SentWorkout;
import com.joshrap.liteweight.models.User;
import com.joshrap.liteweight.models.UserWithWorkout;
import com.joshrap.liteweight.models.Workout;
import com.joshrap.liteweight.network.ApiGateway;
import com.joshrap.liteweight.network.RequestFields;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private static final String getReceivedWorkoutsAction = "getReceivedWorkouts";
    private static final String sendWorkoutAction = "sendWorkout";
    private static final String getReceivedWorkoutAction = "getSentWorkout";
    private static final String setReceivedWorkoutSeenAction = "setReceivedWorkoutSeen";
    private static final String setAllReceivedWorkoutsSeenAction = "setAllReceivedWorkoutsSeen";
    private static final String acceptReceivedWorkoutAction = "acceptReceivedWorkout";
    private static final String declineReceivedWorkoutAction = "declineReceivedWorkout";

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

    public ResultStatus<String> sendWorkout(String recipientUsername, String workoutId) {
        ResultStatus<String> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(User.USERNAME, recipientUsername);
        requestBody.put(Workout.WORKOUT_ID, workoutId);

        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(sendWorkoutAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            try {
                resultStatus.setData((String) JsonParser.deserialize(apiResponse.getData()).get(SentWorkout.SENT_WORKOUT_ID));
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("Unable to send workout. 2");
            }
        } else if (apiResponse.isNetworkError()) {
            resultStatus.setErrorMessage("Network error. Unable to send workout. Check internet connection.");
        } else {
            resultStatus.setErrorMessage("Unable to send workout. 3");
        }
        return resultStatus;
    }

    public ResultStatus<List<ReceivedWorkoutMeta>> getReceivedWorkouts(final int batchNumber) {
        ResultStatus<List<ReceivedWorkoutMeta>> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(RequestFields.BATCH_NUMBER, batchNumber);

        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(getReceivedWorkoutsAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            try {
                Map<String, Object> receivedWorkoutsResponseRaw = JsonParser.deserialize(apiResponse.getData());
                List<ReceivedWorkoutMeta> receivedWorkoutMetas = new ArrayList<>();
                for (String workoutId : receivedWorkoutsResponseRaw.keySet()) {
                    receivedWorkoutMetas.add(new ReceivedWorkoutMeta((Map<String, Object>) receivedWorkoutsResponseRaw.get(workoutId)));
                }
                resultStatus.setData(receivedWorkoutMetas);
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("Unable to receive workouts. 2");
            }
        } else if (apiResponse.isNetworkError()) {
            resultStatus.setErrorMessage("Network error. Unable to receive workouts. Check internet connection.");
        } else {
            resultStatus.setErrorMessage("Unable to receive workouts. 3");
        }
        return resultStatus;
    }

    public ResultStatus<SentWorkout> getReceivedWorkout(final String sentWorkoutId) {
        ResultStatus<SentWorkout> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(SentWorkout.SENT_WORKOUT_ID, sentWorkoutId);

        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(getReceivedWorkoutAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            try {
                resultStatus.setData(new SentWorkout(JsonParser.deserialize(apiResponse.getData())));
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("Unable to receive workout. 2");
            }
        } else if (apiResponse.isNetworkError()) {
            resultStatus.setErrorMessage("Network error. Unable to receive workouts. Check internet connection.");
        } else {
            resultStatus.setErrorMessage("Unable to receive workout. 3");
        }
        return resultStatus;
    }

    public void setReceivedWorkoutSeen(String sentWorkoutId) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(SentWorkout.SENT_WORKOUT_ID, sentWorkoutId);
        // todo maybe this shouldn't be a blind send?
        this.apiGateway.makeRequest(setReceivedWorkoutSeenAction, requestBody, true);
    }

    public void setAllReceivedWorkoutsSeen() {
        Map<String, Object> requestBody = new HashMap<>();
        // todo maybe this shouldn't be a blind send?
        this.apiGateway.makeRequest(setAllReceivedWorkoutsSeenAction, requestBody, true);
    }

    public ResultStatus<AcceptWorkoutResponse> acceptReceivedWorkout(final String sentWorkoutId, final String optionalName) {
        ResultStatus<AcceptWorkoutResponse> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(SentWorkout.SENT_WORKOUT_ID, sentWorkoutId);
        if (optionalName != null) {
            requestBody.put(Workout.WORKOUT_NAME, optionalName);
        }

        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(acceptReceivedWorkoutAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            try {
                resultStatus.setData(new AcceptWorkoutResponse(JsonParser.deserialize(apiResponse.getData())));
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("Unable to accept workout. 2");
            }
        } else if (apiResponse.isNetworkError()) {
            resultStatus.setErrorMessage("Network error. Unable to accept workout. Check internet connection.");
        } else {
            // todo actually use the error messages
            resultStatus.setErrorMessage("Unable to accept workout. 3");
        }
        return resultStatus;
    }

    public ResultStatus<String> declineReceivedWorkout(final String receivedWorkoutId) {
        ResultStatus<String> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(SentWorkout.SENT_WORKOUT_ID, receivedWorkoutId);

        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(declineReceivedWorkoutAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            try {
                resultStatus.setData("Workout successfully declined.");
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("Unable to decline workout. 2");
            }
        } else if (apiResponse.isNetworkError()) {
            resultStatus.setErrorMessage("Network error. Unable to decline workout. Check internet connection.");
        } else {
            // todo actually use the error messages
            resultStatus.setErrorMessage("Unable to decline workout. 3");
        }
        return resultStatus;
    }
}
