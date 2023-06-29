package com.joshrap.liteweight.models.receivedWorkout;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReceivedWorkout {

    private String id;
    private String workoutName;
    private String senderId;
    private String senderUsername;
    private String recipientId;
    private ReceivedRoutine routine;
    private List<ReceivedWorkoutDistinctExercise> distinctExercises = new ArrayList<>();
}
