package com.joshrap.liteweight.messages.activitymessages;

import lombok.Getter;

public class CanceledFriendRequestMessage {

    @Getter
    private final String usernameToRemove; // user who canceled the friend request

    public CanceledFriendRequestMessage(String usernameToRemove) {
        this.usernameToRemove = usernameToRemove;
    }
}
