package com.joshrap.liteweight.models;

import com.joshrap.liteweight.interfaces.Model;
import com.joshrap.liteweight.network.RequestFields;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
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
    private String defaultDetails;
    private String videoUrl;
    @Setter(AccessLevel.NONE)
    private List<String> focuses;
    @Setter(AccessLevel.NONE)
    private Map<String, String> workouts; // id to workout name that this exercise is apart of


    public ExerciseUser(Map<String, Object> json, String exerciseId) {
        this.exerciseName = (String) json.get(EXERCISE_NAME);
        this.defaultExercise = (boolean) json.get(DEFAULT_EXERCISE);
        this.defaultWeight = (double) json.get(DEFAULT_WEIGHT);
        this.defaultSets = (int) json.get(DEFAULT_SETS);
        this.exerciseId = exerciseId;
        this.defaultReps = (int) json.get(DEFAULT_REPS);
        this.defaultDetails = (String) json.get(DEFAULT_DETAILS);
        this.videoUrl = (String) json.get(VIDEO_URL);
        this.setWorkouts((Map<String, Object>) json.get(User.WORKOUTS));
        this.focuses = (List<String>) json.get(FOCUSES);
    }

    public ExerciseUser(Map<String, Object> json) {
        this.exerciseId = (String) json.get(RequestFields.EXERCISE_ID);
        Map<String, Object> exerciseJson = (Map<String, Object>) json.get(RequestFields.EXERCISE);

        this.exerciseName = (String) exerciseJson.get(EXERCISE_NAME);
        this.defaultExercise = (boolean) exerciseJson.get(DEFAULT_EXERCISE);
        this.defaultWeight = (double) exerciseJson.get(DEFAULT_WEIGHT);
        this.defaultSets = (int) exerciseJson.get(DEFAULT_SETS);
        this.defaultReps = (int) exerciseJson.get(DEFAULT_REPS);
        this.defaultDetails = (String) exerciseJson.get(DEFAULT_DETAILS);
        this.videoUrl = (String) exerciseJson.get(VIDEO_URL);
        this.setWorkouts((Map<String, Object>) exerciseJson.get(User.WORKOUTS));
        this.focuses = (List<String>) exerciseJson.get(FOCUSES);
    }

    public static ExerciseUser getExerciseForUpdate(ExerciseUser toBeCopied) {
        /*
            Used when updating an exercise to the backend
         */
        ExerciseUser exerciseUser = new ExerciseUser();
        exerciseUser.exerciseName = toBeCopied.getExerciseName();
        exerciseUser.defaultExercise = toBeCopied.isDefaultExercise();
        exerciseUser.defaultWeight = toBeCopied.getDefaultWeight();
        exerciseUser.defaultSets = toBeCopied.getDefaultSets();
        exerciseUser.defaultReps = toBeCopied.getDefaultReps();
        exerciseUser.defaultDetails = toBeCopied.getDefaultDetails();
        exerciseUser.videoUrl = toBeCopied.getVideoUrl();
        exerciseUser.workouts = new HashMap<>(toBeCopied.getWorkouts());
        exerciseUser.focuses = new ArrayList<>(toBeCopied.getFocuses());

        return exerciseUser;
    }

    private void setWorkouts(Map<String, Object> json) {
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
        retVal.put(DEFAULT_DETAILS, this.defaultDetails);
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

