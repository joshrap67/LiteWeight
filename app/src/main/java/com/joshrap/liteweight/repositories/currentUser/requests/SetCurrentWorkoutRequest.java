package com.joshrap.liteweight.repositories.currentUser.requests;

import com.joshrap.liteweight.repositories.BodyRequest;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SetCurrentWorkoutRequest extends BodyRequest {

    public String workoutId;
}
