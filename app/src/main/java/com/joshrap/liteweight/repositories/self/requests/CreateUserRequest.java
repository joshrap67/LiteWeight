package com.joshrap.liteweight.repositories.self.requests;

import com.joshrap.liteweight.repositories.BodyRequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CreateUserRequest extends BodyRequest {

    private String username;
    private byte[] profilePictureData;
    private boolean metricUnits;
}
