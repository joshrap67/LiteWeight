package com.joshrap.liteweight.models;

import com.joshrap.liteweight.interfaces.Model;
import java.util.Map;

public class Workout implements Model {

    public static final String WORKOUT_ID = "workoutId";
    public static final String WORKOUT_NAME = "workoutName";
    public static final String CREATION_DATE = "creationDate";
    public static final String MOST_FREQUENT_FOCUS = "mostFrequentFocus";
    public static final String WORKOUT_TYPE = "workoutType";
    public static final String TOTAL_DAYS = "totalDays";
    public static final String CREATOR = "creator";
    public static final String ROUTINE = "routine";


    public Workout(Map<String, Object> json) {

    }

    @Override
    public Map<String, Object> asMap() {
        return null;
    }
}
