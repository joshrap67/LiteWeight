package com.joshrap.liteweight.repositories.self;

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
import com.joshrap.liteweight.repositories.self.requests.CreateUserRequest;
import com.joshrap.liteweight.repositories.self.requests.SetCurrentWorkoutRequest;
import com.joshrap.liteweight.repositories.self.requests.SetUserSettingsRequest;
import com.joshrap.liteweight.repositories.self.requests.UpdateProfilePictureRequest;
import com.joshrap.liteweight.models.user.User;
import com.joshrap.liteweight.models.user.UserSettings;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

public class SelfRepository {

    private static final String updateProfilePictureRoute = "profile-picture";
    private static final String setAllFriendRequestsSeenRoute = "all-friend-requests-seen";
    private static final String setSettingsRoute = "settings";
    private static final String setCurrentWorkoutRoute = "current-workout";

    private static final String selfRoute = "self";
    private static final String usersCollection = "users";

    private final ApiGateway apiGateway;
    private final ObjectMapper objectMapper;

    @Inject
    public SelfRepository(ApiGateway apiGateway, ObjectMapper objectMapper) {
        this.apiGateway = apiGateway;
        this.objectMapper = objectMapper;
    }

    // using firebase directly for optimized reads
    public User getSelf() throws ExecutionException, InterruptedException, JsonProcessingException {
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

    public User createSelf(String username, byte[] profilePictureData, boolean metricUnits) throws IOException, LiteWeightNetworkException {
        BodyRequest body = new CreateUserRequest(username, profilePictureData, metricUnits);
        String apiResponse = this.apiGateway.post(selfRoute, body);

        return this.objectMapper.readValue(apiResponse, User.class);
    }

    public void deleteSelf() throws IOException, LiteWeightNetworkException {
        String route = getRoute(selfRoute);
        this.apiGateway.delete(route);
    }

    public void updateProfilePicture(byte[] pictureData) throws IOException, LiteWeightNetworkException {
        UpdateProfilePictureRequest body = new UpdateProfilePictureRequest(pictureData);
        String route = getRoute(selfRoute, updateProfilePictureRoute);

        this.apiGateway.put(route, body);
    }

    // using firebase since it is a simple property set. Saves a bunch of api calls. Rules on the console are in place for security
    public void linkFirebaseMessagingToken(String tokenId) throws ExecutionException, InterruptedException {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null && currentUser.isEmailVerified()) {
            Task<Void> task = db.collection(usersCollection)
                    .document(currentUser.getUid())
                    .update(Collections.singletonMap("firebaseMessagingToken", tokenId));
            Tasks.await(task);
        }
    }

    public void setAllFriendRequestsSeen() throws IOException, LiteWeightNetworkException {
        String route = getRoute(selfRoute, setAllFriendRequestsSeenRoute);
        this.apiGateway.put(route);
    }

    public void setSettings(UserSettings userSettings) throws IOException, LiteWeightNetworkException {
        BodyRequest body = new SetUserSettingsRequest(userSettings);
        String route = getRoute(selfRoute, setSettingsRoute);

        this.apiGateway.put(route, body);
    }

    public void setCurrentWorkout(String workoutId) throws IOException, LiteWeightNetworkException {
        BodyRequest body = new SetCurrentWorkoutRequest(workoutId);
        String route = getRoute(selfRoute, setCurrentWorkoutRoute);

        this.apiGateway.put(route, body);
    }


}
