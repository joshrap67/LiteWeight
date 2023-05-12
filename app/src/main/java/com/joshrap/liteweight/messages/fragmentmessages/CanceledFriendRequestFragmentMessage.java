package com.joshrap.liteweight.messages.fragmentmessages;

import lombok.Getter;

public class CanceledFriendRequestFragmentMessage {

    @Getter
    private final String userIdToRemove; // user who canceled the friend request

    public CanceledFriendRequestFragmentMessage(String userIdToRemove) {
        this.userIdToRemove = userIdToRemove;
    }
}
