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
public class RestartWorkoutRequest extends BodyRequest {

    private SetRoutineRequest routine;

    public RestartWorkoutRequest(Routine routine) {
        this.routine = new SetRoutineRequest(routine);
    }
}
