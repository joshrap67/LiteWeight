package com.joshrap.liteweight.messages.activitymessages;

import com.joshrap.liteweight.models.user.FriendRequest;

import lombok.Getter;

@Getter
public class NewFriendRequestMessage {

    private final FriendRequest friendRequest;

    public NewFriendRequestMessage(FriendRequest friendRequest) {
        this.friendRequest = friendRequest;
    }
}
