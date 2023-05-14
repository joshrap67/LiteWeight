package com.joshrap.liteweight.repositories.workouts;

import static com.joshrap.liteweight.utils.NetworkUtils.getRoute;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.joshrap.liteweight.models.LiteWeightNetworkException;
import com.joshrap.liteweight.repositories.ApiGateway;
import com.joshrap.liteweight.repositories.BodyRequest;
import com.joshrap.liteweight.repositories.workouts.requests.CopyWorkoutRequest;
import com.joshrap.liteweight.repositories.workouts.requests.CreateWorkoutRequest;
import com.joshrap.liteweight.repositories.workouts.requests.RenameWorkoutRequest;
import com.joshrap.liteweight.repositories.workouts.requests.RestartWorkoutRequest;
import com.joshrap.liteweight.repositories.workouts.requests.SetRoutineRequest;
import com.joshrap.liteweight.repositories.workouts.requests.UpdateWorkoutRequest;
import com.joshrap.liteweight.models.workout.Routine;
import com.joshrap.liteweight.models.UserAndWorkout;
import com.joshrap.liteweight.models.workout.Workout;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import lombok.NonNull;

public class WorkoutRepository {

    private static final String copyWorkoutRoute = "copy";
    private static final String renameWorkoutRoute = "rename";
    private static final String resetStatisticsRoute = "reset-statistics";
    private static final String setRoutineRoute = "routine";
    private static final String updateWorkoutRoute = "update";
    private static final String restartWorkoutRoute = "restart";

    private static final String workoutsRoute = "workouts";
    private static final String workoutsCollection = "workouts";

    private final ApiGateway apiGateway;
    private final ObjectMapper objectMapper;

    @Inject
    public WorkoutRepository(ApiGateway apiGateway, ObjectMapper objectMapper) {
        this.apiGateway = apiGateway;
        this.objectMapper = objectMapper;
    }

    public UserAndWorkout createWorkout(@NonNull Routine routine, @NonNull String workoutName, boolean setAsCurrentWorkout)
            throws IOException, LiteWeightNetworkException {
        BodyRequest body = new CreateWorkoutRequest(workoutName, routine, setAsCurrentWorkout);
        String apiResponse = this.apiGateway.post(workoutsRoute, body);

        return this.objectMapper.readValue(apiResponse, UserAndWorkout.class);
    }

    // using firebase directly for optimized reads
    public Workout getWorkout(String workoutId) throws ExecutionException, InterruptedException, JsonProcessingException {
        Workout workout = null;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null && currentUser.isEmailVerified()) {
            DocumentReference docRef = db.collection(workoutsCollection).document(workoutId);
            Task<DocumentSnapshot> task = docRef.get();

            Tasks.await(task);

            if (task.isSuccessful()) {
                String result = objectMapper.writeValueAsString(task.getResult().getData());
                workout = objectMapper.readValue(result, Workout.class);
                workout.setId(task.getResult().getId());
            }
        }
        return workout;
    }

    public UserAndWorkout copyWorkout(@NonNull String workoutId, @NonNull String workoutName)
            throws IOException, LiteWeightNetworkException {
        String route = getRoute(workoutsRoute, workoutId, copyWorkoutRoute);
        BodyRequest body = new CopyWorkoutRequest(workoutName);
        String apiResponse = this.apiGateway.post(route, body);

        return this.objectMapper.readValue(apiResponse, UserAndWorkout.class);
    }

    public void renameWorkout(@NonNull String workoutId, @NonNull String workoutName)
            throws IOException, LiteWeightNetworkException {
        String route = getRoute(workoutsRoute, workoutId, renameWorkoutRoute);
        BodyRequest body = new RenameWorkoutRequest(workoutName);

        this.apiGateway.post(route, body);
    }

    public void deleteWorkout(String workoutId)
            throws IOException, LiteWeightNetworkException {
        String route = getRoute(workoutsRoute, workoutId);
        this.apiGateway.delete(route);
    }

    public void resetWorkoutStatistics(@NonNull String workoutId)
            throws IOException, LiteWeightNetworkException {
        String route = getRoute(workoutsRoute, workoutId, resetStatisticsRoute);
        this.apiGateway.put(route);
    }

    public UserAndWorkout setRoutine(@NonNull String workoutId, @NonNull Routine routine)
            throws IOException, LiteWeightNetworkException {
        BodyRequest body = new SetRoutineRequest(routine);
        String route = getRoute(workoutsRoute, workoutId, setRoutineRoute);
        String apiResponse = this.apiGateway.put(route, body);

        return this.objectMapper.readValue(apiResponse, UserAndWorkout.class);
    }

    public void updateWorkout(@NonNull Workout workout)
            throws IOException, LiteWeightNetworkException {
        // todo use firebase. use update to ensure it doesn't overwrite schema on backend
        // need to ensure validation is present... might not be worth doing it on frontend anymore
        BodyRequest body = new UpdateWorkoutRequest(workout.getCurrentWeek(), workout.getCurrentDay(), workout.getRoutine());
        String route = getRoute(workoutsRoute, workout.getId(), updateWorkoutRoute);

        this.apiGateway.put(route, body);
    }

    public UserAndWorkout restartWorkout(@NonNull Workout workout)
            throws IOException, LiteWeightNetworkException {
        BodyRequest body = new RestartWorkoutRequest(workout.getRoutine());
        String route = getRoute(workoutsRoute, workout.getId(), restartWorkoutRoute);
        String apiResponse = this.apiGateway.post(route, body);

        return this.objectMapper.readValue(apiResponse, UserAndWorkout.class);
    }
}
