package com.joshrap.liteweight.messages.fragmentmessages;

import lombok.Getter;

public class DeclinedFriendRequestFragmentMessage {

    @Getter
    private final String usernameToRemove; // user who declined the friend request

    public DeclinedFriendRequestFragmentMessage(String usernameToRemove) {
        this.usernameToRemove = usernameToRemove;
    }
}
