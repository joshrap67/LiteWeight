package com.joshrap.liteweight.repositories.users.requests;

import com.joshrap.liteweight.repositories.BodyRequest;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ShareWorkoutRequest extends BodyRequest {

    private String workoutId;
    private String username;
}
