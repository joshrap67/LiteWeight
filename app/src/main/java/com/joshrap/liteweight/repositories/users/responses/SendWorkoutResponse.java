package com.joshrap.liteweight.repositories.users.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendWorkoutResponse {

    private String receivedWorkoutId;
}
