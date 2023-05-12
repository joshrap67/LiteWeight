package com.joshrap.liteweight.models.sharedWorkout;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SharedWorkout {

    private String id;
    private String workoutName;
    private String senderId;
    private String senderUsername;
    private String recipientId;
    private SharedRoutine routine;
    private List<SharedWorkoutDistinctExercise> distinctExercises = new ArrayList<>();
}
