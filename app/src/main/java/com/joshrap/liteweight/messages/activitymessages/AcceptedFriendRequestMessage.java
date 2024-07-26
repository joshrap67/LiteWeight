package com.joshrap.liteweight.messages.activitymessages;

import lombok.Getter;

@Getter
public class AcceptedFriendRequestMessage {

    private final String acceptedUserId;

    public AcceptedFriendRequestMessage(String acceptedUserId) {
        this.acceptedUserId = acceptedUserId;
    }
}
