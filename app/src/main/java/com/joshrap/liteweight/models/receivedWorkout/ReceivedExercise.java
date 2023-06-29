package com.joshrap.liteweight.models.receivedWorkout;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReceivedExercise {

    private String exerciseName;
    private Double weight;
    private Integer sets;
    private Integer reps;
    private String details;
}
