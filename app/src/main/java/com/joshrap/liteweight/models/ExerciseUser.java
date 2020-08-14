package com.joshrap.liteweight.models;

import com.joshrap.liteweight.interfaces.Model;

import java.util.HashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
public class ExerciseUser implements Model, Comparable<ExerciseUser> {

    public static final String EXERCISE_NAME = "exerciseName";
    public static final String FOCUSES = "focuses";
    public static final String DEFAULT_EXERCISE = "defaultExercise";
    public static final String DEFAULT_WEIGHT = "defaultWeight";
    public static final String DEFAULT_SETS = "defaultSets";
    public static final String DEFAULT_REPS = "defaultReps";
    public static final String DEFAULT_DETAILS = "defaultDetails";
    public static final String VIDEO_URL = "videoUrl";

    private String exerciseName;
    private boolean defaultExercise;
    private double defaultWeight; // stored in lbs
    private int defaultSets;
    private int defaultReps;
    private String exerciseId; // NOT EVER SENT BACK TO BACKEND, IS A VAR TO MAKE THINGS EASIER
    private String defaultNote;
    private String videoUrl;
    @Setter(AccessLevel.NONE)
    private Map<String, Boolean> focuses;
    @Setter(AccessLevel.NONE)
    private Map<String, String> workouts; // id to workout name that this exercise is apart of


    public ExerciseUser(Map<String, Object> json, String exerciseId) {
        this.exerciseName = (String) json.get(EXERCISE_NAME);
        this.defaultExercise = (boolean) json.get(DEFAULT_EXERCISE);
        this.defaultWeight = (double) json.get(DEFAULT_WEIGHT);
        this.defaultSets = (int) json.get(DEFAULT_SETS);
        this.exerciseId = exerciseId;
        this.defaultReps = (int) json.get(DEFAULT_REPS);
        this.defaultNote = (String) json.get(DEFAULT_DETAILS);
        this.videoUrl = (String) json.get(VIDEO_URL);
        this.setWorkouts((Map<String, Object>) json.get(User.WORKOUTS));
        this.setFocuses((Map<String, Object>) json.get(FOCUSES));
    }

    public void setFocuses(Map<String, Object> json) {
        if (json == null) {
            this.focuses = null;
        } else {
            this.focuses = new HashMap<>();
            for (String focusName : json.keySet()) {
                this.focuses.put(focusName, true);
            }
        }
    }

    public void setWorkouts(Map<String, Object> json) {
        if (json == null) {
            this.workouts = null;
        } else {
            this.workouts = new HashMap<>();
            for (String workoutId : json.keySet()) {
                this.workouts.put(workoutId, (String) json.get(workoutId));
            }
        }
    }

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> retVal = new HashMap<>();
        retVal.put(EXERCISE_NAME, this.exerciseName);
        retVal.put(DEFAULT_EXERCISE, this.defaultExercise);
        retVal.put(DEFAULT_WEIGHT, this.defaultWeight);
        retVal.put(DEFAULT_REPS, this.defaultReps);
        retVal.put(DEFAULT_SETS, this.defaultSets);
        retVal.put(DEFAULT_DETAILS, this.defaultNote);
        retVal.put(VIDEO_URL, this.videoUrl);
        retVal.put(FOCUSES, this.focuses);
        retVal.put(User.WORKOUTS, this.workouts);
        return retVal;
    }

    @Override
    public int compareTo(ExerciseUser o) {
        return this.getExerciseName().compareTo(o.getExerciseName());
    }
}

