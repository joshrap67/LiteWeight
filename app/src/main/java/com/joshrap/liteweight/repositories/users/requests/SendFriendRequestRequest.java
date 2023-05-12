package com.joshrap.liteweight.repositories.users.requests;

import com.joshrap.liteweight.repositories.BodyRequest;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SendFriendRequestRequest extends BodyRequest {

    private String recipientUsername;
}
