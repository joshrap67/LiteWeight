package com.joshrap.liteweight.repositories.currentUser;

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
import com.joshrap.liteweight.repositories.currentUser.requests.CreateUserRequest;
import com.joshrap.liteweight.repositories.currentUser.requests.SetCurrentWorkoutRequest;
import com.joshrap.liteweight.repositories.currentUser.requests.SetPushEndpointRequest;
import com.joshrap.liteweight.repositories.currentUser.requests.SetUserPreferencesRequest;
import com.joshrap.liteweight.repositories.currentUser.requests.UpdateIconRequest;
import com.joshrap.liteweight.models.Result;
import com.joshrap.liteweight.models.user.User;
import com.joshrap.liteweight.models.user.UserPreferences;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

public class CurrentUserRepository {

    private static final String updateProfilePictureRoute = "update-icon";
    private static final String setPushEndpointRoute = "set-push-endpoint";
    private static final String removePushEndpointRoute = "remove-push-endpoint";
    private static final String setAllFriendRequestsSeenRoute = "set-all-friend-requests-seen";
    private static final String setUserPreferencesRoute = "set-user-preferences";
    private static final String setCurrentWorkoutRoute = "set-current-workout";
    private static final String setAllReceivedWorkoutsSeenRoute = "set-all-seen";
    private static final String setReceivedWorkoutSeenRoute = "set-seen";
    private static final String sendFeedbackAction = "sendFeedback";

    private static final String receivedWorkoutsRoute = "received-workouts";
    private static final String currentUserRoute = "current-user";

    private static final String usersCollection = "users";

    private final ApiGateway apiGateway;
    private final ObjectMapper objectMapper;

    @Inject
    public CurrentUserRepository(ApiGateway apiGateway, ObjectMapper objectMapper) {
        this.apiGateway = apiGateway;
        this.objectMapper = objectMapper;
    }

    // using firebase directly for optimized reads
    public User getUser() throws ExecutionException, InterruptedException, JsonProcessingException {
        User user = null;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null && currentUser.isEmailVerified()) {
            DocumentReference docRef = db.collection(usersCollection).document(currentUser.getUid());
            Task<DocumentSnapshot> task = docRef.get();
            Tasks.await(task);
            if (task.isSuccessful()) {
                String result = objectMapper.writeValueAsString(task.getResult().getData());
                user = objectMapper.readValue(result, User.class);
                if (user != null) {
                    user.setId(task.getResult().getId());
                }
            }
        }
        return user;
    }

    public User createUser(String username, boolean metricUnits) throws IOException, LiteWeightNetworkException, ExecutionException, InterruptedException {
        BodyRequest body = new CreateUserRequest(username, metricUnits);
        String apiResponse = this.apiGateway.post(currentUserRoute, body);

        return this.objectMapper.readValue(apiResponse, User.class);
    }

    public void updateProfilePicture(byte[] pictureData) throws IOException, LiteWeightNetworkException {
        UpdateIconRequest body = new UpdateIconRequest(pictureData);
        String route = getRoute(currentUserRoute, updateProfilePictureRoute);

        this.apiGateway.put(route, body);
    }

    public void setPushEndpointId(String tokenId) throws IOException, LiteWeightNetworkException {
        BodyRequest body = new SetPushEndpointRequest(tokenId);
        String route = getRoute(currentUserRoute, setPushEndpointRoute);

        this.apiGateway.put(route, body);
    }

    public void removePushEndpointId() throws IOException, LiteWeightNetworkException {
        String route = getRoute(currentUserRoute, removePushEndpointRoute);
        this.apiGateway.delete(route);
    }

    public void setAllFriendRequestsSeen() throws IOException, LiteWeightNetworkException {
        String route = getRoute(currentUserRoute, setAllFriendRequestsSeenRoute);
        this.apiGateway.put(route);
    }

    public void setUserPreferences(UserPreferences userPreferences) throws IOException, LiteWeightNetworkException {
        BodyRequest body = new SetUserPreferencesRequest(userPreferences);
        String route = getRoute(currentUserRoute, setUserPreferencesRoute);

        this.apiGateway.put(route, body);
    }

    public void setCurrentWorkout(String workoutId) throws IOException, LiteWeightNetworkException {
        BodyRequest body = new SetCurrentWorkoutRequest(workoutId);
        String route = getRoute(currentUserRoute, setCurrentWorkoutRoute);

        this.apiGateway.put(route, body);
    }

    public Result<String> sendFeedback(String feedback, String feedbackTime) {
        Result<String> result = new Result<>();
        // todo
        return result;
    }

    public void setAllReceivedWorkoutsSeen() throws IOException, LiteWeightNetworkException {
        String route = getRoute(currentUserRoute, receivedWorkoutsRoute, setAllReceivedWorkoutsSeenRoute);
        this.apiGateway.put(route);
    }

    public void setReceivedWorkoutSeen(String sharedWorkoutId) throws IOException, LiteWeightNetworkException {
        String route = getRoute(currentUserRoute, receivedWorkoutsRoute, sharedWorkoutId, setReceivedWorkoutSeenRoute);
        this.apiGateway.put(route);
    }
}
