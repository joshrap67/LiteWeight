package com.joshrap.liteweight.repositories.workouts.requests;

import com.joshrap.liteweight.models.workout.Routine;
import com.joshrap.liteweight.models.workout.RoutineDay;
import com.joshrap.liteweight.models.workout.RoutineExercise;
import com.joshrap.liteweight.models.workout.RoutineWeek;
import com.joshrap.liteweight.repositories.BodyRequest;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class SetRoutineRequest extends BodyRequest {

    public List<SetRoutineWeekRequest> weeks;

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
                weekRequest.days.add(routineDayRequest);
            }
            this.weeks.add(weekRequest);
        }
    }
}
