package com.joshrap.liteweight.repositories.users.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchByUsernameResponse {

    private String id;
    private String username;
    private String profilePicture;
}
