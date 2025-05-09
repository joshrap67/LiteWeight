package com.joshrap.liteweight.helpers;

import com.joshrap.liteweight.models.user.Link;
import com.joshrap.liteweight.models.user.OwnedExercise;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class OwnedExerciseBuilder {

    private String exerciseId;
    private String exerciseName;
    private Double defaultWeight;
    private Integer defaultSets;
    private Integer defaultReps;
    private String notes;
    private List<String> focuses;
    private List<Link> links;

    // any fields not specified will be random
    public OwnedExerciseBuilder() {

    }

    public OwnedExerciseBuilder withExerciseId(String exerciseId) {
        this.exerciseId = exerciseId;
        return this;
    }

    public OwnedExerciseBuilder withExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
        return this;
    }

    public OwnedExerciseBuilder withDefaultWeight(double weight) {
        this.defaultWeight = weight;
        return this;
    }

    public OwnedExerciseBuilder withDefaultSets(int sets) {
        this.defaultSets = sets;
        return this;
    }

    public OwnedExerciseBuilder withDefaultReps(int reps) {
        this.defaultReps = reps;
        return this;
    }

    public OwnedExerciseBuilder withNotes(String notes) {
        this.notes = notes;
        return this;
    }

    public OwnedExerciseBuilder withFocuses(List<String> focuses) {
        this.focuses = focuses;
        return this;
    }

    public OwnedExerciseBuilder withLinks(List<Link> links) {
        this.links = links;
        return this;
    }

    public OwnedExercise build() {
        Random rng = new Random();
        String exerciseId = this.exerciseId == null ? UUID.randomUUID().toString() : this.exerciseId;
        String exerciseName = this.exerciseName == null ? UUID.randomUUID().toString() : this.exerciseName;
        double weight = this.defaultWeight == null ? rng.nextDouble() * 300 : this.defaultWeight;
        int sets = this.defaultSets == null ? rng.nextInt(100) : this.defaultSets;
        int reps = this.defaultReps == null ? rng.nextInt(100) : this.defaultReps;
        String notes = this.notes == null ? UUID.randomUUID().toString() : this.notes;
        List<String> focuses = this.focuses;
        List<Link> links = this.links;
        if (this.focuses == null) {
            focuses = new ArrayList<>();
            focuses.add(UUID.randomUUID().toString());
            focuses.add(UUID.randomUUID().toString());
        }
        if (this.links == null) {
            links = new ArrayList<>();
            links.add(new Link(UUID.randomUUID().toString(), UUID.randomUUID().toString()));
            links.add(new Link(UUID.randomUUID().toString(), UUID.randomUUID().toString()));
        }

        return new OwnedExercise(exerciseId, exerciseName, weight, sets, reps, notes, links, focuses, new ArrayList<>());
    }
}
