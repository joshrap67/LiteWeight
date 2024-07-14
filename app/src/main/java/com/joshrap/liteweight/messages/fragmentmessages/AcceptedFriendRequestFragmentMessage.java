package com.joshrap.liteweight.messages.fragmentmessages;

import lombok.Getter;

@Getter
public class AcceptedFriendRequestFragmentMessage {

    private final String acceptedUserId;

    public AcceptedFriendRequestFragmentMessage(String acceptedUserId) {
        this.acceptedUserId = acceptedUserId;
    }
}