package com.joshrap.liteweight.messages.activitymessages;

import lombok.Getter;

public class DeclinedFriendRequestMessage {

    @Getter
    private final String usernameToRemove; // user who declined the friend request

    public DeclinedFriendRequestMessage(String usernameToRemove) {
        this.usernameToRemove = usernameToRemove;
    }
}
