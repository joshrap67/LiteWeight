package com.joshrap.liteweight.messages.activitymessages;

import com.joshrap.liteweight.models.user.ReceivedWorkoutInfo;

import lombok.Getter;

@Getter
public class ReceivedWorkoutMessage {

    private final ReceivedWorkoutInfo receivedWorkoutInfo;

    public ReceivedWorkoutMessage(ReceivedWorkoutInfo receivedWorkoutInfo) {
        this.receivedWorkoutInfo = receivedWorkoutInfo;
    }
}
