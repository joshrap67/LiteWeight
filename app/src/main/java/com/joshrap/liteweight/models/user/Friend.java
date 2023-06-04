package com.joshrap.liteweight.models.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Friend {

    private String userId;
    private String username;
    private String profilePicture;
    private boolean confirmed;
}
