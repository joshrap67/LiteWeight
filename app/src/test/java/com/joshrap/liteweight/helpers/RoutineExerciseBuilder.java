package com.joshrap.liteweight.helpers;

import com.joshrap.liteweight.models.RoutineExercise;

import java.util.Random;
import java.util.UUID;

public class RoutineExerciseBuilder {

    private Boolean completed;
    private String exerciseId;
    private Double weight;
    private Integer sets;
    private Integer reps;
    private String details;

    // any fields not specified will be random
    public RoutineExerciseBuilder() {

    }

    public RoutineExerciseBuilder withCompleted(boolean completed) {
        this.completed = completed;
        return this;
    }

    public RoutineExerciseBuilder withExerciseId(String exerciseId) {
        this.exerciseId = exerciseId;
        return this;
    }

    public RoutineExerciseBuilder withWeight(double weight) {
        this.weight = weight;
        return this;
    }

    public RoutineExerciseBuilder withSets(int sets) {
        this.sets = sets;
        return this;
    }

    public RoutineExerciseBuilder withReps(int reps) {
        this.reps = reps;
        return this;
    }

    public RoutineExerciseBuilder withDetails(String details) {
        this.details = details;
        return this;
    }

    public RoutineExercise build() {
        Random rng = new Random();
        boolean completed = this.completed == null ? rng.nextBoolean() : this.completed;
        String exerciseId = this.exerciseId == null ? UUID.randomUUID().toString() : this.exerciseId;
        double weight = this.weight == null ? rng.nextDouble() * 300 : this.weight;
        int sets = this.sets == null ? rng.nextInt(100) : this.sets;
        int reps = this.reps == null ? rng.nextInt(100) : this.reps;
        String details = this.details == null ? UUID.randomUUID().toString() : this.details;

        return new RoutineExercise(completed, exerciseId, weight, sets, reps, details);
    }
}
