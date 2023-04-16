package com.joshrap.liteweight.messages.fragmentmessages;

import lombok.Getter;

public class AcceptedFriendRequestFragmentMessage {

    @Getter
    private final String acceptedUsername;

    public AcceptedFriendRequestFragmentMessage(String acceptedUsername) {
        this.acceptedUsername = acceptedUsername;
    }
}