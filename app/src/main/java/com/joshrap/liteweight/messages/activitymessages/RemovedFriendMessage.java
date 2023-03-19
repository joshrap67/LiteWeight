package com.joshrap.liteweight.messages.activitymessages;

import lombok.Getter;

public class RemovedFriendMessage {

    @Getter
    private final String usernameToRemove; // user who removed the active user

    public RemovedFriendMessage(String usernameToRemove) {
        this.usernameToRemove = usernameToRemove;
    }
}
