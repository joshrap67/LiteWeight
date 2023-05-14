package com.joshrap.liteweight.repositories.sharedWorkouts.responses;

import com.joshrap.liteweight.models.user.OwnedExercise;
import com.joshrap.liteweight.models.user.WorkoutInfo;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AcceptWorkoutResponse {

    private WorkoutInfo newWorkoutInfo;
    private List<OwnedExercise> newExercises;
}
