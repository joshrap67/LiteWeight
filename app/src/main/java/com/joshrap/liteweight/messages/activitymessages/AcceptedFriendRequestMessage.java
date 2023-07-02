package com.joshrap.liteweight.messages.activitymessages;

import lombok.Getter;

public class AcceptedFriendRequestMessage {

    @Getter
    private final String acceptedUserId;

    public AcceptedFriendRequestMessage(String acceptedUserId) {
        this.acceptedUserId = acceptedUserId;
    }
}
