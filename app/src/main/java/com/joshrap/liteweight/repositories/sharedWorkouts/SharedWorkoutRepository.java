package com.joshrap.liteweight.repositories.sharedWorkouts;

import static com.joshrap.liteweight.utils.NetworkUtils.getRoute;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.joshrap.liteweight.models.LiteWeightNetworkException;
import com.joshrap.liteweight.repositories.sharedWorkouts.responses.AcceptWorkoutResponse;
import com.joshrap.liteweight.models.sharedWorkout.SharedWorkout;
import com.joshrap.liteweight.repositories.ApiGateway;
import com.joshrap.liteweight.repositories.BodyRequest;
import com.joshrap.liteweight.repositories.sharedWorkouts.requests.AcceptSharedWorkoutRequest;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

public class SharedWorkoutRepository {

    private static final String acceptReceivedWorkoutRoute = "accept";
    private static final String declineReceivedWorkoutRoute = "decline";

    private static final String sharedWorkoutsRoute = "shared-workouts";
    private static final String sharedWorkoutsCollection = "sharedWorkouts";

    private final ApiGateway apiGateway;
    private final ObjectMapper objectMapper;

    @Inject
    public SharedWorkoutRepository(ApiGateway apiGateway, ObjectMapper objectMapper) {
        this.apiGateway = apiGateway;
        this.objectMapper = objectMapper;
    }

    // using firebase directly for optimized reads
    public SharedWorkout getReceivedWorkout(final String sharedWorkoutId) throws IOException, ExecutionException, InterruptedException {
        SharedWorkout sharedWorkout = null;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null && currentUser.isEmailVerified()) {
            DocumentReference docRef = db.collection(sharedWorkoutsCollection).document(sharedWorkoutId);
            Task<DocumentSnapshot> task = docRef.get();

            Tasks.await(task);

            if (task.isSuccessful()) {
                String result = objectMapper.writeValueAsString(task.getResult().getData());
                sharedWorkout = objectMapper.readValue(result, SharedWorkout.class);
                sharedWorkout.setId(task.getResult().getId());
            }
        }

        return sharedWorkout;
    }

    public AcceptWorkoutResponse acceptReceivedWorkout(final String sharedWorkoutId, final String optionalName) throws IOException, LiteWeightNetworkException {
        String route = getRoute(sharedWorkoutsRoute, sharedWorkoutId, acceptReceivedWorkoutRoute);
        BodyRequest body = new AcceptSharedWorkoutRequest(optionalName);
        String apiResponse = this.apiGateway.post(route, body);

        return this.objectMapper.readValue(apiResponse, AcceptWorkoutResponse.class);
    }

    public void declineReceivedWorkout(final String sharedWorkoutId) throws IOException, LiteWeightNetworkException {
        String route = getRoute(sharedWorkoutsRoute, sharedWorkoutId, declineReceivedWorkoutRoute);

        this.apiGateway.delete(route);
    }
}
