package com.joshrap.liteweight.models.notifications;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewFriendRequestNotification {

    private String userId;
    private String username;
    private String icon;
    private String requestTimeStamp;
}
