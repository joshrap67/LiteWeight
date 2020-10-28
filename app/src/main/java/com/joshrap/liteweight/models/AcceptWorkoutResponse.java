package com.joshrap.liteweight.models;

import com.joshrap.liteweight.interfaces.Model;
import com.joshrap.liteweight.network.RequestFields;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AcceptWorkoutResponse implements Model {

    private String workoutId;
    private WorkoutMeta workoutMeta;
    private Workout workout;

    public AcceptWorkoutResponse(Map<String, Object> json) {
        this.workoutId = (String) json.get(Workout.WORKOUT_ID);
        this.workoutMeta = new WorkoutMeta((Map<String, Object>) json.get(RequestFields.WORKOUT_META), workoutId);
        this.workout = new Workout((Map<String, Object>) json.get(RequestFields.WORKOUT));
    }

    @Override
    public Map<String, Object> asMap() {
        Map<String, Object> retVal = new HashMap<>();
        retVal.put(Workout.WORKOUT_ID, this.workoutId);
        retVal.put(RequestFields.WORKOUT_META, this.workoutMeta.asMap());
        retVal.put(RequestFields.WORKOUT, this.workout.asMap());
        return retVal;
    }
}
