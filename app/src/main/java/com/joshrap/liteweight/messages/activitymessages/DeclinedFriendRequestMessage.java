package com.joshrap.liteweight.messages.activitymessages;

import lombok.Getter;

public class DeclinedFriendRequestMessage {

    @Getter
    private final String userIdToRemove; // user who declined the friend request

    public DeclinedFriendRequestMessage(String userIdToRemove) {
        this.userIdToRemove = userIdToRemove;
    }
}
