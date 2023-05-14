package com.joshrap.liteweight.repositories.workouts.requests;

import com.joshrap.liteweight.models.workout.RoutineExercise;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SetRoutineExerciseRequest {
    private boolean completed;
    private String exerciseId;
    private double weight;
    private int sets;
    private int reps;
    private String details;

    public SetRoutineExerciseRequest(RoutineExercise routineExercise) {
        this.completed = routineExercise.isCompleted();
        this.exerciseId = routineExercise.getExerciseId();
        this.weight = routineExercise.getWeight();
        this.sets = routineExercise.getSets();
        this.reps = routineExercise.getReps();
        this.details = routineExercise.getDetails();
    }
}
