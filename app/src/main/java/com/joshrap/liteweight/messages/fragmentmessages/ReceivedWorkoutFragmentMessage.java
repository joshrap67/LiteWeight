package com.joshrap.liteweight.messages.fragmentmessages;

import com.joshrap.liteweight.models.SharedWorkoutMeta;

import lombok.Getter;

public class ReceivedWorkoutFragmentMessage {

    @Getter
    private final SharedWorkoutMeta sharedWorkoutMeta;

    public ReceivedWorkoutFragmentMessage(SharedWorkoutMeta sharedWorkoutMeta) {
        this.sharedWorkoutMeta = sharedWorkoutMeta;
    }
}
