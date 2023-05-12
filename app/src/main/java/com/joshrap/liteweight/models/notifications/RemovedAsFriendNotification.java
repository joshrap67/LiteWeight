package com.joshrap.liteweight.models.notifications;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RemovedAsFriendNotification {

    private String userId;
    private String username;
}
