package com.joshrap.liteweight.messages.fragmentmessages;

import com.joshrap.liteweight.models.user.ReceivedWorkoutInfo;

import lombok.Getter;

@Getter
public class ReceivedWorkoutFragmentMessage {

    private final ReceivedWorkoutInfo receivedWorkoutInfo;

    public ReceivedWorkoutFragmentMessage(ReceivedWorkoutInfo receivedWorkoutInfo) {
        this.receivedWorkoutInfo = receivedWorkoutInfo;
    }
}
