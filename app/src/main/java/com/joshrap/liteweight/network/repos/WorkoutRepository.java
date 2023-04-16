package com.joshrap.liteweight.network.repos;

import com.joshrap.liteweight.models.WorkoutMeta;
import com.joshrap.liteweight.utils.JsonUtils;
import com.joshrap.liteweight.models.AcceptWorkoutResponse;
import com.joshrap.liteweight.models.SharedWorkoutMeta;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.Routine;
import com.joshrap.liteweight.models.SharedWorkout;
import com.joshrap.liteweight.models.User;
import com.joshrap.liteweight.models.UserAndWorkout;
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
    private static final String getSharedWorkoutAction = "getSharedWorkout";
    private static final String acceptReceivedWorkoutAction = "acceptReceivedWorkout";
    private static final String declineReceivedWorkoutAction = "declineReceivedWorkout";

    private final ApiGateway apiGateway;

    @Inject
    public WorkoutRepository(ApiGateway apiGateway) {
        this.apiGateway = apiGateway;
    }

    public ResultStatus<UserAndWorkout> createWorkout(@NonNull Routine routine, @NonNull String workoutName) {
        ResultStatus<UserAndWorkout> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(Workout.ROUTINE, routine.asMap());
        requestBody.put(Workout.WORKOUT_NAME, workoutName);

        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(newWorkoutAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            try {
                resultStatus.setData(new UserAndWorkout(JsonUtils.deserialize(apiResponse.getData())));
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("There was a problem trying to create the workout.");
            }
        } else {
            resultStatus.setErrorMessage("There was a problem trying to create the workout.");
        }
        return resultStatus;
    }

    public ResultStatus<UserAndWorkout> copyWorkout(@NonNull Workout workout, @NonNull String workoutName) {
        ResultStatus<UserAndWorkout> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(RequestFields.WORKOUT, workout.asMap());
        requestBody.put(Workout.WORKOUT_NAME, workoutName);

        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(copyWorkoutAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            try {
                resultStatus.setData(new UserAndWorkout(JsonUtils.deserialize(apiResponse.getData())));
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("There was a problem trying to copy the workout.");
            }
        } else {
            resultStatus.setErrorMessage("There was a problem trying to copy the workout.");
        }
        return resultStatus;
    }

    public ResultStatus<UserAndWorkout> switchWorkout(@NonNull Workout oldWorkout, @NonNull String workoutId) {
        ResultStatus<UserAndWorkout> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(RequestFields.WORKOUT, oldWorkout.asMap());
        requestBody.put(Workout.WORKOUT_ID, workoutId);

        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(switchWorkoutAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            try {
                resultStatus.setData(new UserAndWorkout(JsonUtils.deserialize(apiResponse.getData())));
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("There was a problem trying to switch workouts.");
            }
        } else {
            resultStatus.setErrorMessage("There was a problem trying to switch workouts.");
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
                resultStatus.setErrorMessage("There was a problem trying to rename the workout.");
            }
        } else {
            resultStatus.setErrorMessage("There was a problem trying to rename the workout.");
        }
        return resultStatus;
    }

    public ResultStatus<UserAndWorkout> deleteWorkoutThenFetchNext(String workoutId, String nextWorkoutId) {
        ResultStatus<UserAndWorkout> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(Workout.WORKOUT_ID, workoutId);
        requestBody.put(RequestFields.NEXT_WORKOUT_ID, nextWorkoutId);

        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(deleteWorkoutThenFetchAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            try {
                resultStatus.setData(new UserAndWorkout(JsonUtils.deserialize(apiResponse.getData())));
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("There was a problem trying to delete the workout.");
            }
        } else {
            resultStatus.setErrorMessage("There was a problem trying to delete the workout");
        }
        return resultStatus;
    }

    public ResultStatus<WorkoutMeta> resetWorkoutStatistics(@NonNull String workoutId) {
        ResultStatus<WorkoutMeta> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(Workout.WORKOUT_ID, workoutId);

        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(resetWorkoutStatisticsAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            try {
                resultStatus.setData(new WorkoutMeta(JsonUtils.deserialize(apiResponse.getData()), workoutId));
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("There was a problem trying to reset the statistics.");
            }
        } else {
            resultStatus.setErrorMessage("There was a problem trying to reset the statistics.");
        }
        return resultStatus;
    }

    public ResultStatus<UserAndWorkout> editWorkout(@NonNull String workoutId, @NonNull Workout workout) {
        ResultStatus<UserAndWorkout> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(Workout.WORKOUT_ID, workoutId);
        requestBody.put(RequestFields.WORKOUT, workout.asMap());

        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(editWorkoutAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            try {
                resultStatus.setData(new UserAndWorkout(JsonUtils.deserialize(apiResponse.getData())));
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("There was a problem trying to edit the workout.");
            }
        } else {
            resultStatus.setErrorMessage("There was a problem trying to edit the workout.");
        }
        return resultStatus;
    }

    public ResultStatus<String> syncWorkout(@NonNull Workout workout) {
        ResultStatus<String> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(RequestFields.WORKOUT, workout.asMap());

        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(syncWorkoutAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            resultStatus.setData(apiResponse.getData());
            resultStatus.setSuccess(true);
        } else {
            resultStatus.setErrorMessage("There was a problem trying to sync the workout.");
        }
        return resultStatus;
    }

    public ResultStatus<UserAndWorkout> restartWorkout(@NonNull Workout workout) {
        ResultStatus<UserAndWorkout> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(RequestFields.WORKOUT, workout.asMap());

        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(restartWorkoutAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            try {
                resultStatus.setData(new UserAndWorkout(JsonUtils.deserialize(apiResponse.getData())));
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("There was a problem trying to restart the workout.");
            }
        } else {
            resultStatus.setErrorMessage("There was a problem trying to restart the workout.");
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
                resultStatus.setData((String) JsonUtils.deserialize(apiResponse.getData()).get(SharedWorkout.SHARED_WORKOUT_ID));
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("There was a problem trying to send the workout.");
            }
        } else {
            resultStatus.setErrorMessage("There was a problem trying to send the workout.");
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
                resultStatus.setErrorMessage("There was a problem trying to fetch received workouts.");
            }
        } else {
            resultStatus.setErrorMessage("There was a problem trying to fetch received workouts.");
        }
        return resultStatus;
    }

    public ResultStatus<SharedWorkout> getReceivedWorkout(final String sharedWorkoutId) {
        ResultStatus<SharedWorkout> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(SharedWorkout.SHARED_WORKOUT_ID, sharedWorkoutId);

        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(getSharedWorkoutAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            try {
                resultStatus.setData(new SharedWorkout(JsonUtils.deserialize(apiResponse.getData())));
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("There was a problem trying to load the received workout.");
            }
        } else {
            resultStatus.setErrorMessage("There was a problem trying to load the received workout.");
        }
        return resultStatus;
    }

    public ResultStatus<AcceptWorkoutResponse> acceptReceivedWorkout(final String sharedWorkoutId, final String optionalName) {
        ResultStatus<AcceptWorkoutResponse> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(SharedWorkout.SHARED_WORKOUT_ID, sharedWorkoutId);
        if (optionalName != null) {
            requestBody.put(Workout.WORKOUT_NAME, optionalName);
        }

        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(acceptReceivedWorkoutAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            try {
                resultStatus.setData(new AcceptWorkoutResponse(JsonUtils.deserialize(apiResponse.getData())));
                resultStatus.setSuccess(true);
            } catch (Exception e) {
                resultStatus.setErrorMessage("There was a problem trying to accept the workout.");
            }
        } else {
            resultStatus.setErrorMessage(apiResponse.getErrorMessage());
        }
        return resultStatus;
    }

    public ResultStatus<String> declineReceivedWorkout(final String receivedWorkoutId) {
        ResultStatus<String> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(SharedWorkout.SHARED_WORKOUT_ID, receivedWorkoutId);

        ResultStatus<String> apiResponse = this.apiGateway.makeRequest(declineReceivedWorkoutAction, requestBody, true);

        if (apiResponse.isSuccess()) {
            resultStatus.setData("Workout successfully declined.");
            resultStatus.setSuccess(true);
        } else {
            resultStatus.setErrorMessage(apiResponse.getErrorMessage());
        }
        return resultStatus;
    }
}
