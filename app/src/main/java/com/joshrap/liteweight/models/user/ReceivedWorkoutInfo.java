package com.joshrap.liteweight.models.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReceivedWorkoutInfo {

    private String receivedWorkoutId;
    private String workoutName;
    private String receivedUtc;
    private boolean seen;
    private String senderId;
    private String senderUsername;
    private String senderProfilePicture;
    private Integer totalDays;
    private String mostFrequentFocus;
}
