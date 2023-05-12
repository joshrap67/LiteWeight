package com.joshrap.liteweight.messages.fragmentmessages;

import com.joshrap.liteweight.models.user.SharedWorkoutInfo;

import lombok.Getter;

public class ReceivedWorkoutFragmentMessage {

    @Getter
    private final SharedWorkoutInfo sharedWorkoutInfo;

    public ReceivedWorkoutFragmentMessage(SharedWorkoutInfo sharedWorkoutInfo) {
        this.sharedWorkoutInfo = sharedWorkoutInfo;
    }
}
