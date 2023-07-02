package com.joshrap.liteweight.messages.fragmentmessages;

import com.joshrap.liteweight.models.user.ReceivedWorkoutInfo;

import lombok.Getter;

public class ReceivedWorkoutFragmentMessage {

    @Getter
    private final ReceivedWorkoutInfo receivedWorkoutInfo;

    public ReceivedWorkoutFragmentMessage(ReceivedWorkoutInfo receivedWorkoutInfo) {
        this.receivedWorkoutInfo = receivedWorkoutInfo;
    }
}
