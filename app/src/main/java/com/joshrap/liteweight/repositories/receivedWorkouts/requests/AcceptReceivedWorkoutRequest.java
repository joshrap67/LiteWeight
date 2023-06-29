package com.joshrap.liteweight.repositories.receivedWorkouts.requests;

import com.joshrap.liteweight.repositories.BodyRequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AcceptReceivedWorkoutRequest extends BodyRequest {

    private String workoutName;
}
