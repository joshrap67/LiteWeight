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
import com.joshrap.liteweight.activities.WorkoutActivity;
import com.joshrap.liteweight.utils.JsonUtils;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.models.FriendRequest;
import com.joshrap.liteweight.models.PushNotification;
import com.joshrap.liteweight.models.SharedWorkoutMeta;
import com.joshrap.liteweight.models.User;

import java.io.IOException;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String friendRequestAction = "friendRequest";
    private static final String canceledFriendRequestAction = "canceledFriendRequest";
    private static final String acceptedFriendRequestAction = "acceptedFriendRequest";
    private static final String removedAsFriendAction = "removeFriend"; // todo change this...
    private static final String declinedFriendRequestAction = "declinedFriendRequest";
    private static final String receivedNotificationAction = "receivedWorkout";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        try {
            Map<String, Object> jsonMap = JsonUtils.deserialize(remoteMessage.getData().get("metadata"));
            PushNotification pushNotification = new PushNotification(jsonMap);
            switch (pushNotification.getAction()) {
                case friendRequestAction:
                    showNotificationFriendRequest(pushNotification.getJsonPayload());
                    break;
                case canceledFriendRequestAction:
                    // silent notification
                    cancelFriendRequest(pushNotification.getJsonPayload());
                    break;
                case acceptedFriendRequestAction:
                    showNotificationAcceptedFriendRequest(pushNotification.getJsonPayload());
                    break;
                case removedAsFriendAction:
                    // silent notification
                    removedFriend(pushNotification.getJsonPayload());
                    break;
                case declinedFriendRequestAction:
                    // silent notification
                    declinedFriendRequest(pushNotification.getJsonPayload());
                    break;
                case receivedNotificationAction:
                    showNotificationReceivedWorkout(pushNotification.getJsonPayload());
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onMessageReceived(remoteMessage);
    }


    private void showNotificationFriendRequest(final String jsonData) throws IOException {
        FriendRequest friendRequest = new FriendRequest(JsonUtils.deserialize(jsonData));
        Intent notificationIntent = new Intent(this, WorkoutActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.setAction(Variables.NOTIFICATION_CLICKED);
        notificationIntent.putExtra(Variables.INTENT_NOTIFICATION_DATA, jsonData);
        notificationIntent.putExtra(Variables.NOTIFICATION_ACTION, Variables.NEW_FRIEND_REQUEST_CLICK);

        PendingIntent contentIntent = PendingIntent.getActivity(this,
                Variables.FRIEND_REQUEST_CODE, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
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
        String userToRemove = (String) JsonUtils.deserialize(jsonData).get(User.USERNAME);
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
        String declinedUser = (String) JsonUtils.deserialize(jsonData).get(User.USERNAME);
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
        String userToRemove = (String) JsonUtils.deserialize(jsonData).get(User.USERNAME);
        Intent notificationIntent = new Intent(this, WorkoutActivity.class);
        notificationIntent.putExtra(Variables.INTENT_NOTIFICATION_DATA, userToRemove);
        notificationIntent.setAction(Variables.REMOVED_AS_FRIEND_BROADCAST);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null) {
            // if user still has notification saying this user accepted their request, hide it if user removes them
            mNotificationManager.cancel(userToRemove.hashCode());
        }

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.sendBroadcast(notificationIntent);
    }

    private void showNotificationAcceptedFriendRequest(final String jsonData) throws IOException {
        String userAccepted = (String) JsonUtils.deserialize(jsonData).get(User.USERNAME);
        Intent notificationIntent = new Intent(this, WorkoutActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.setAction(Variables.NOTIFICATION_CLICKED);
        notificationIntent.putExtra(Variables.INTENT_NOTIFICATION_DATA, userAccepted);
        notificationIntent.putExtra(Variables.NOTIFICATION_ACTION, Variables.ACCEPTED_FRIEND_REQUEST_CLICK);

        PendingIntent contentIntent = PendingIntent.getActivity(this,
                Variables.ACCEPTED_REQUEST_CODE, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Notification notification = new NotificationCompat.Builder(this, Variables.ACCEPTED_FRIEND_CHANNEL)
                .setContentTitle("New Workout Buddy!")
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
        final SharedWorkoutMeta sharedWorkoutMeta = new SharedWorkoutMeta(JsonUtils.deserialize(jsonData));
        Intent notificationIntent = new Intent(this, WorkoutActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.setAction(Variables.NOTIFICATION_CLICKED);
        notificationIntent.putExtra(Variables.INTENT_NOTIFICATION_DATA, jsonData);
        notificationIntent.putExtra(Variables.NOTIFICATION_ACTION, Variables.RECEIVED_WORKOUT_CLICK);

        PendingIntent contentIntent = PendingIntent.getActivity(this,
                Variables.RECEIVED_WORKOUT_REQUEST_CODE, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Notification notification = new NotificationCompat.Builder(this, Variables.RECEIVED_WORKOUT_CHANNEL)
                .setContentTitle("Received workout")
                .setContentText(String.format("%s sent you a workout: \"%s\"! Click to respond.",
                        sharedWorkoutMeta.getSender(), sharedWorkoutMeta.getWorkoutName()))
                .setSmallIcon(R.drawable.notification_icon)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setOngoing(false)
                .build();
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null) {
            mNotificationManager.notify(sharedWorkoutMeta.getWorkoutId().hashCode(), notification);
        }
        Intent broadcastIntent = new Intent(this, WorkoutActivity.class);
        broadcastIntent.putExtra(Variables.INTENT_NOTIFICATION_DATA, jsonData);
        broadcastIntent.setAction(Variables.RECEIVED_WORKOUT_BROADCAST);

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.sendBroadcast(broadcastIntent);
    }
}
