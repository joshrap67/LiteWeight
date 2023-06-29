package com.joshrap.liteweight.repositories.receivedWorkouts;

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
import com.joshrap.liteweight.repositories.receivedWorkouts.responses.AcceptWorkoutResponse;
import com.joshrap.liteweight.models.receivedWorkout.ReceivedWorkout;
import com.joshrap.liteweight.repositories.ApiGateway;
import com.joshrap.liteweight.repositories.BodyRequest;
import com.joshrap.liteweight.repositories.receivedWorkouts.requests.AcceptReceivedWorkoutRequest;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

public class ReceivedWorkoutRepository {

    private static final String acceptReceivedWorkoutRoute = "accept";
    private static final String declineReceivedWorkoutRoute = "decline";

    private static final String receivedWorkoutsRoute = "received-workouts";
    private static final String receivedWorkoutsCollection = "receivedWorkouts";

    private final ApiGateway apiGateway;
    private final ObjectMapper objectMapper;

    @Inject
    public ReceivedWorkoutRepository(ApiGateway apiGateway, ObjectMapper objectMapper) {
        this.apiGateway = apiGateway;
        this.objectMapper = objectMapper;
    }

    // using firebase directly for optimized reads
    public ReceivedWorkout getReceivedWorkout(final String receivedWorkoutId) throws IOException, ExecutionException, InterruptedException {
        ReceivedWorkout receivedWorkout = null;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null && currentUser.isEmailVerified()) {
            DocumentReference docRef = db.collection(receivedWorkoutsCollection).document(receivedWorkoutId);
            Task<DocumentSnapshot> task = docRef.get();

            Tasks.await(task);

            if (task.isSuccessful()) {
                String result = objectMapper.writeValueAsString(task.getResult().getData());
                receivedWorkout = objectMapper.readValue(result, ReceivedWorkout.class);
                receivedWorkout.setId(task.getResult().getId());
            }
        }

        return receivedWorkout;
    }

    public AcceptWorkoutResponse acceptReceivedWorkout(final String receivedWorkoutId, final String optionalName) throws IOException, LiteWeightNetworkException {
        String route = getRoute(receivedWorkoutsRoute, receivedWorkoutId, acceptReceivedWorkoutRoute);
        BodyRequest body = new AcceptReceivedWorkoutRequest(optionalName);
        String apiResponse = this.apiGateway.post(route, body);

        return this.objectMapper.readValue(apiResponse, AcceptWorkoutResponse.class);
    }

    public void declineReceivedWorkout(final String receivedWorkoutId) throws IOException, LiteWeightNetworkException {
        String route = getRoute(receivedWorkoutsRoute, receivedWorkoutId, declineReceivedWorkoutRoute);

        this.apiGateway.delete(route);
    }
}
