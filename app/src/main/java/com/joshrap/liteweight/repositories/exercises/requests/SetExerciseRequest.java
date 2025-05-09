package com.joshrap.liteweight.repositories.exercises.requests;

import com.joshrap.liteweight.models.user.Link;
import com.joshrap.liteweight.models.user.OwnedExercise;
import com.joshrap.liteweight.repositories.BodyRequest;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SetExerciseRequest extends BodyRequest {

    private String name;
    private double defaultWeight;
    private int defaultSets;
    private int defaultReps;
    private List<String> focuses;
    private List<Link> links;
    private String notes;

    public SetExerciseRequest(OwnedExercise ownedExercise) {
        this.name = ownedExercise.getName();
        this.defaultWeight = ownedExercise.getDefaultWeight();
        this.defaultSets = ownedExercise.getDefaultSets();
        this.defaultReps = ownedExercise.getDefaultReps();
        this.focuses = ownedExercise.getFocuses();
        this.notes = ownedExercise.getNotes();
        this.links = ownedExercise.getLinks();
    }
}
