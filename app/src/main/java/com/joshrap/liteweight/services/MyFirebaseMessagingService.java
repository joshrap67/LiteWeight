package com.joshrap.liteweight.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.activities.MainActivity;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.managers.UserManager;
import com.joshrap.liteweight.messages.activitymessages.AcceptedFriendRequestMessage;
import com.joshrap.liteweight.messages.activitymessages.CanceledFriendRequestMessage;
import com.joshrap.liteweight.messages.activitymessages.DeclinedFriendRequestMessage;
import com.joshrap.liteweight.messages.activitymessages.NewFriendRequestMessage;
import com.joshrap.liteweight.messages.activitymessages.ReceivedWorkoutMessage;
import com.joshrap.liteweight.messages.activitymessages.RemovedFriendMessage;
import com.joshrap.liteweight.models.notifications.AcceptedFriendRequestNotification;
import com.joshrap.liteweight.models.notifications.CanceledFriendRequestNotification;
import com.joshrap.liteweight.models.notifications.DeclinedFriendRequestNotification;
import com.joshrap.liteweight.models.notifications.RemovedAsFriendNotification;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.models.user.FriendRequest;
import com.joshrap.liteweight.models.notifications.PushNotification;
import com.joshrap.liteweight.models.user.SharedWorkoutInfo;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String friendRequestAction = "friendRequest";
    private static final String canceledFriendRequestAction = "canceledFriendRequest";
    private static final String acceptedFriendRequestAction = "acceptedFriendRequest";
    private static final String removedAsFriendAction = "removedAsFriend";
    private static final String declinedFriendRequestAction = "declinedFriendRequest";
    private static final String receivedWorkoutNotificationAction = "receivedWorkout";

    @Inject
    ObjectMapper objectMapper;

    @Inject
    UserManager userManager;

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> userManager.setFirebaseMessagingToken(token));
    }

    @Override
    public void onCreate() {
        Injector.getInjector(getBaseContext()).inject(this);
        super.onCreate();
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        try {
            Map<String, String> json = remoteMessage.getData();
            PushNotification pushNotification = this.objectMapper.convertValue(json, PushNotification.class);
            switch (pushNotification.getAction()) {
                case friendRequestAction:
                    showNotificationNewFriendRequest(pushNotification.getJsonPayload());
                    break;
                case canceledFriendRequestAction:
                    silentNotificationCancelFriendRequest(pushNotification.getJsonPayload());
                    break;
                case acceptedFriendRequestAction:
                    showNotificationAcceptedFriendRequest(pushNotification.getJsonPayload());
                    break;
                case removedAsFriendAction:
                    silentNotificationRemovedFriend(pushNotification.getJsonPayload());
                    break;
                case declinedFriendRequestAction:
                    silentNotificationDeclinedFriendRequest(pushNotification.getJsonPayload());
                    break;
                case receivedWorkoutNotificationAction:
                    showNotificationReceivedWorkout(pushNotification.getJsonPayload());
                    break;
            }
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        super.onMessageReceived(remoteMessage);
    }


    private void showNotificationNewFriendRequest(final String jsonData) throws IOException {
        FriendRequest friendRequest = objectMapper.readValue(jsonData, FriendRequest.class);
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.putExtra(Variables.NOTIFICATION_ACTION, Variables.NEW_FRIEND_REQUEST_CLICK);

        PendingIntent contentIntent = PendingIntent.getActivity(this,
                Variables.FRIEND_REQUEST_CODE, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new NotificationCompat.Builder(this, Variables.FRIEND_REQUEST_CHANNEL)
                .setContentTitle("New Friend Request!")
                .setContentText(String.format("%s wants to be your friend! Click to respond.", friendRequest.getUsername()))
                .setSmallIcon(R.drawable.notification_icon)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setOngoing(false)
                .build();
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null) {
            mNotificationManager.notify(friendRequest.getUserId().hashCode(), notification);
        }

        NewFriendRequestMessage message = new NewFriendRequestMessage(friendRequest);
        EventBus.getDefault().post(message);
    }

    private void silentNotificationCancelFriendRequest(final String jsonData) throws IOException {
        CanceledFriendRequestNotification userToRemove = objectMapper.readValue(jsonData, CanceledFriendRequestNotification.class);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null) {
            mNotificationManager.cancel(userToRemove.getUserId().hashCode());
        }

        CanceledFriendRequestMessage message = new CanceledFriendRequestMessage(userToRemove.getUserId());
        EventBus.getDefault().post(message);
    }

    private void silentNotificationDeclinedFriendRequest(final String jsonData) throws IOException {
        DeclinedFriendRequestNotification declinedFriendRequestNotification = objectMapper.readValue(jsonData, DeclinedFriendRequestNotification.class);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null) {
            mNotificationManager.cancel(declinedFriendRequestNotification.getUserId().hashCode());
        }

        DeclinedFriendRequestMessage message = new DeclinedFriendRequestMessage(declinedFriendRequestNotification.getUserId());
        EventBus.getDefault().post(message);
    }

    private void silentNotificationRemovedFriend(final String jsonData) throws IOException {
        RemovedAsFriendNotification removedAsFriendNotification = objectMapper.readValue(jsonData, RemovedAsFriendNotification.class);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null) {
            // if user still has notification saying this user accepted their request, hide it if user removes them
            mNotificationManager.cancel(removedAsFriendNotification.getUserId().hashCode());
        }

        RemovedFriendMessage message = new RemovedFriendMessage(removedAsFriendNotification.getUserId());
        EventBus.getDefault().post(message);
    }

    private void showNotificationAcceptedFriendRequest(final String jsonData) throws IOException {
        AcceptedFriendRequestNotification userAccepted = objectMapper.readValue(jsonData, AcceptedFriendRequestNotification.class);
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.putExtra(Variables.NOTIFICATION_ACTION, Variables.ACCEPTED_FRIEND_REQUEST_CLICK);

        PendingIntent contentIntent = PendingIntent.getActivity(this,
                Variables.ACCEPTED_REQUEST_CODE, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new NotificationCompat.Builder(this, Variables.ACCEPTED_FRIEND_CHANNEL)
                .setContentTitle("New Workout Buddy!")
                .setContentText(String.format("%s accepted your friend request!", userAccepted.getUsername()))
                .setSmallIcon(R.drawable.notification_icon)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setOngoing(false)
                .build();
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null) {
            mNotificationManager.notify(userAccepted.getUserId().hashCode(), notification);
        }

        AcceptedFriendRequestMessage message = new AcceptedFriendRequestMessage(userAccepted.getUserId());
        EventBus.getDefault().post(message);
    }

    private void showNotificationReceivedWorkout(final String jsonData) throws IOException {
        final SharedWorkoutInfo sharedWorkoutInfo = objectMapper.readValue(jsonData, SharedWorkoutInfo.class);
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.putExtra(Variables.NOTIFICATION_ACTION, Variables.RECEIVED_WORKOUT_CLICK);

        PendingIntent contentIntent = PendingIntent.getActivity(this,
                Variables.RECEIVED_WORKOUT_REQUEST_CODE, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new NotificationCompat.Builder(this, Variables.RECEIVED_WORKOUT_CHANNEL)
                .setContentTitle("Workout Received!")
                .setContentText(String.format("%s sent you a workout: %s. Click to respond.",
                        sharedWorkoutInfo.getSenderUsername(), sharedWorkoutInfo.getWorkoutName()))
                .setSmallIcon(R.drawable.notification_icon)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setOngoing(false)
                .build();
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null) {
            mNotificationManager.notify(sharedWorkoutInfo.getSharedWorkoutId().hashCode(), notification);
        }

        ReceivedWorkoutMessage message = new ReceivedWorkoutMessage(sharedWorkoutInfo);
        EventBus.getDefault().post(message);
    }
}
