package com.joshrap.liteweight.repositories.workouts.requests;

import com.joshrap.liteweight.models.workout.Routine;
import com.joshrap.liteweight.repositories.BodyRequest;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class CreateWorkoutRequest extends BodyRequest {

    public String workoutName;
    public SetRoutineRequest routine;
    public boolean setAsCurrentWorkout;

    public CreateWorkoutRequest(String workoutName, Routine routine, boolean setAsCurrentWorkout) {
        this.workoutName = workoutName;
        this.routine = new SetRoutineRequest(routine);
        this.setAsCurrentWorkout = setAsCurrentWorkout;
    }
}
