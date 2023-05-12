package com.joshrap.liteweight.repositories.workouts.requests;

import com.joshrap.liteweight.models.workout.Routine;
import com.joshrap.liteweight.repositories.BodyRequest;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class RestartWorkoutRequest extends BodyRequest {
    public SetRoutineRequest routine;

    public RestartWorkoutRequest(Routine routine) {
        this.routine = new SetRoutineRequest(routine);
    }
}
