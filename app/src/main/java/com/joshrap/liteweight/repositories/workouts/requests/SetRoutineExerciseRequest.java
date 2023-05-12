package com.joshrap.liteweight.repositories.workouts.requests;

import com.joshrap.liteweight.models.workout.RoutineExercise;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class SetRoutineExerciseRequest {
    public boolean completed;
    public String exerciseId;
    public double weight;
    public int sets;
    public int reps;
    public String details;

    public SetRoutineExerciseRequest(RoutineExercise routineExercise) {
        this.completed = routineExercise.isCompleted();
        this.exerciseId = routineExercise.getExerciseId();
        this.weight = routineExercise.getWeight();
        this.sets = routineExercise.getSets();
        this.reps = routineExercise.getReps();
        this.details = routineExercise.getDetails();
    }
}
