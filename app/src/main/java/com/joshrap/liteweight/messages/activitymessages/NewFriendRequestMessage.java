package com.joshrap.liteweight.messages.activitymessages;

import com.joshrap.liteweight.models.FriendRequest;

import lombok.Getter;

public class NewFriendRequestMessage {

    @Getter
    private final FriendRequest friendRequest;

    public NewFriendRequestMessage(FriendRequest friendRequest) {
        this.friendRequest = friendRequest;
    }
}
