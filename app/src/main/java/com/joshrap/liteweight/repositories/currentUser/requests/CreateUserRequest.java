package com.joshrap.liteweight.repositories.currentUser.requests;

import com.joshrap.liteweight.repositories.BodyRequest;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CreateUserRequest extends BodyRequest {

    public String Username;
    public boolean metricUnits;
}
