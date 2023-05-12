package com.joshrap.liteweight.repositories.sharedWorkouts;

import static com.joshrap.liteweight.utils.NetworkUtils.getRoute;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joshrap.liteweight.models.LiteWeightNetworkException;
import com.joshrap.liteweight.repositories.sharedWorkouts.responses.AcceptWorkoutResponse;
import com.joshrap.liteweight.models.sharedWorkout.SharedWorkout;
import com.joshrap.liteweight.repositories.ApiGateway;
import com.joshrap.liteweight.repositories.BodyRequest;
import com.joshrap.liteweight.repositories.sharedWorkouts.requests.AcceptSharedWorkoutRequest;

import java.io.IOException;

import javax.inject.Inject;

public class SharedWorkoutRepository {

    private static final String acceptReceivedWorkoutRoute = "accept";
    private static final String declineReceivedWorkoutRoute = "decline";

    private static final String sharedWorkoutsRoute = "shared-workouts";

    private final ApiGateway apiGateway;
    private final ObjectMapper objectMapper;

    @Inject
    public SharedWorkoutRepository(ApiGateway apiGateway, ObjectMapper objectMapper) {
        this.apiGateway = apiGateway;
        this.objectMapper = objectMapper;
    }

    public SharedWorkout getReceivedWorkout(final String sharedWorkoutId) throws IOException, LiteWeightNetworkException {
        String route = getRoute(sharedWorkoutsRoute, sharedWorkoutId);
        String apiResponse = this.apiGateway.get(route);

        return this.objectMapper.readValue(apiResponse, SharedWorkout.class);
    }

    public AcceptWorkoutResponse acceptReceivedWorkout(final String sharedWorkoutId, final String optionalName) throws IOException, LiteWeightNetworkException {
        String route = getRoute(sharedWorkoutsRoute, sharedWorkoutId, acceptReceivedWorkoutRoute);
        BodyRequest body = new AcceptSharedWorkoutRequest(optionalName);
        String apiResponse = this.apiGateway.post(route, body);

        return this.objectMapper.readValue(apiResponse, AcceptWorkoutResponse.class);
    }

    public void declineReceivedWorkout(final String sharedWorkoutId) throws IOException, LiteWeightNetworkException {
        String route = getRoute(sharedWorkoutsRoute, sharedWorkoutId, declineReceivedWorkoutRoute);

        this.apiGateway.post(route);
    }
}
