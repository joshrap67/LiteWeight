package com.joshrap.liteweight.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.activities.NotificationActivity;
import com.joshrap.liteweight.helpers.JsonParser;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.models.FriendRequest;
import com.joshrap.liteweight.models.PushNotification;

import java.io.IOException;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        try {
            System.out.println(remoteMessage.getData());
            Map<String, Object> jsonMap = JsonParser.deserialize(remoteMessage.getData().get("metadata"));
            PushNotification pushNotification = new PushNotification(jsonMap);
            switch (pushNotification.getAction()) {
                case "friendRequest":
                    showNotificationFriendRequest(pushNotification.getJsonPayload());
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onMessageReceived(remoteMessage);
    }


    private void showNotificationFriendRequest(String jsonData) throws IOException {
        // todo broadcast to activity
        // todo do this same thing for the timer notifications
        FriendRequest friendRequest = new FriendRequest(JsonParser.deserialize(jsonData));
        Intent notificationIntent = new Intent(this, NotificationActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        notificationIntent.putExtra(Variables.INTENT_NOTIFICATION_DATA, jsonData);
        notificationIntent.setAction(Variables.INTENT_FRIEND_REQUEST_CLICK);

        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Notification notification = new NotificationCompat.Builder(this, Variables.FRIEND_REQUEST_CHANNEL)
                .setContentTitle("New Friend Request")
                .setContentText(String.format("%s wants to be your friend! Click to respond.", friendRequest.getUsername()))
                .setSmallIcon(R.drawable.notification_icon)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setOngoing(false)
                .build();
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null) {
            mNotificationManager.notify(friendRequest.getUsername().hashCode(), notification);
        }
    }
}
