package com.joshrap.liteweight.messages.fragmentmessages;

import lombok.Getter;

public class RemovedFriendFragmentMessage {

    @Getter
    private final String userIdToRemove; // user who removed the active user

    public RemovedFriendFragmentMessage(String userIdToRemove) {
        this.userIdToRemove = userIdToRemove;
    }
}
