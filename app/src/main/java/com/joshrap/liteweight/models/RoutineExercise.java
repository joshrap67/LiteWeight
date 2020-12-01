package com.joshrap.liteweight.models;

import com.joshrap.liteweight.interfaces.Model;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class RoutineExercise implements Model, Cloneable {

    public static final String COMPLETED = "completed";
    public static final String EXERCISE_ID = "exerciseId";
    public static final String WEIGHT = "weight";
    public static final String SETS = "sets";
    public static final String REPS = "reps";
    public static final String DETAILS = "details";

    private boolean completed;
    private String exerciseId;
    private double weight;
    private int sets;
    private int reps;
    private String details;

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

    public RoutineExercise(Map<String, Object> json) {
        this.completed = (boolean) json.get(COMPLETED);
        this.exerciseId = (String) json.get(EXERCISE_ID);
        this.weight = (double) json.get(WEIGHT);
        this.sets = (int) json.get(SETS);
        this.reps = (int) json.get(REPS);
        this.details = (String) json.get(DETAILS);
    }

    public RoutineExercise(OwnedExercise ownedExercise, String exerciseId) {
        this.completed = false;
        this.exerciseId = exerciseId;
        this.weight = ownedExercise.getDefaultWeight();
        this.sets = ownedExercise.getDefaultSets();
        this.reps = ownedExercise.getDefaultReps();
        this.details = ownedExercise.getDefaultDetails();
    }

    static boolean exercisesDifferent(RoutineExercise exercise1, RoutineExercise exercise2) {
        boolean retVal = false;
        if (exercise1.isCompleted() != exercise2.isCompleted()) {
            retVal = true;
        }
        if (!exercise1.getExerciseId().equals(exercise2.getExerciseId())) {
            retVal = true;
        }
        if (exercise1.getWeight() != exercise2.getWeight()) {
            retVal = true;
        }
        if (exercise1.getSets() != exercise2.getSets()) {
            retVal = true;
        }
        if (exercise1.getReps() != exercise2.getReps()) {
            retVal = true;
        }
        if (!exercise1.getDetails().equals(exercise2.getDetails())) {
            retVal = true;
        }
        return retVal;
    }

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> retVal = new HashMap<>();
        retVal.put(COMPLETED, this.completed);
        retVal.put(EXERCISE_ID, this.exerciseId);
        retVal.put(WEIGHT, this.weight);
        retVal.put(SETS, this.sets);
        retVal.put(REPS, this.reps);
        retVal.put(DETAILS, this.details);
        return retVal;
    }
}