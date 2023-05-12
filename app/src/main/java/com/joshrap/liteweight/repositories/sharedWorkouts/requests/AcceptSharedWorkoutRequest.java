package com.joshrap.liteweight.repositories.sharedWorkouts.requests;

import com.joshrap.liteweight.repositories.BodyRequest;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AcceptSharedWorkoutRequest extends BodyRequest {

    public String newName;
}
