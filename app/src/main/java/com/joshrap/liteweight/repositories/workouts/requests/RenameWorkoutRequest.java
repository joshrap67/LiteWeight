package com.joshrap.liteweight.repositories.workouts.requests;

import com.joshrap.liteweight.repositories.BodyRequest;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class RenameWorkoutRequest extends BodyRequest {

    public String newName;
}
