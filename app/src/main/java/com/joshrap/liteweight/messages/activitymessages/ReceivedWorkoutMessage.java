package com.joshrap.liteweight.messages.activitymessages;

import com.joshrap.liteweight.models.user.SharedWorkoutInfo;

import lombok.Getter;

public class ReceivedWorkoutMessage {

    @Getter
    private final SharedWorkoutInfo sharedWorkoutInfo;

    public ReceivedWorkoutMessage(SharedWorkoutInfo sharedWorkoutInfo) {
        this.sharedWorkoutInfo = sharedWorkoutInfo;
    }
}
