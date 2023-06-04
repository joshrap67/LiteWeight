package com.joshrap.liteweight.models.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkoutInfo {

    private String workoutId;
    private String workoutName;
    private String lastSetAsCurrentUtc;
    private int timesRestarted;
    private double averageExercisesCompleted;
    private Integer currentWeek;
    private Integer currentDay;

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

        return (other instanceof WorkoutInfo) && (((WorkoutInfo) other).getWorkoutId().equals(this.getWorkoutId()));
    }

    public void update(WorkoutInfo newWorkoutInfo) {
        this.workoutName = newWorkoutInfo.workoutName;
        this.lastSetAsCurrentUtc = newWorkoutInfo.lastSetAsCurrentUtc;
        this.timesRestarted = newWorkoutInfo.timesRestarted;
        this.averageExercisesCompleted = newWorkoutInfo.averageExercisesCompleted;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + workoutName.hashCode();
        result = 31 * result + lastSetAsCurrentUtc.hashCode();
        result = 31 * result + timesRestarted;
        result = (int) (31 * result + averageExercisesCompleted);
        result = 31 * result + workoutId.hashCode();
        return result;
    }
}
