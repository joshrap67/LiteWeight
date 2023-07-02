package com.joshrap.liteweight.repositories.workouts.requests;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SetRoutineDayRequest {

    private String tag;
    private List<SetRoutineExerciseRequest> exercises = new ArrayList<>();
}
