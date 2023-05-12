package com.joshrap.liteweight.repositories.workouts.requests;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class SetRoutineDayRequest {

    public String tag;
    public List<SetRoutineExerciseRequest> exercises;

    public SetRoutineDayRequest() {
        this.exercises = new ArrayList<>(); // todo better to just initialize field above?
    }
}
