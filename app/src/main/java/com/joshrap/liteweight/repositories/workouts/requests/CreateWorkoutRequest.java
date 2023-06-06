package com.joshrap.liteweight.repositories.workouts.requests;

import com.joshrap.liteweight.models.workout.Routine;
import com.joshrap.liteweight.repositories.BodyRequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CreateWorkoutRequest extends BodyRequest {

    private String name;
    private SetRoutineRequest routine;
    private boolean setAsCurrentWorkout;

    public CreateWorkoutRequest(String name, Routine routine, boolean setAsCurrentWorkout) {
        this.name = name;
        this.routine = new SetRoutineRequest(routine);
        this.setAsCurrentWorkout = setAsCurrentWorkout;
    }
}
