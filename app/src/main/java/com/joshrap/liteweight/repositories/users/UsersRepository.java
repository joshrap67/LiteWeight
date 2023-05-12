package com.joshrap.liteweight.repositories.users;

import static com.joshrap.liteweight.utils.NetworkUtils.getRoute;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joshrap.liteweight.models.LiteWeightNetworkException;
import com.joshrap.liteweight.models.user.Friend;
import com.joshrap.liteweight.repositories.ApiGateway;
import com.joshrap.liteweight.repositories.BodyRequest;
import com.joshrap.liteweight.repositories.users.requests.SendFriendRequestRequest;
import com.joshrap.liteweight.repositories.users.requests.ShareWorkoutRequest;
import com.joshrap.liteweight.repositories.users.responses.ShareWorkoutResponse;

import java.io.IOException;

import javax.inject.Inject;

public class UsersRepository {

    private static final String shareWorkoutRoute = "share-workout";
    private static final String sendFriendRequestRoute = "send-friend-request";
    private static final String acceptFriendRequestRoute = "accept-friend-request";
    private static final String removeFriendRoute = "remove-friend";
    private static final String declineFriendRequestRoute = "decline-friend-request";
    private static final String cancelFriendRequestRoute = "cancel-friend-request";

    private static final String usersRoute = "users";


    private final ApiGateway apiGateway;
    private final ObjectMapper objectMapper;

    @Inject
    public UsersRepository(ApiGateway apiGateway, ObjectMapper objectMapper) {
        this.apiGateway = apiGateway;
        this.objectMapper = objectMapper;
    }

    public Friend sendFriendRequest(String username) throws IOException, LiteWeightNetworkException {
        String route = getRoute(usersRoute, sendFriendRequestRoute);
        BodyRequest body = new SendFriendRequestRequest(username);
        String apiResponse = this.apiGateway.post(route, body);

        return this.objectMapper.readValue(apiResponse, Friend.class);
    }

    public void shareWorkout(String recipientUsername, String workoutId) throws IOException, LiteWeightNetworkException {
        String route = getRoute(usersRoute, shareWorkoutRoute);
        BodyRequest body = new ShareWorkoutRequest(workoutId, recipientUsername);
        String apiResponse = this.apiGateway.post(route, body);

        this.objectMapper.readValue(apiResponse, ShareWorkoutResponse.class);
    }

    public void cancelFriendRequest(String userId) throws IOException, LiteWeightNetworkException {
        String route = getRoute(usersRoute, userId, cancelFriendRequestRoute);
        this.apiGateway.put(route);
    }

    public void acceptFriendRequest(String userId) throws IOException, LiteWeightNetworkException {
        String route = getRoute(usersRoute, userId, acceptFriendRequestRoute);
        this.apiGateway.put(route);
    }

    public void removeFriend(String userId) throws IOException, LiteWeightNetworkException {
        String route = getRoute(usersRoute, userId, removeFriendRoute);
        this.apiGateway.delete(route);
    }

    public void declineFriendRequest(String userId) throws IOException, LiteWeightNetworkException {
        String route = getRoute(usersRoute, userId, declineFriendRequestRoute);
        this.apiGateway.put(route);
    }
}
