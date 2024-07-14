package com.joshrap.liteweight.messages.fragmentmessages;

import com.joshrap.liteweight.models.user.FriendRequest;

import lombok.Getter;

@Getter
public class NewFriendRequestFragmentMessage {

    private final FriendRequest friendRequest;

    public NewFriendRequestFragmentMessage(FriendRequest friendRequest) {
        this.friendRequest = friendRequest;
    }
}
