package com.joshrap.liteweight.models;

import com.joshrap.liteweight.interfaces.Model;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WorkoutUser implements Model {

    public static final String WORKOUT_NAME = "workoutName";
    public static final String DATE_LAST = "dateLast";
    public static final String TIMES_COMPLETED = "timesCompleted";
    public static final String AVERAGE_EXERCISES_COMPLETED = "averageExercisesCompleted";
    public static final String TOTAL_EXERCISES_SUM = "totalExercisesSum";

    private String workoutName;
    private String dateLast;
    private Integer timesCompleted;
    private Double averageExercisesCompleted;
    private Integer totalExercisesSum;
    private String workoutId;

    WorkoutUser(Map<String, Object> json, String workoutId) {
        this.workoutId = workoutId;
        this.workoutName = (String) json.get(WORKOUT_NAME);
        this.dateLast = (String) json.get(DATE_LAST);
        this.timesCompleted = (int) json.get(TIMES_COMPLETED);
        this.averageExercisesCompleted = (Double) json.get(AVERAGE_EXERCISES_COMPLETED);
        this.totalExercisesSum = (int) json.get(TOTAL_EXERCISES_SUM);
    }

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> retVal = new HashMap<>();
        retVal.put(WORKOUT_NAME, this.workoutName);
        retVal.put(TIMES_COMPLETED, this.timesCompleted);
        retVal.put(AVERAGE_EXERCISES_COMPLETED, this.averageExercisesCompleted);
        retVal.put(TOTAL_EXERCISES_SUM, this.totalExercisesSum);
        return retVal;
    }

    @Override
    public boolean equals(Object other) {
        // null check
        if (other == null) {
            return false;
        }

        // reflexive check
        if (this == other) {
            return true;
        }

        return (other instanceof WorkoutUser) && (((WorkoutUser) other).getWorkoutId().equals(this.getWorkoutId()));
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + workoutName.hashCode();
        result = 31 * result + dateLast.hashCode();
        result = 31 * result + timesCompleted.hashCode();
        result = 31 * result + averageExercisesCompleted.hashCode();
        result = 31 * result + totalExercisesSum.hashCode();
        result = 31 * result + workoutId.hashCode();
        return result;
    }
}
