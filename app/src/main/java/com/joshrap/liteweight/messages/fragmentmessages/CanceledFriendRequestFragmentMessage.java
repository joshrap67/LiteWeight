package com.joshrap.liteweight.messages.fragmentmessages;

import lombok.Getter;

public class CanceledFriendRequestFragmentMessage {

    @Getter
    private final String usernameToRemove; // user who canceled the friend request

    public CanceledFriendRequestFragmentMessage(String usernameToRemove) {
        this.usernameToRemove = usernameToRemove;
    }
}
