package com.joshrap.liteweight.models.notifications;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AcceptedFriendRequestNotification {

    private String userId;
    private String username;
}
