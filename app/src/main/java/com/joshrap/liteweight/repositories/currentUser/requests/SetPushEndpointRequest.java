package com.joshrap.liteweight.repositories.currentUser.requests;

import com.joshrap.liteweight.repositories.BodyRequest;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SetPushEndpointRequest extends BodyRequest {

    public String firebaseToken;
}
