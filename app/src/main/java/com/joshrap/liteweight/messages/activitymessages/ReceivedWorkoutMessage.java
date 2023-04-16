package com.joshrap.liteweight.messages.activitymessages;

import com.joshrap.liteweight.models.SharedWorkoutMeta;

import lombok.Getter;

public class ReceivedWorkoutMessage {

    @Getter
    private final SharedWorkoutMeta sharedWorkoutMeta;

    public ReceivedWorkoutMessage(SharedWorkoutMeta sharedWorkoutMeta) {
        this.sharedWorkoutMeta = sharedWorkoutMeta;
    }
}
