package com.joshrap.liteweight.network.repos;

import com.joshrap.liteweight.utils.JsonUtils;
import com.joshrap.liteweight.models.AcceptWorkoutResponse;
import com.joshrap.liteweight.models.SharedWorkoutMeta;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.Routine;
import com.joshrap.liteweight.models.SharedWorkout;
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
                resultStatus.setData(new UserWithWorkout(JsonUtils.deserialize(apiResponse.getData())));
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("Unable to create workout.");
            }
        } else {
            resultStatus.setErrorMessage("Unable to create workout.");
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
                resultStatus.setData(new UserWithWorkout(JsonUtils.deserialize(apiResponse.getData())));
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("Unable to copy workout.");
            }
        } else {
            resultStatus.setErrorMessage("Unable to copy workout.");
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
                resultStatus.setData(new UserWithWorkout(JsonUtils.deserialize(apiResponse.getData())));
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("Unable to switch workout.");
            }
        } else {
            resultStatus.setErrorMessage("Unable to switch workout.");
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
                resultStatus.setData(new User(JsonUtils.deserialize(apiResponse.getData())));
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("Unable to copy workout.");
            }
        } else {
            resultStatus.setErrorMessage("Unable to copy workout.");
        }
        return resultStatus;
    }

    public ResultStatus<UserWithWorkout> deleteWorkoutThenFetchNext(String workoutId, String nextWorkoutId) {
        ResultStatus<UserWithWorkout> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(Workout.WORKOUT_ID, workoutId);
        requestBody.put(RequestFields.NEXT_WORKOUT_ID, nextWorkoutId);

        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(deleteWorkoutThenFetchAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            try {
                resultStatus.setData(new UserWithWorkout(JsonUtils.deserialize(apiResponse.getData())));
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("Unable to delete workout.");
            }
        } else {
            resultStatus.setErrorMessage("Unable to delete workout.");
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
                resultStatus.setData(new User(JsonUtils.deserialize(apiResponse.getData())));
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("Unable to copy workout.");
            }
        } else {
            resultStatus.setErrorMessage("Unable to copy workout.");
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
                resultStatus.setData(new UserWithWorkout(JsonUtils.deserialize(apiResponse.getData())));
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("Unable to edit workout.");
            }
        } else {
            resultStatus.setErrorMessage("Unable to edit workout.");
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
                resultStatus.setErrorMessage("Unable to sync workout.");
            }
        } else {
            resultStatus.setErrorMessage("Unable to sync workout.");
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
                resultStatus.setData(new UserWithWorkout(JsonUtils.deserialize(apiResponse.getData())));
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("Unable to restart workout.");
            }
        } else {
            resultStatus.setErrorMessage("Unable to restart workout.");
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
                resultStatus.setData((String) JsonUtils.deserialize(apiResponse.getData()).get(SharedWorkout.SENT_WORKOUT_ID));
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("Unable to send workout.");
            }
        } else {
            resultStatus.setErrorMessage("Unable to send workout.");
        }
        return resultStatus;
    }

    public ResultStatus<List<SharedWorkoutMeta>> getReceivedWorkouts(final int batchNumber) {
        ResultStatus<List<SharedWorkoutMeta>> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(RequestFields.BATCH_NUMBER, batchNumber);

        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(getReceivedWorkoutsAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            try {
                Map<String, Object> receivedWorkoutsResponseRaw = JsonUtils.deserialize(apiResponse.getData());
                List<SharedWorkoutMeta> sharedWorkoutMetas = new ArrayList<>();
                for (String workoutId : receivedWorkoutsResponseRaw.keySet()) {
                    sharedWorkoutMetas.add(new SharedWorkoutMeta((Map<String, Object>) receivedWorkoutsResponseRaw.get(workoutId)));
                }
                resultStatus.setData(sharedWorkoutMetas);
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("Unable to receive workouts.");
            }
        } else {
            resultStatus.setErrorMessage("Unable to receive workouts.");
        }
        return resultStatus;
    }

    public ResultStatus<SharedWorkout> getReceivedWorkout(final String sentWorkoutId) {
        ResultStatus<SharedWorkout> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(SharedWorkout.SENT_WORKOUT_ID, sentWorkoutId);

        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(getReceivedWorkoutAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            try {
                resultStatus.setData(new SharedWorkout(JsonUtils.deserialize(apiResponse.getData())));
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("Unable to receive workout.");
            }
        } else {
            resultStatus.setErrorMessage("Unable to receive workout.");
        }
        return resultStatus;
    }

    public ResultStatus<String> setReceivedWorkoutSeen(String sentWorkoutId) {
        ResultStatus<String> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(SharedWorkout.SENT_WORKOUT_ID, sentWorkoutId);

        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(setReceivedWorkoutSeenAction, requestBody, true);
        if (apiResponse.isSuccess()) {
            try {
                resultStatus.setData(apiResponse.getData());
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("Unable to set received workout as seen.");
            }
        } else {
            resultStatus.setErrorMessage("Unable to set received workout as seen.");
        }
        return resultStatus;
    }

    public ResultStatus<String> setAllReceivedWorkoutsSeen() {
        ResultStatus<String> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(setAllReceivedWorkoutsSeenAction, requestBody, true);
        if (apiResponse.isSuccess()) {
            try {
                resultStatus.setData(apiResponse.getData());
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("Unable to set all received workouts as seen.");
            }
        } else {
            resultStatus.setErrorMessage("Unable to set all received workouts as seen.");
        }
        return resultStatus;
    }

    public ResultStatus<AcceptWorkoutResponse> acceptReceivedWorkout(final String sentWorkoutId, final String optionalName) {
        ResultStatus<AcceptWorkoutResponse> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(SharedWorkout.SENT_WORKOUT_ID, sentWorkoutId);
        if (optionalName != null) {
            requestBody.put(Workout.WORKOUT_NAME, optionalName);
        }

        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(acceptReceivedWorkoutAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            try {
                resultStatus.setData(new AcceptWorkoutResponse(JsonUtils.deserialize(apiResponse.getData())));
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("Unable to accept workout.");
            }
        } else {
            resultStatus.setErrorMessage(apiResponse.getData());
        }
        return resultStatus;
    }

    public ResultStatus<String> declineReceivedWorkout(final String receivedWorkoutId) {
        ResultStatus<String> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(SharedWorkout.SENT_WORKOUT_ID, receivedWorkoutId);

        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(declineReceivedWorkoutAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            try {
                resultStatus.setData("Workout successfully declined.");
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("Unable to decline workout.");
            }
        } else {
            resultStatus.setErrorMessage(apiResponse.getData());
        }
        return resultStatus;
    }
}
