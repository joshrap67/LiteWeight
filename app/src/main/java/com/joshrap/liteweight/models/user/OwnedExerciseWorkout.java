package com.joshrap.liteweight.models.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OwnedExerciseWorkout {

    private String workoutId;
    private String workoutName;

    public OwnedExerciseWorkout(OwnedExerciseWorkout ownedExerciseWorkout){
        this.workoutId = ownedExerciseWorkout.getWorkoutId();
        this.workoutName = ownedExerciseWorkout.getWorkoutName();
    }
}
