package com.joshrap.liteweight.repositories.users;

import static com.joshrap.liteweight.utils.NetworkUtils.getRoute;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joshrap.liteweight.models.LiteWeightNetworkException;
import com.joshrap.liteweight.models.user.Friend;
import com.joshrap.liteweight.repositories.ApiGateway;
import com.joshrap.liteweight.repositories.BodyRequest;
import com.joshrap.liteweight.repositories.users.requests.ReportUserRequest;
import com.joshrap.liteweight.repositories.users.requests.SendWorkoutRequest;
import com.joshrap.liteweight.repositories.users.responses.ReportUserResponse;
import com.joshrap.liteweight.repositories.users.responses.SearchByUsernameResponse;
import com.joshrap.liteweight.repositories.users.responses.SendWorkoutResponse;

import java.io.IOException;

import javax.inject.Inject;

public class UsersRepository {

    private static final String sendWorkoutRoute = "send-workout";
    private static final String searchByUsernameRoute = "search";
    private static final String sendFriendRequestRoute = "send-friend-request";
    private static final String acceptFriendRequestRoute = "accept-friend-request";
    private static final String removeFriendRoute = "friend";
    private static final String declineFriendRequestRoute = "decline-friend-request";
    private static final String cancelFriendRequestRoute = "cancel-friend-request";
    private static final String reportUserRoute = "report";

    private static final String usersRoute = "users";


    private final ApiGateway apiGateway;
    private final ObjectMapper objectMapper;

    @Inject
    public UsersRepository(ApiGateway apiGateway, ObjectMapper objectMapper) {
        this.apiGateway = apiGateway;
        this.objectMapper = objectMapper;
    }

    public SearchByUsernameResponse searchByUsername(String username) throws IOException, LiteWeightNetworkException {
        String route = getRoute(usersRoute, searchByUsernameRoute);
        route += "?username=" + username;
        String apiResponse = this.apiGateway.get(route);

        return this.objectMapper.readValue(apiResponse, SearchByUsernameResponse.class);
    }

    public Friend sendFriendRequest(String userId) throws IOException, LiteWeightNetworkException {
        String route = getRoute(usersRoute, userId, sendFriendRequestRoute);
        String apiResponse = this.apiGateway.put(route);

        return this.objectMapper.readValue(apiResponse, Friend.class);
    }

    public void sendWorkout(String userId, String workoutId) throws IOException, LiteWeightNetworkException {
        String route = getRoute(usersRoute, userId, sendWorkoutRoute);
        BodyRequest body = new SendWorkoutRequest(workoutId);
        String apiResponse = this.apiGateway.post(route, body);

        this.objectMapper.readValue(apiResponse, SendWorkoutResponse.class);
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

    public ReportUserResponse reportUser(String userId, String complaint) throws IOException, LiteWeightNetworkException {
        String route = getRoute(usersRoute, userId, reportUserRoute);
        BodyRequest request = new ReportUserRequest(complaint);
        String apiResponse = this.apiGateway.post(route, request);

        return this.objectMapper.readValue(apiResponse, ReportUserResponse.class);
    }
}
