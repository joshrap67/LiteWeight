package com.joshrap.liteweight.models.receivedWorkout;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReceivedExercise {

    private String exerciseName;
    private double weight;
    private int sets;
    private int reps;
    private String instructions;
}
