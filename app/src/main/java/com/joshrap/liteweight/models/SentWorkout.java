package com.joshrap.liteweight.models;

import com.joshrap.liteweight.interfaces.Model;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SentWorkout implements Model {

    public static final String SENT_WORKOUT_ID = "sentWorkoutId";
    public static final String WORKOUT_NAME = "workoutName";
    public static final String CREATOR = "creator";
    public static final String ROUTINE = "routine";

    private String sentWorkoutId;
    private String workoutName;
    private String creator;
    private SentRoutine routine;

    public SentWorkout(Map<String, Object> json) {
        this.sentWorkoutId = (String) json.get(SENT_WORKOUT_ID);
        this.workoutName = (String) json.get(WORKOUT_NAME);
        this.creator = (String) json.get(CREATOR);
        this.routine = new SentRoutine((Map<String, Object>) json.get(ROUTINE));
    }


    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> retVal = new HashMap<>();
        retVal.put(WORKOUT_NAME, this.workoutName);
        retVal.put(SENT_WORKOUT_ID, this.sentWorkoutId);
        retVal.put(CREATOR, this.creator);
        retVal.put(ROUTINE, this.routine.asMap());
        return retVal;
    }
}
