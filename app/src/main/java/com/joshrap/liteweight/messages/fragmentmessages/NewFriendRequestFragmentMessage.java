package com.joshrap.liteweight.messages.fragmentmessages;

import com.joshrap.liteweight.models.FriendRequest;

import lombok.Getter;

public class NewFriendRequestFragmentMessage {

    @Getter
    private final FriendRequest friendRequest;

    public NewFriendRequestFragmentMessage(FriendRequest friendRequest) {
        this.friendRequest = friendRequest;
    }
}
