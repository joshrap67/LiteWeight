package com.joshrap.liteweight.messages.activitymessages;

import lombok.Getter;


@Getter
public class CanceledFriendRequestMessage {

    private final String userIdToRemove; // user who canceled the friend request

    public CanceledFriendRequestMessage(String userIdToRemove) {
        this.userIdToRemove = userIdToRemove;
    }
}
