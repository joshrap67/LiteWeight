package com.joshrap.liteweight.repositories.workouts.requests;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SetRoutineWeekRequest {

    private List<SetRoutineDayRequest> days = new ArrayList<>();
}
