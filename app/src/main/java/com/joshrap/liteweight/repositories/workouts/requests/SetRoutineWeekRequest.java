package com.joshrap.liteweight.repositories.workouts.requests;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
public class SetRoutineWeekRequest {

    public List<SetRoutineDayRequest> days;

    public SetRoutineWeekRequest(){
        this.days = new ArrayList<>();
    }
}
