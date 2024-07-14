package com.joshrap.liteweight.messages.fragmentmessages;

import lombok.Getter;

@Getter
public class DeclinedFriendRequestFragmentMessage {

    private final String userIdToRemove; // user who declined the friend request

    public DeclinedFriendRequestFragmentMessage(String userIdToRemove) {
        this.userIdToRemove = userIdToRemove;
    }
}
