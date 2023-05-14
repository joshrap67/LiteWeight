package com.joshrap.liteweight.repositories.workouts.requests;

import com.joshrap.liteweight.models.workout.Routine;
import com.joshrap.liteweight.models.workout.RoutineDay;
import com.joshrap.liteweight.models.workout.RoutineExercise;
import com.joshrap.liteweight.models.workout.RoutineWeek;
import com.joshrap.liteweight.repositories.BodyRequest;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SetRoutineRequest extends BodyRequest {

    private List<SetRoutineWeekRequest> weeks = new ArrayList<>();

    public SetRoutineRequest(Routine routine) {
        this.weeks = new ArrayList<>();
        for (RoutineWeek week : routine) {
            SetRoutineWeekRequest weekRequest = new SetRoutineWeekRequest();
            for (RoutineDay routineDay : week) {
                SetRoutineDayRequest routineDayRequest = new SetRoutineDayRequest();
                routineDayRequest.setTag(routineDay.getTag());
                for (RoutineExercise routineExercise : routineDay) {
                    SetRoutineExerciseRequest setRoutineExerciseRequest = new SetRoutineExerciseRequest(routineExercise);
                    routineDayRequest.getExercises().add(setRoutineExerciseRequest);
                }
                weekRequest.getDays().add(routineDayRequest);
            }
            this.weeks.add(weekRequest);
        }
    }
}
