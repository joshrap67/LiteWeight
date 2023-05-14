package com.joshrap.liteweight.repositories.sharedWorkouts.requests;

import com.joshrap.liteweight.repositories.BodyRequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AcceptSharedWorkoutRequest extends BodyRequest {

    private String newName;
}
