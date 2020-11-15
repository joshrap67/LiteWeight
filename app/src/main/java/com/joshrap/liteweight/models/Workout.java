package com.joshrap.liteweight.models;

import com.joshrap.liteweight.interfaces.Model;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class Workout implements Model {

    public static final String WORKOUT_ID = "workoutId";
    public static final String WORKOUT_NAME = "workoutName";
    public static final String CREATION_DATE = "creationDate";
    public static final String MOST_FREQUENT_FOCUS = "mostFrequentFocus";
    public static final String CREATOR = "creator";
    public static final String ROUTINE = "routine";
    public static final String CURRENT_DAY = "currentDay";
    public static final String CURRENT_WEEK = "currentWeek";

    private String workoutId;
    private String workoutName;
    private String creationDate;
    private String mostFrequentFocus;
    private String creator;
    private Routine routine;
    private Integer currentDay;
    private Integer currentWeek;


    public Workout(Map<String, Object> json) {
        this.workoutId = (String) json.get(WORKOUT_ID);
        this.workoutName = (String) json.get(WORKOUT_NAME);
        this.creationDate = (String) json.get(CREATION_DATE);
        this.mostFrequentFocus = (String) json.get(MOST_FREQUENT_FOCUS);
        this.creator = (String) json.get(CREATOR);
        this.routine = new Routine((Map<String, Object>) json.get(ROUTINE));
        this.currentDay = (Integer) json.get(CURRENT_DAY);
        this.currentWeek = (Integer) json.get(CURRENT_WEEK);
    }

    public Workout(Workout toBeCopied) {
        // copy constructor
        this.workoutId = toBeCopied.getWorkoutId();
        this.workoutName = toBeCopied.getWorkoutName();
        this.creationDate = toBeCopied.getCreationDate();
        this.mostFrequentFocus = toBeCopied.getMostFrequentFocus();
        this.creator = toBeCopied.getCreator();
        this.currentDay = toBeCopied.getCurrentDay();
        this.currentWeek = toBeCopied.getCurrentWeek();
        this.routine = new Routine(toBeCopied.getRoutine());
    }

    public static boolean workoutsIdentical(Workout workout1, Workout workout2) {
        boolean retVal = true;
        if (!workout1.getCurrentWeek().equals(workout2.getCurrentWeek())) {
            retVal = false;
        }
        if (!workout1.getCurrentDay().equals(workout2.getCurrentDay())) {
            retVal = false;
        }
        if (!Routine.routinesIdentical(workout1.getRoutine(), workout2.getRoutine())) {
            retVal = false;
        }
        return retVal;
    }

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> retVal = new HashMap<>();
        retVal.put(WORKOUT_NAME, this.workoutName);
        retVal.put(WORKOUT_ID, this.workoutId);
        retVal.put(CREATION_DATE, this.creationDate);
        retVal.put(MOST_FREQUENT_FOCUS, this.mostFrequentFocus);
        retVal.put(CREATOR, this.creator);
        retVal.put(ROUTINE, this.routine.asMap());
        retVal.put(CURRENT_WEEK, this.currentWeek);
        retVal.put(CURRENT_DAY, this.currentDay);
        return retVal;
    }
}
