package com.joshrap.liteweight.models;

import com.joshrap.liteweight.interfaces.Model;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class SharedExercise implements Model {

    public static final String EXERCISE_NAME = "exerciseName";
    public static final String WEIGHT = "weight";
    public static final String SETS = "sets";
    public static final String REPS = "reps";
    public static final String DETAILS = "details";

    private String exerciseName;
    private Double weight;
    private Integer sets;
    private Integer reps;
    private String details;

    SharedExercise(final Map<String, Object> json) {
        this.exerciseName = (String) json.get(EXERCISE_NAME);
        this.weight = (Double) json.get(WEIGHT);
        this.sets = (Integer) json.get(SETS);
        this.reps = (Integer) json.get(REPS);
        this.details = (String) json.get(DETAILS);
    }

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> retVal = new HashMap<>();
        retVal.put(WEIGHT, this.weight);
        retVal.put(EXERCISE_NAME, this.exerciseName);
        retVal.put(SETS, this.sets);
        retVal.put(REPS, this.reps);
        retVal.put(DETAILS, this.details);
        return retVal;
    }
}
