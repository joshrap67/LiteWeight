package com.joshrap.liteweight.models;

import com.joshrap.liteweight.interfaces.Model;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class ExerciseRoutine implements Model {

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

    public ExerciseRoutine(Map<String, Object> json) {
        this.completed = (boolean) json.get(COMPLETED);
        this.exerciseId = (String) json.get(EXERCISE_ID);
        this.weight = (double) json.get(WEIGHT);
        this.sets = (int) json.get(SETS);
        this.reps = (int) json.get(REPS);
        this.details = (String) json.get(DETAILS);
    }

    public ExerciseRoutine(ExerciseUser exerciseUser, String exerciseId) {
        this.completed = false;
        this.exerciseId = exerciseId;
        this.weight = exerciseUser.getDefaultWeight();
        this.sets = exerciseUser.getDefaultSets();
        this.reps = exerciseUser.getDefaultReps();
        this.details = exerciseUser.getDefaultNote();
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