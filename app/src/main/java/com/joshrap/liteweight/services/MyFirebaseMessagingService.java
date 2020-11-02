package com.joshrap.liteweight.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.activities.NotificationActivity;
import com.joshrap.liteweight.activities.WorkoutActivity;
import com.joshrap.liteweight.helpers.JsonParser;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.models.FriendRequest;
import com.joshrap.liteweight.models.PushNotification;
import com.joshrap.liteweight.models.ReceivedWorkoutMeta;
import com.joshrap.liteweight.models.User;

import java.io.IOException;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        try {
            Map<String, Object> jsonMap = JsonParser.deserialize(remoteMessage.getData().get("metadata"));
            PushNotification pushNotification = new PushNotification(jsonMap);
            switch (pushNotification.getAction()) {
                case "friendRequest":
                    showNotificationFriendRequest(pushNotification.getJsonPayload());
                    break;
                case "canceledFriendRequest":
                    cancelFriendRequest(pushNotification.getJsonPayload());
                    break;
                case "acceptedFriendRequest":
                    showNotificationAcceptedFriendRequest(pushNotification.getJsonPayload());
                    break;
                case "removeFriend":
                    removedFriend(pushNotification.getJsonPayload());
                    break;
                case "declinedFriendRequest":
                    declinedFriendRequest(pushNotification.getJsonPayload());
                    break;
                case "receivedWorkout":
                    showNotificationReceivedWorkout(pushNotification.getJsonPayload());
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onMessageReceived(remoteMessage);
    }


    private void showNotificationFriendRequest(final String jsonData) throws IOException {
        // todo do this same thing for the timer notifications
        FriendRequest friendRequest = new FriendRequest(JsonParser.deserialize(jsonData));
        Intent notificationIntent = new Intent(this, NotificationActivity.class);
        notificationIntent.putExtra(Variables.INTENT_NOTIFICATION_DATA, jsonData);
        notificationIntent.setAction(Variables.NEW_FRIEND_REQUEST_CLICK);

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
        Intent broadcastIntent = new Intent(this, WorkoutActivity.class);
        broadcastIntent.putExtra(Variables.INTENT_NOTIFICATION_DATA, jsonData);
        broadcastIntent.setAction(Variables.NEW_FRIEND_REQUEST_BROADCAST);

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.sendBroadcast(broadcastIntent);
    }

    private void cancelFriendRequest(final String jsonData) throws IOException {
        String userToRemove = (String) JsonParser.deserialize(jsonData).get(User.USERNAME);
        Intent notificationIntent = new Intent(this, WorkoutActivity.class);
        notificationIntent.putExtra(Variables.INTENT_NOTIFICATION_DATA, userToRemove);
        notificationIntent.setAction(Variables.CANCELED_FRIEND_REQUEST_BROADCAST);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null) {
            mNotificationManager.cancel(userToRemove.hashCode());
        }

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.sendBroadcast(notificationIntent);
    }

    private void declinedFriendRequest(final String jsonData) throws IOException {
        String declinedUser = (String) JsonParser.deserialize(jsonData).get(User.USERNAME);
        Intent notificationIntent = new Intent(this, WorkoutActivity.class);
        notificationIntent.putExtra(Variables.INTENT_NOTIFICATION_DATA, declinedUser);
        notificationIntent.setAction(Variables.DECLINED_FRIEND_REQUEST_BROADCAST);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null) {
            mNotificationManager.cancel(declinedUser.hashCode());
        }

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.sendBroadcast(notificationIntent);
    }

    private void removedFriend(final String jsonData) throws IOException {
        String userToRemove = (String) JsonParser.deserialize(jsonData).get(User.USERNAME);
        Intent notificationIntent = new Intent(this, WorkoutActivity.class);
        notificationIntent.putExtra(Variables.INTENT_NOTIFICATION_DATA, userToRemove);
        notificationIntent.setAction(Variables.REMOVE_FRIEND_BROADCAST);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null) {
            // if user still has notification saying this user accepted their request, hide it if user removes them
            mNotificationManager.cancel(userToRemove.hashCode());
        }

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.sendBroadcast(notificationIntent);
    }

    private void showNotificationAcceptedFriendRequest(final String jsonData) throws IOException {
        String userAccepted = (String) JsonParser.deserialize(jsonData).get(User.USERNAME);
        Intent notificationIntent = new Intent(this, NotificationActivity.class);
        notificationIntent.putExtra(Variables.INTENT_NOTIFICATION_DATA, userAccepted);
        notificationIntent.setAction(Variables.ACCEPTED_FRIEND_REQUEST_CLICK);

        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Notification notification = new NotificationCompat.Builder(this, Variables.ACCEPTED_FRIEND_CHANNEL)
                .setContentTitle("New Friend!")
                .setContentText(String.format("%s accepted your friend request!", userAccepted))
                .setSmallIcon(R.drawable.notification_icon)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setOngoing(false)
                .build();
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null) {
            mNotificationManager.notify(userAccepted.hashCode(), notification);
        }
        Intent broadcastIntent = new Intent(this, WorkoutActivity.class);
        broadcastIntent.putExtra(Variables.INTENT_NOTIFICATION_DATA, userAccepted);
        broadcastIntent.setAction(Variables.ACCEPTED_FRIEND_REQUEST_BROADCAST);

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.sendBroadcast(broadcastIntent);
    }

    private void showNotificationReceivedWorkout(final String jsonData) throws IOException {
        final ReceivedWorkoutMeta receivedWorkoutMeta = new ReceivedWorkoutMeta(JsonParser.deserialize(jsonData));
        Intent notificationIntent = new Intent(this, NotificationActivity.class);
        notificationIntent.putExtra(Variables.INTENT_NOTIFICATION_DATA, jsonData);
        notificationIntent.setAction(Variables.RECEIVED_WORKOUT_CLICK);

        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Notification notification = new NotificationCompat.Builder(this, Variables.RECEIVED_WORKOUT_CHANNEL)
                .setContentTitle("Received workout")
                .setContentText(String.format("%s sent you a workout: \"%s\"! Click to respond.",
                        receivedWorkoutMeta.getSender(), receivedWorkoutMeta.getWorkoutName()))
                .setSmallIcon(R.drawable.notification_icon)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setOngoing(false)
                .build();
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null) {
            mNotificationManager.notify(receivedWorkoutMeta.getWorkoutId().hashCode(), notification);
        }
        Intent broadcastIntent = new Intent(this, WorkoutActivity.class);
        broadcastIntent.putExtra(Variables.INTENT_NOTIFICATION_DATA, jsonData);
        broadcastIntent.setAction(Variables.RECEIVED_WORKOUT_BROADCAST);

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.sendBroadcast(broadcastIntent);
    }
}
