package com.joshrap.liteweight.models.sharedWorkout;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SharedExercise {

    private String exerciseName;
    private Double weight;
    private Integer sets;
    private Integer reps;
    private String details;
}
