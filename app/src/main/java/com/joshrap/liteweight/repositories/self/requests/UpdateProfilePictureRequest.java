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
public class UpdateProfilePictureRequest extends BodyRequest {

    private byte[] imageData;
}
