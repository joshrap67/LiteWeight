package com.joshrap.liteweight.messages.activitymessages;

import lombok.Getter;

public class RemovedFriendMessage {

    @Getter
    private final String userIdToRemove; // user who removed the active user

    public RemovedFriendMessage(String userIdToRemove) {
        this.userIdToRemove = userIdToRemove;
    }
}
