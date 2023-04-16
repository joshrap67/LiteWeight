package com.joshrap.liteweight.messages.fragmentmessages;

import lombok.Getter;

public class RemovedFriendFragmentMessage {

    @Getter
    private final String usernameToRemove; // user who removed the active user

    public RemovedFriendFragmentMessage(String usernameToRemove) {
        this.usernameToRemove = usernameToRemove;
    }
}
