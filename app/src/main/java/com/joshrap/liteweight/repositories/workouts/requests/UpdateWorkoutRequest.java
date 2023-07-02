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
public class UpdateWorkoutRequest extends BodyRequest {

    private int currentWeek;
    private int currentDay;
    private SetRoutineRequest routine;

    public UpdateWorkoutRequest(int currentWeek, int currentDay, Routine routine) {
        this.currentWeek = currentWeek;
        this.currentDay = currentDay;
        this.routine = new SetRoutineRequest(routine);
    }
}
