package com.joshrap.liteweight.messages.fragmentmessages;

import lombok.Getter;

@Getter
public class RemovedFriendFragmentMessage {

    private final String userIdToRemove; // user who removed the active user

    public RemovedFriendFragmentMessage(String userIdToRemove) {
        this.userIdToRemove = userIdToRemove;
    }
}
