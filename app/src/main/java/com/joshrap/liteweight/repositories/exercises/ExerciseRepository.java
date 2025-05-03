package com.joshrap.liteweight.repositories.exercises;

import static com.joshrap.liteweight.utils.NetworkUtils.getRoute;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joshrap.liteweight.models.LiteWeightNetworkException;
import com.joshrap.liteweight.models.user.Link;
import com.joshrap.liteweight.models.user.OwnedExercise;
import com.joshrap.liteweight.repositories.ApiGateway;
import com.joshrap.liteweight.repositories.BodyRequest;
import com.joshrap.liteweight.repositories.exercises.requests.SetExerciseRequest;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

public class ExerciseRepository {

    private static final String exerciseRoute = "exercises";

    private final ApiGateway apiGateway;
    private final ObjectMapper objectMapper;

    @Inject
    public ExerciseRepository(ApiGateway apiGateway, ObjectMapper objectMapper) {
        this.apiGateway = apiGateway;
        this.objectMapper = objectMapper;
    }

    public OwnedExercise newExercise(String exerciseName, List<String> focuses, double weight, int sets, int reps, String notes, List<Link> links)
            throws LiteWeightNetworkException, IOException {
        BodyRequest body = new SetExerciseRequest(exerciseName, weight, sets, reps, focuses, links, notes);

        String apiResponse = this.apiGateway.post(exerciseRoute, body);
        return this.objectMapper.readValue(apiResponse, OwnedExercise.class);
    }

    public void updateExercise(String exerciseId, OwnedExercise ownedExercise) throws LiteWeightNetworkException, IOException {
        BodyRequest body = new SetExerciseRequest(ownedExercise);
        String route = getRoute(exerciseRoute, exerciseId);

        this.apiGateway.put(route, body);
    }

    public void deleteExercise(String exerciseId) throws LiteWeightNetworkException, IOException {
        String route = getRoute(exerciseRoute, exerciseId);
        this.apiGateway.delete(route);
    }
}
