package com.joshrap.liteweight.models.receivedWorkout;


import com.joshrap.liteweight.models.user.Link;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReceivedWorkoutDistinctExercise {

    private String exerciseName;
    private String notes;
    private List<String> focuses = new ArrayList<>();
    private List<Link> links = new ArrayList<>();
}
