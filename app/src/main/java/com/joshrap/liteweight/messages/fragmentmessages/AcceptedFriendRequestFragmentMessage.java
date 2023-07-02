package com.joshrap.liteweight.messages.fragmentmessages;

import lombok.Getter;

public class AcceptedFriendRequestFragmentMessage {

    @Getter
    private final String acceptedUserId;

    public AcceptedFriendRequestFragmentMessage(String acceptedUserId) {
        this.acceptedUserId = acceptedUserId;
    }
}