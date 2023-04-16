package com.joshrap.liteweight.messages.activitymessages;

import lombok.Getter;

public class AcceptedFriendRequestMessage {

    @Getter
    private final String acceptedUsername;

    public AcceptedFriendRequestMessage(String acceptedUsername) {
        this.acceptedUsername = acceptedUsername;
    }
}
