package com.joshrap.liteweight.models.workout;

import androidx.annotation.NonNull;

import com.joshrap.liteweight.models.user.OwnedExercise;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoutineExercise implements Cloneable {

    private boolean completed;
    private String exerciseId;
    private double weight;
    private int sets;
    private int reps;
    private String details;

    @NonNull
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public RoutineExercise(RoutineExercise toBeCopied) {
        // Copy constructor used for deep copies
        this.completed = toBeCopied.completed;
        this.exerciseId = toBeCopied.exerciseId;
        this.weight = toBeCopied.weight;
        this.sets = toBeCopied.sets;
        this.reps = toBeCopied.reps;
        this.details = toBeCopied.details;
    }

    public RoutineExercise(OwnedExercise ownedExercise, String exerciseId) {
        this.completed = false;
        this.exerciseId = exerciseId;
        this.weight = ownedExercise.getDefaultWeight();
        this.sets = ownedExercise.getDefaultSets();
        this.reps = ownedExercise.getDefaultReps();
        this.details = ownedExercise.getDefaultDetails();
    }

    public static boolean exercisesDifferent(RoutineExercise exercise1, RoutineExercise exercise2) {
        if (exercise1.isCompleted() != exercise2.isCompleted()) {
            return true;
        }
        if (!exercise1.getExerciseId().equals(exercise2.getExerciseId())) {
            return true;
        }
        if (exercise1.getWeight() != exercise2.getWeight()) {
            return true;
        }
        if (exercise1.getSets() != exercise2.getSets()) {
            return true;
        }
        if (exercise1.getReps() != exercise2.getReps()) {
            return true;
        }

        if (exercise1.getDetails() == null && exercise2.getDetails() != null) {
            return true;
        } else if (exercise1.getDetails() != null && exercise2.getDetails() == null) {
            return true;
        } else {
            return exercise1.getDetails() != null && !exercise1.getDetails().equals(exercise2.getDetails());
        }
    }
}