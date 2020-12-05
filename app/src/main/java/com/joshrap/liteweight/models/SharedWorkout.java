package com.joshrap.liteweight.models;

import com.joshrap.liteweight.interfaces.Model;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SharedWorkout implements Model {

    public static final String SHARED_WORKOUT_ID = "sharedWorkoutId";
    public static final String WORKOUT_NAME = "workoutName";
    public static final String CREATOR = "creator";
    public static final String ROUTINE = "routine";

    private String sharedWorkoutId;
    private String workoutName;
    private String creator;
    private SharedRoutine routine;

    public SharedWorkout(Map<String, Object> json) {
        this.sharedWorkoutId = (String) json.get(SHARED_WORKOUT_ID);
        this.workoutName = (String) json.get(WORKOUT_NAME);
        this.creator = (String) json.get(CREATOR);
        this.routine = new SharedRoutine((Map<String, Object>) json.get(ROUTINE));
    }

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> retVal = new HashMap<>();
        retVal.put(WORKOUT_NAME, this.workoutName);
        retVal.put(SHARED_WORKOUT_ID, this.sharedWorkoutId);
        retVal.put(CREATOR, this.creator);
        retVal.put(ROUTINE, this.routine.asMap());
        return retVal;
    }
}
