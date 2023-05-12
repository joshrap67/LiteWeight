package com.joshrap.liteweight.repositories.exercises.requests;

import com.joshrap.liteweight.models.user.OwnedExercise;
import com.joshrap.liteweight.repositories.BodyRequest;

import java.util.List;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SetExerciseRequest extends BodyRequest {

    public String name;
    public double defaultWeight;
    public int defaultSets;
    public int defaultReps;
    public List<String> focuses;
    public String defaultDetails;
    public String videoUrl;

    public SetExerciseRequest(OwnedExercise ownedExercise) {
        this.name = ownedExercise.getName();
        this.defaultWeight = ownedExercise.getDefaultWeight();
        this.defaultSets = ownedExercise.getDefaultSets();
        this.defaultReps = ownedExercise.getDefaultReps();
        this.focuses = ownedExercise.getFocuses();
        this.defaultDetails = ownedExercise.getDefaultDetails();
        this.videoUrl = ownedExercise.getVideoUrl();
    }
}
