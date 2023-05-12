package com.joshrap.liteweight.messages.fragmentmessages;

import lombok.Getter;

public class DeclinedFriendRequestFragmentMessage {

    @Getter
    private final String userIdToRemove; // user who declined the friend request

    public DeclinedFriendRequestFragmentMessage(String userIdToRemove) {
        this.userIdToRemove = userIdToRemove;
    }
}
