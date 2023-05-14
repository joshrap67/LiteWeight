package com.joshrap.liteweight.repositories.users.requests;

import com.joshrap.liteweight.repositories.BodyRequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ShareWorkoutRequest extends BodyRequest {

    private String workoutId;
    private String recipientUsername;
}
