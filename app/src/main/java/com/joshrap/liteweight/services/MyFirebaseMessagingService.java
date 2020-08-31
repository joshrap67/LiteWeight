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
import com.joshrap.liteweight.models.PushNotification;

import java.io.IOException;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        try {
            Map<String, Object> jsonMap = JsonParser.deserialize(remoteMessage.getData().get("metadata"));
            PushNotification pushNotification = new PushNotification(jsonMap);
            showNotificationFriendRequest(pushNotification.getAction(), "test");
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onMessageReceived(remoteMessage);
    }


    private void showNotificationFriendRequest(String title, String body) {
        // todo do this same thing for the timer notifications
        String username = "joe";
        Intent notificationIntent = new Intent(this, NotificationActivity.class);
        notificationIntent.putExtra(Variables.INTENT_FRIEND_REQUEST_DATA, "fuck"); // todo put actual data
        notificationIntent.setAction(Variables.INTENT_FRIEND_REQUEST_CLICK);
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new NotificationCompat.Builder(this, Variables.FRIEND_REQUEST_CHANNEL)
                .setContentTitle("Friend Request")
                .setContentText("Joe mama wants to be your friend! Click to respond.")
                .setSmallIcon(R.drawable.notification_icon)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setOngoing(false)
                .build();
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null) {
            mNotificationManager.notify(username.hashCode(), notification);
        }
    }
}
