package com.joshrap.liteweight.models.sharedWorkout;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SharedWorkoutDistinctExercise {

    private String exerciseName;
    private List<String> focuses = new ArrayList<>();
    private String videoUrl;
}
