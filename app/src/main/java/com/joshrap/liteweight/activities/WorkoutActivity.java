package com.joshrap.liteweight.activities;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.navigation.NavigationView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.fragments.*;
import com.joshrap.liteweight.imports.Globals;
import com.joshrap.liteweight.models.Workout;
import com.joshrap.liteweight.utils.AndroidUtils;
import com.joshrap.liteweight.utils.ImageUtils;
import com.joshrap.liteweight.utils.JsonUtils;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;
import com.joshrap.liteweight.models.FriendRequest;
import com.joshrap.liteweight.models.SharedWorkoutMeta;
import com.joshrap.liteweight.models.SharedWorkout;
import com.joshrap.liteweight.models.Tokens;
import com.joshrap.liteweight.models.User;
import com.joshrap.liteweight.models.UserWithWorkout;
import com.joshrap.liteweight.network.RequestFields;
import com.joshrap.liteweight.network.repos.UserRepository;
import com.joshrap.liteweight.services.StopwatchService;
import com.joshrap.liteweight.services.SyncWorkoutService;
import com.joshrap.liteweight.services.TimerService;
import com.joshrap.liteweight.widgets.Stopwatch;
import com.joshrap.liteweight.widgets.Timer;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import lombok.Getter;

public class WorkoutActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;
    private AlertDialog alertDialog;
    private ActionBarDrawerToggle toggle;
    private boolean drawerListenerIsRegistered;
    private TextView toolbarTitleTV, accountNotificationTV;
    private NavigationView nav;
    private FragmentManager fragmentManager;
    private boolean showPopupFlag;
    private ArrayList<String> fragmentStack; // stack of fragment ids
    private Map<String, Fragment.SavedState> fragmentSavedStatesMap;
    private boolean activityFinishing;
    private ImageView profilePicture;
    private Workout lastSyncedWorkout;

    @Getter
    private UserWithWorkout userWithWorkout;
    @Getter
    private User user;
    @Getter
    private Timer timer;
    @Getter
    private Stopwatch stopwatch;

    @Inject
    Tokens tokens;
    @Inject
    UserRepository userRepository;
    @Inject
    SharedPreferences sharedPreferences;
    @Inject
    ProgressDialog loadingDialog;

    private final BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                return;
            }
            // the broadcasts should only be updating the base models (user/workout). Individual fragments handle UI changes on their own
            switch (action) {
                case Variables.NEW_FRIEND_REQUEST_BROADCAST: {
                    try {
                        FriendRequest friendRequest = new FriendRequest(JsonUtils.deserialize((String) intent.getExtras().get(Variables.INTENT_NOTIFICATION_DATA)));
                        user.getFriendRequests().put(friendRequest.getUsername(), friendRequest);
                        updateAccountNotificationIndicator();

                        // send broadcast to any fragments waiting on this model update
                        Intent broadcastIntent = new Intent();
                        broadcastIntent.setAction(Variables.NEW_FRIEND_REQUEST_MODEL_UPDATED_BROADCAST);
                        broadcastIntent.putExtra(Variables.INTENT_NOTIFICATION_DATA, (String) intent.getExtras().get(Variables.INTENT_NOTIFICATION_DATA));
                        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
                        localBroadcastManager.sendBroadcast(broadcastIntent);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case Variables.CANCELED_FRIEND_REQUEST_BROADCAST: {
                    String usernameToRemove = intent.getExtras().get(Variables.INTENT_NOTIFICATION_DATA).toString();
                    user.getFriendRequests().remove(usernameToRemove);
                    updateAccountNotificationIndicator();

                    // send broadcast to any fragments waiting on this model update
                    Intent broadcastIntent = new Intent();
                    broadcastIntent.setAction(Variables.CANCELED_REQUEST_MODEL_UPDATED_BROADCAST);
                    broadcastIntent.putExtra(Variables.INTENT_NOTIFICATION_DATA, (String) intent.getExtras().get(Variables.INTENT_NOTIFICATION_DATA));
                    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
                    localBroadcastManager.sendBroadcast(broadcastIntent);
                    break;
                }
                case Variables.DECLINED_FRIEND_REQUEST_BROADCAST: {
                    String usernameToRemove = intent.getExtras().get(Variables.INTENT_NOTIFICATION_DATA).toString();
                    user.getFriends().remove(usernameToRemove);

                    // send broadcast to any fragments waiting on this model update
                    Intent broadcastIntent = new Intent();
                    broadcastIntent.setAction(Variables.DECLINED_REQUEST_MODEL_UPDATED_BROADCAST);
                    broadcastIntent.putExtra(Variables.INTENT_NOTIFICATION_DATA, (String) intent.getExtras().get(Variables.INTENT_NOTIFICATION_DATA));
                    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
                    localBroadcastManager.sendBroadcast(broadcastIntent);
                    break;
                }
                case Variables.REMOVED_AS_FRIEND_BROADCAST: {
                    String usernameToRemove = intent.getExtras().get(Variables.INTENT_NOTIFICATION_DATA).toString();
                    user.getFriends().remove(usernameToRemove);

                    // send broadcast to any fragments waiting on this model update
                    Intent broadcastIntent = new Intent();
                    broadcastIntent.setAction(Variables.REMOVE_FRIEND_MODEL_UPDATED_BROADCAST);
                    broadcastIntent.putExtra(Variables.INTENT_NOTIFICATION_DATA, (String) intent.getExtras().get(Variables.INTENT_NOTIFICATION_DATA));
                    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
                    localBroadcastManager.sendBroadcast(broadcastIntent);
                    break;
                }
                case Variables.ACCEPTED_FRIEND_REQUEST_BROADCAST: {
                    String usernameAccepted = intent.getExtras().get(Variables.INTENT_NOTIFICATION_DATA).toString();
                    user.getFriends().get(usernameAccepted).setConfirmed(true);

                    // send broadcast to any fragments waiting on this model update
                    Intent broadcastIntent = new Intent();
                    broadcastIntent.setAction(Variables.ACCEPTED_REQUEST_MODEL_UPDATED_BROADCAST);
                    broadcastIntent.putExtra(Variables.INTENT_NOTIFICATION_DATA, (String) intent.getExtras().get(Variables.INTENT_NOTIFICATION_DATA));
                    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
                    localBroadcastManager.sendBroadcast(broadcastIntent);
                    break;
                }
                case Variables.RECEIVED_WORKOUT_BROADCAST:
                    try {
                        SharedWorkoutMeta sharedWorkoutMeta = new SharedWorkoutMeta(JsonUtils.deserialize((String) intent.getExtras().get(Variables.INTENT_NOTIFICATION_DATA)));
                        boolean updateTotal = user.getReceivedWorkouts().get(sharedWorkoutMeta.getWorkoutId()) == null;
                        if (updateTotal) {
                            // workout wasn't here, so total needs to be increased
                            user.setTotalReceivedWorkouts(user.getTotalReceivedWorkouts() + 1);
                        }

                        // if workout isn't there, update unseen. If workout is there and it is already marked as seen: update it to unseen
                        boolean updateUnseen =
                                user.getReceivedWorkouts().get(sharedWorkoutMeta.getWorkoutId()) == null ||
                                        user.getReceivedWorkouts().get(sharedWorkoutMeta.getWorkoutId()).isSeen();
                        // no npe since Java will see the first part is true and then immediately return true
                        if (updateUnseen) {
                            // workout has not been seen yet so increase the unseen count
                            user.setUnseenReceivedWorkouts(user.getUnseenReceivedWorkouts() + 1);
                        }
                        updateReceivedWorkoutNotificationIndicator();
                        user.getReceivedWorkouts().put(sharedWorkoutMeta.getWorkoutId(), sharedWorkoutMeta);
                        // send broadcast to any fragments waiting on this model update
                        Intent broadcastIntent = new Intent();
                        broadcastIntent.setAction(Variables.RECEIVED_WORKOUT_MODEL_UPDATED_BROADCAST);
                        broadcastIntent.putExtra(Variables.INTENT_NOTIFICATION_DATA, (String) intent.getExtras().get(Variables.INTENT_NOTIFICATION_DATA));
                        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
                        localBroadcastManager.sendBroadcast(broadcastIntent);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        String action = null;
        String jsonNotificationData = null;
        activityFinishing = false;
        if (getIntent().getExtras() != null) {
            // there is notification data
            action = getIntent().getAction();
            if (action != null && action.equals(Variables.NOTIFICATION_CLICKED)) {
                /*
                    So freaking hacky. Essentially if user clicked a notification while app is terminated, we immediately finish this
                    activity and start the splash activity. This is due to android being awful and their flags
                    aren't working properly so this is the only way i can avoid this activity needlessly being destroyed twice.
                 */
                activityFinishing = true;
                launchSplashActivity(getIntent().getExtras().getString(Variables.INTENT_NOTIFICATION_DATA),
                        getIntent().getExtras().getString(Variables.NOTIFICATION_ACTION));
                finish();
                return;
            }
            jsonNotificationData = getIntent().getExtras().getString(Variables.INTENT_NOTIFICATION_DATA);
        }

        if (userWithWorkout == null) {
            userWithWorkout = Globals.userWithWorkout;
            Globals.userWithWorkout = null; // grr have to use because can't serialize big data in an intent
        }

        user = userWithWorkout.getUser();
        if (userWithWorkout.isWorkoutPresent()) {
            lastSyncedWorkout = new Workout(userWithWorkout.getWorkout());
        } else {
            lastSyncedWorkout = null;
        }

        Injector.getInjector(this).inject(this);

        IntentFilter receiverActions = new IntentFilter();
        receiverActions.addAction(Variables.NEW_FRIEND_REQUEST_BROADCAST);
        receiverActions.addAction(Variables.ACCEPTED_FRIEND_REQUEST_BROADCAST);
        receiverActions.addAction(Variables.CANCELED_FRIEND_REQUEST_BROADCAST);
        receiverActions.addAction(Variables.REMOVED_AS_FRIEND_BROADCAST);
        receiverActions.addAction(Variables.DECLINED_FRIEND_REQUEST_BROADCAST);
        receiverActions.addAction(Variables.RECEIVED_WORKOUT_BROADCAST);
        LocalBroadcastManager.getInstance(this).registerReceiver(notificationReceiver, receiverActions);

        setContentView(R.layout.activity_workout);
        timer = new Timer(this, sharedPreferences);
        stopwatch = new Stopwatch(this, sharedPreferences);
        fragmentStack = new ArrayList<>();
        showPopupFlag = true;
        fragmentSavedStatesMap = new HashMap<>();
        drawerListenerIsRegistered = false;
        fragmentManager = getSupportFragmentManager();

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbarTitleTV = findViewById(R.id.toolbar_title);
        drawer = findViewById(R.id.drawer);
        nav = findViewById(R.id.nav_view);
        nav.setNavigationItemSelectedListener(this);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false); // removes the app title from the toolbar
        }

        toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.nav_draw_open, R.string.nav_draw_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        if (savedInstanceState == null) {
            // default landing fragment is current workout one
            fragmentManager.beginTransaction().replace(R.id.fragment_container,
                    new CurrentWorkoutFragment(), Variables.CURRENT_WORKOUT_TITLE).commit();
            fragmentStack.add(Variables.CURRENT_WORKOUT_TITLE);
            nav.setCheckedItem(R.id.nav_current_workout);
        }
        View headerView = nav.getHeaderView(0);
        ConstraintLayout headerLayout = headerView.findViewById(R.id.nav_header);
        headerLayout.setOnClickListener(view -> {
            goToAccountSettings();
            drawer.closeDrawer(GravityCompat.START);
        });
        TextView usernameTV = headerView.findViewById(R.id.username_tv);
        usernameTV.setText(user.getUsername());
        accountNotificationTV = headerView.findViewById(R.id.notification_tv);
        profilePicture = headerView.findViewById(R.id.profile_picture);
        Picasso.get()
                .load(ImageUtils.getIconUrl(user.getIcon()))
                .error(R.drawable.picture_load_error)
                .networkPolicy(NetworkPolicy.NO_CACHE) // on first loading in app, always fetch online
                .into(profilePicture, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        Bitmap imageBitmap = ((BitmapDrawable) profilePicture.getDrawable()).getBitmap();
                        RoundedBitmapDrawable imageDrawable = RoundedBitmapDrawableFactory.create(getResources(), imageBitmap);
                        imageDrawable.setCircular(true);
                        imageDrawable.setCornerRadius(Math.max(imageBitmap.getWidth(), imageBitmap.getHeight()) / 2.0f);
                        profilePicture.setImageDrawable(imageDrawable);
                    }

                    @Override
                    public void onError(Exception e) {
                    }
                });

        createNotificationChannel();
        updateEndpointToken();
        updateAccountNotificationIndicator();
        updateReceivedWorkoutNotificationIndicator();
        if (action != null && jsonNotificationData != null) {
            // means the user clicked on a notification which created this activity, so take them to the appropriate fragment
            navigateToFragmentFromNotification(action);
        }
    }

    private void launchSplashActivity(String jsonData, String action) {
        Intent intent = new Intent(this, SplashActivity.class);
        intent.putExtra(Variables.INTENT_NOTIFICATION_DATA, jsonData);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setAction(action);
        startActivity(intent);
    }

    /**
     * Called whenever the user clicks on a notification while the app is running or paused.
     *
     * @param intent contains data about how to route and what data to consume.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Bundle extras = intent.getExtras();
        if (extras == null) {
            return;
        }
        String action = extras.getString(Variables.NOTIFICATION_ACTION);
        if (action == null) {
            return;
        }
        switch (action) {
            case Variables.INTENT_TIMER_NOTIFICATION_CLICK:
            case Variables.INTENT_STOPWATCH_NOTIFICATION_CLICK:
                // close any popup that might be showing
                closeAllOpenDialogs();
                goToCurrentWorkout();
                nav.setCheckedItem(R.id.nav_current_workout);
                break;
            case Variables.RECEIVED_WORKOUT_CLICK:
                closeAllOpenDialogs();
                nav.setCheckedItem(R.id.nav_received_workouts);
                goToReceivedWorkouts();
                break;
            case Variables.ACCEPTED_FRIEND_REQUEST_CLICK:
                closeAllOpenDialogs();
                goToFriendsList(null);
                break;
            case Variables.NEW_FRIEND_REQUEST_CLICK:
                closeAllOpenDialogs();
                Bundle extrasFriendRequest = new Bundle(); // to start the fragment on the friend request tab
                extrasFriendRequest.putInt(Variables.FRIEND_LIST_POSITION, FriendsListFragment.REQUESTS_POSITION);
                goToFriendsList(extrasFriendRequest);
                break;
        }
    }

    /**
     * This is called whenever user opens notification while app was terminated.
     * No need to update any models since that would have been taken care of from the splash screen load
     *
     * @param action informs which route to take.
     */
    private void navigateToFragmentFromNotification(String action) {
        if (action == null) {
            return;
        }
        switch (action) {
            case Variables.NEW_FRIEND_REQUEST_CLICK:
                Bundle extras = new Bundle(); // to start the fragment on the friend request tab
                extras.putInt(Variables.FRIEND_LIST_POSITION, FriendsListFragment.REQUESTS_POSITION);
                goToFriendsList(extras);
                break;
            case Variables.ACCEPTED_FRIEND_REQUEST_CLICK:
                goToFriendsList(null);
                break;
            case Variables.RECEIVED_WORKOUT_CLICK:
                goToReceivedWorkouts();
                nav.setCheckedItem(R.id.nav_received_workouts);
                break;
        }
    }

    @Override
    protected void onPause() {
        syncCurrentWorkout();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (!activityFinishing) {
            // stop any timer/stopwatch services that may be running.
            stopService(new Intent(this, TimerService.class));
            stopService(new Intent(this, StopwatchService.class));
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                // if timer finished and user hasn't acknowledged the notification yet, just clear it on app termination
                notificationManager.cancel(TimerService.timerFinishedId);
            }
            // update tokens just in case they changed in apps life cycle
            LocalBroadcastManager.getInstance(this).unregisterReceiver(notificationReceiver);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(Variables.REFRESH_TOKEN_KEY, tokens.getRefreshToken());
            editor.putString(Variables.ID_TOKEN_KEY, tokens.getIdToken());
            editor.apply();
        }
        super.onDestroy();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_current_workout:
                if (!(getVisibleFragment() instanceof CurrentWorkoutFragment)) {
                    goToCurrentWorkout();
                }
                break;
            case R.id.nav_my_workouts:
                if (!(getVisibleFragment() instanceof MyWorkoutsFragment)) {
                    goToMyWorkouts();
                }
                break;
            case R.id.nav_my_exercises:
                if (!(getVisibleFragment() instanceof MyExercisesFragment)) {
                    goToMyExercises();
                }
                break;
            case R.id.nav_received_workouts:
                if (!(getVisibleFragment() instanceof ReceivedWorkoutsFragment)) {
                    goToReceivedWorkouts();
                }
                break;
            case R.id.nav_user_settings:
                if (!(getVisibleFragment() instanceof AppSettingsFragment)) {
                    goToAppSettings();
                }
                break;
            case R.id.nav_about:
                if (!(getVisibleFragment() instanceof AboutFragment)) {
                    goToAbout();
                }
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        Fragment visibleFragment = getVisibleFragment();
        boolean modified = isFragmentModified(visibleFragment);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            // if the user clicked the navigation panel, allow back press to close it.
            drawer.closeDrawer(GravityCompat.START);
            return;
        } else if (visibleFragment instanceof NewWorkoutFragment) {
            if (modified && showPopupFlag) {
                // workout is being made, so give user option to prevent fragment from closing from back press
                showUnsavedChangesPopup(Variables.NEW_WORKOUT_TITLE);
                return;
            }
        } else if (visibleFragment instanceof EditWorkoutFragment) {
            if (modified && showPopupFlag) {
                // workout is being edited, so give user option to prevent fragment from closing from back press
                showUnsavedChangesPopup(Variables.EDIT_WORKOUT_TITLE);
                return;
            }
        }

        if (fragmentStack.size() > 1) {
            // there's at least two fragments on the stack, so pressing back button will pop the one on the top of the stack
            popFragStack();
        } else {
            super.onBackPressed();
        }
    }

    private void syncCurrentWorkout() {
        if (userWithWorkout.isWorkoutPresent()
                && Workout.workoutsDifferent(lastSyncedWorkout, userWithWorkout.getWorkout())) {
            // we assume it always succeeds
            lastSyncedWorkout = new Workout(userWithWorkout.getWorkout());
            Intent intent = new Intent(this, SyncWorkoutService.class);
            intent.putExtra(Variables.INTENT_REFRESH_TOKEN, tokens.getRefreshToken());
            intent.putExtra(Variables.INTENT_ID_TOKEN, tokens.getIdToken());
            try {
                intent.putExtra(RequestFields.WORKOUT, new ObjectMapper().writeValueAsString(userWithWorkout.getWorkout().asMap()));
                startService(intent);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }

    public void popFragStack() {
        /*
            Utilizes a custom stack to handle navigation of fragments. Always pops the top and then goes to
            which ever fragment is now on the top of the stack.
            The frag stack will only have one instance of the fragments in it at all times, handled by the
            navigation methods.
         */
        showPopupFlag = true;
        fragmentStack.remove(0);

        String currentFrag = fragmentStack.get(0);
        switch (currentFrag) {
            case Variables.CURRENT_WORKOUT_TITLE:
                goToCurrentWorkout();
                nav.setCheckedItem(R.id.nav_current_workout);
                break;
            case Variables.MY_WORKOUT_TITLE:
                goToMyWorkouts();
                nav.setCheckedItem(R.id.nav_my_workouts);
                break;
            case Variables.SETTINGS_TITLE:
                goToAppSettings();
                nav.setCheckedItem(R.id.nav_user_settings);
                break;
            case Variables.MY_EXERCISES_TITLE:
                goToMyExercises();
                nav.setCheckedItem(R.id.nav_my_exercises);
                break;
            case Variables.ABOUT_TITLE:
                goToAbout();
                nav.setCheckedItem(R.id.nav_about);
                break;
            case Variables.ACCOUNT_TITLE:
                goToAccountSettings();
                break;
            case Variables.BLOCKED_LIST_TITLE:
                goToBlockedList();
                break;
            case Variables.FRIENDS_LIST_TITLE:
                goToFriendsList(null);
                break;
            case Variables.ACCOUNT_PREFS_TITLE:
                goToAccountPreferences();
                break;
            case Variables.RECEIVED_WORKOUTS_TITLE:
                goToReceivedWorkouts();
                nav.setCheckedItem(R.id.nav_received_workouts);
                break;
            case Variables.FAQ_TITLE:
                goToFaq();
                break;
            default:
                /*
                    If the fragment now currently on the backstack is a fragment that I don't want the user to get back to,
                    then go back again to get rid of it.

                    This would happen for example if clicking on notification when on the edit workout fragment.
                 */
                onBackPressed();
                break;
        }
    }

    public void logout() {
        // stop any timer/stopwatch services that may be running.
        stopService(new Intent(this, TimerService.class));
        stopService(new Intent(this, StopwatchService.class));
        syncCurrentWorkout();

        AndroidUtils.showLoadingDialog(loadingDialog, "Logging out...");
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            // blind send for now for removing notification endpoint id
            userRepository.removeEndpointId();
            // doing this all in the same thread to avoid potential race condition of deleting tokens while trying to make api call
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                LocalBroadcastManager.getInstance(this).unregisterReceiver(notificationReceiver);
                // clear appropriate values in shared prefs
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove(Variables.ID_TOKEN_KEY);
                editor.remove(Variables.REFRESH_TOKEN_KEY);
                editor.apply();

                // clear all notifications
                NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null) {
                    notificationManager.cancelAll();
                }
                // since tokens are singleton, need to update tokens to null here
                tokens.setRefreshToken(null);
                tokens.setIdToken(null);
                timer.stopTimer();
                stopwatch.stopStopwatch();

                // take user back to sign in activity
                Intent intent = new Intent(this, SignInActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            });
        });
    }

    /**
     * Fetches token from Firebase and then registers it with SNS in order for push notifications to work.
     */
    private void updateEndpointToken() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        return;
                    }

                    // Get new Instance ID token
                    String token = task.getResult().getToken();
                    Executor executor = Executors.newSingleThreadExecutor();
                    executor.execute(() -> {
                        // blind send for now for updating notification endpoint id
                        userRepository.updateEndpointId(token);
                    });
                });
    }

    /**
     * Sets up a notification channel for each channel in the app. Each channel is preset with
     * notification options but these can always be changed by the user.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // channel for when the timer is running but not finished
            NotificationChannel timerRunningChannel = new NotificationChannel(
                    Variables.TIMER_RUNNING_CHANNEL,
                    "Timer Running",
                    NotificationManager.IMPORTANCE_DEFAULT);
            timerRunningChannel.setSound(null, null);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(timerRunningChannel);
            // channel for when the timer finished
            NotificationChannel timerFinishedChannel = new NotificationChannel(
                    Variables.TIMER_FINISHED_CHANNEL,
                    "Timer Finished",
                    NotificationManager.IMPORTANCE_DEFAULT);
            manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(timerFinishedChannel);

            // channel for when stopwatch is running
            NotificationChannel stopwatchRunningChannel = new NotificationChannel(
                    Variables.STOPWATCH_RUNNING_CHANNEL,
                    "Stopwatch Running",
                    NotificationManager.IMPORTANCE_DEFAULT);
            manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(stopwatchRunningChannel);

            // channel for incoming friend requests
            NotificationChannel friendRequestChannel = new NotificationChannel(
                    Variables.FRIEND_REQUEST_CHANNEL,
                    "New Friend Requests",
                    NotificationManager.IMPORTANCE_DEFAULT);
            manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(friendRequestChannel);

            // channel for accepted friend requests
            NotificationChannel acceptedRequestChannel = new NotificationChannel(
                    Variables.ACCEPTED_FRIEND_CHANNEL,
                    "Accepted Friend Requests",
                    NotificationManager.IMPORTANCE_DEFAULT);
            manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(acceptedRequestChannel);

            // channel for accepted friend requests
            NotificationChannel receivedWorkoutChannel = new NotificationChannel(
                    Variables.RECEIVED_WORKOUT_CHANNEL,
                    "Received Workouts",
                    NotificationManager.IMPORTANCE_DEFAULT);
            manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(receivedWorkoutChannel);
        }
    }

    private void closeAllOpenDialogs() {
        Fragment currentFragment = getVisibleFragment();
        if (currentFragment instanceof FragmentWithDialog) {
            ((FragmentWithDialog) currentFragment).hideAllDialogs();
        }
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        timer.hideDialog();
    }

    /**
     * Toggles whether a back button or hamburger icon should be shown for the drawer menu.
     *
     * @param enable if true, show the back button.
     */
    public void toggleBackButton(boolean enable) {
        if (enable) {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            toggle.setDrawerIndicatorEnabled(false);
            // show back button
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if (!drawerListenerIsRegistered) {
                toggle.setToolbarNavigationClickListener(v -> onBackPressed());
                drawerListenerIsRegistered = true;
            }
        } else {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            // remove back button
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            toggle.setDrawerIndicatorEnabled(true);
            toggle.setToolbarNavigationClickListener(null);
            drawerListenerIsRegistered = false;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        /*
            Found on SO. Hides keyboard when clicking outside editText.
            https://gist.github.com/sc0rch/7c982999e5821e6338c25390f50d2993
         */
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect viewRect = new Rect();
                v.getGlobalVisibleRect(viewRect);
                if (!viewRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    boolean touchTargetIsEditText = false;
                    //Check if another editText has been touched
                    for (View vi : v.getRootView().getTouchables()) {
                        if (vi instanceof EditText) {
                            Rect clickedViewRect = new Rect();
                            vi.getGlobalVisibleRect(clickedViewRect);
                            //Bounding box is to big, reduce it just a little bit
                            if (clickedViewRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                                touchTargetIsEditText = true;
                                break;
                            }
                        }
                    }
                    if (!touchTargetIsEditText) {
                        v.clearFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    /**
     * Shows a popup whenever user has unfinished work in either the new workout fragment or edit workout one.
     *
     * @param fragmentTitle title of the fragment that is unfinished.
     */
    private void showUnsavedChangesPopup(String fragmentTitle) {
        String message = "";
        if (fragmentTitle.equals(Variables.EDIT_WORKOUT_TITLE)) {
            message = getString(R.string.popup_message_edit_workout);
        } else if (fragmentTitle.equals(Variables.NEW_WORKOUT_TITLE)) {
            message = getString(R.string.unsaved_workout_msg);
        }
        alertDialog = new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                .setTitle("Unsaved Changes")
                .setMessage(message)
                .setPositiveButton("Yes", (dialog, which) -> {
                    showPopupFlag = false;
                    onBackPressed();
                })
                .setNegativeButton("No", null)
                .create();
        alertDialog.show();
    }

    /**
     * Called by other fragments to change the string that the toolbar displays.
     *
     * @param aTitle new title for the toolbar.
     */
    public void updateToolbarTitle(String aTitle) {
        toolbarTitleTV.setText(aTitle);
    }

    public void updateUserIcon(Uri uri) {
        profilePicture.setImageURI(uri);
        // make image round
        Bitmap imageBitmap = ((BitmapDrawable) profilePicture.getDrawable()).getBitmap();
        RoundedBitmapDrawable imageDrawable = RoundedBitmapDrawableFactory.create(getResources(), imageBitmap);
        imageDrawable.setCircular(true);
        imageDrawable.setCornerRadius(Math.max(imageBitmap.getWidth(), imageBitmap.getHeight()) / 2.0f);
        profilePicture.setImageDrawable(imageDrawable);
    }

    private boolean isFragmentModified(Fragment aFragment) {
        boolean retVal = false;
        if (aFragment instanceof NewWorkoutFragment) {
            retVal = ((NewWorkoutFragment) aFragment).isModified();
        } else if (aFragment instanceof EditWorkoutFragment) {
            retVal = ((EditWorkoutFragment) aFragment).isModified();
        }
        return retVal;
    }

    private Fragment getVisibleFragment() {
        List<Fragment> fragments = fragmentManager.getFragments();
        for (Fragment fragment : fragments) {
            if (fragment != null && fragment.isVisible())
                return fragment;
        }
        return null;
    }

    /**
     * Called by a fragment that wishes to end itself. It destroys that fragment and handles
     * the back stack appropriately.
     */
    public void finishFragment() {
        showPopupFlag = false;
        onBackPressed();
    }

    public void updateAccountNotificationIndicator() {
        // check if there are any unseen notifications for friend requests
        boolean showAlert = false;
        for (String username : user.getFriendRequests().keySet()) {
            if (!user.getFriendRequests().get(username).isSeen()) {
                showAlert = true;
            }
        }
        accountNotificationTV.setVisibility(showAlert ? View.VISIBLE : View.GONE);
    }

    public void updateReceivedWorkoutNotificationIndicator() {
        // check if there are any unseen notifications for received workouts
        TextView view = (TextView) nav.getMenu().findItem(R.id.nav_received_workouts).getActionView();
        view.setText(user.getUnseenReceivedWorkouts() > 0 ? String.valueOf(user.getUnseenReceivedWorkouts()) : null);
    }

    private void saveCurrentFragmentState() {
        Fragment visibleFragment = getVisibleFragment();
        if (visibleFragment == null) {
            return;
        }
        // for now, only care about saving the states of these fragments
        if (visibleFragment.getTag().equals(Variables.MY_EXERCISES_TITLE) || visibleFragment.getTag().equals(Variables.RECEIVED_WORKOUTS_TITLE)) {
            fragmentSavedStatesMap.put(visibleFragment.getTag(), fragmentManager.saveFragmentInstanceState(visibleFragment));
        }
    }

    // region Navigation Methods

    private void closeDrawerFromNavigation() {
        new Handler().postDelayed(() -> drawer.closeDrawer(GravityCompat.START), 100);
    }

    public void goToCurrentWorkout() {
        saveCurrentFragmentState();
        fragmentStack.remove(Variables.CURRENT_WORKOUT_TITLE);
        fragmentStack.add(0, Variables.CURRENT_WORKOUT_TITLE);

        fragmentManager.beginTransaction().replace(R.id.fragment_container,
                new CurrentWorkoutFragment(), Variables.CURRENT_WORKOUT_TITLE)
                .commit();
        closeDrawerFromNavigation();
    }

    public void goToMyWorkouts() {
        saveCurrentFragmentState();
        fragmentStack.remove(Variables.MY_WORKOUT_TITLE);
        fragmentStack.add(0, Variables.MY_WORKOUT_TITLE);

        fragmentManager.beginTransaction().replace(R.id.fragment_container,
                new MyWorkoutsFragment(), Variables.MY_WORKOUT_TITLE)
                .commit();
        closeDrawerFromNavigation();
    }

    public void goToNewWorkout() {
        saveCurrentFragmentState();
        fragmentStack.remove(Variables.NEW_WORKOUT_TITLE);
        fragmentStack.add(0, Variables.NEW_WORKOUT_TITLE);

        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .replace(R.id.fragment_container, new NewWorkoutFragment(), Variables.NEW_WORKOUT_TITLE)
                .commit();
    }

    public void goToEditWorkout() {
        saveCurrentFragmentState();
        fragmentStack.remove(Variables.EDIT_WORKOUT_TITLE);
        fragmentStack.add(0, Variables.EDIT_WORKOUT_TITLE);

        fragmentManager.beginTransaction().replace(R.id.fragment_container,
                new EditWorkoutFragment(), Variables.EDIT_WORKOUT_TITLE)
                .commit();
    }

    public void goToMyExercises() {
        saveCurrentFragmentState();
        fragmentStack.remove(Variables.MY_EXERCISES_TITLE);
        fragmentStack.add(0, Variables.MY_EXERCISES_TITLE);
        MyExercisesFragment myExercisesFragment = new MyExercisesFragment();
        myExercisesFragment.setInitialSavedState(fragmentSavedStatesMap.get(Variables.MY_EXERCISES_TITLE));

        fragmentManager.beginTransaction().replace(R.id.fragment_container,
                myExercisesFragment, Variables.MY_EXERCISES_TITLE)
                .commit();
        closeDrawerFromNavigation();
    }

    public void goToExerciseDetails(String exerciseId) {
        saveCurrentFragmentState();
        fragmentStack.remove(Variables.EXERCISE_DETAILS_TITLE);
        fragmentStack.add(0, Variables.EXERCISE_DETAILS_TITLE);

        Bundle arguments = new Bundle();
        arguments.putString(Variables.EXERCISE_ID, exerciseId);
        Fragment fragment = new ExerciseDetailsFragment();
        fragment.setArguments(arguments);

        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .replace(R.id.fragment_container, fragment, Variables.EXERCISE_DETAILS_TITLE)
                .commit();
    }

    public void goToNewExercise() {
        saveCurrentFragmentState();
        fragmentStack.remove(Variables.NEW_EXERCISE_TITLE);
        fragmentStack.add(0, Variables.NEW_EXERCISE_TITLE);


        Fragment fragment = new NewExerciseFragment();
        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .replace(R.id.fragment_container, fragment, Variables.NEW_EXERCISE_TITLE)
                .commit();
    }

    public void goToAccountSettings() {
        saveCurrentFragmentState();
        fragmentStack.remove(Variables.ACCOUNT_TITLE);
        fragmentStack.add(0, Variables.ACCOUNT_TITLE);

        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, new MyAccountFragment(), Variables.ACCOUNT_TITLE)
                .commit();
        closeDrawerFromNavigation();
    }

    public void goToAccountPreferences() {
        saveCurrentFragmentState();
        fragmentStack.remove(Variables.ACCOUNT_PREFS_TITLE);
        fragmentStack.add(0, Variables.ACCOUNT_PREFS_TITLE);

        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .replace(R.id.fragment_container, new AccountPreferencesFragment(), Variables.ACCOUNT_PREFS_TITLE)
                .commit();
    }

    public void goToFriendsList(Bundle extras) {
        saveCurrentFragmentState();
        fragmentStack.remove(Variables.FRIENDS_LIST_TITLE);
        fragmentStack.add(0, Variables.FRIENDS_LIST_TITLE);

        Fragment fragment = new FriendsListFragment();
        if (extras != null) {
            fragment.setArguments(extras);
        }
        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .replace(R.id.fragment_container, fragment, Variables.FRIENDS_LIST_TITLE)
                .commit();
    }

    public void goToBlockedList() {
        saveCurrentFragmentState();
        fragmentStack.remove(Variables.BLOCKED_LIST_TITLE);
        fragmentStack.add(0, Variables.BLOCKED_LIST_TITLE);

        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .replace(R.id.fragment_container, new BlockedListFragment(), Variables.BLOCKED_LIST_TITLE)
                .commit();
    }

    public void goToReceivedWorkouts() {
        saveCurrentFragmentState();
        fragmentStack.remove(Variables.RECEIVED_WORKOUTS_TITLE);
        fragmentStack.add(0, Variables.RECEIVED_WORKOUTS_TITLE);
        ReceivedWorkoutsFragment receivedWorkoutsFragment = new ReceivedWorkoutsFragment();
        receivedWorkoutsFragment.setInitialSavedState(fragmentSavedStatesMap.get(Variables.RECEIVED_WORKOUTS_TITLE));

        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, receivedWorkoutsFragment, Variables.RECEIVED_WORKOUTS_TITLE)
                .commit();
        closeDrawerFromNavigation();
    }

    public void goToBrowseReceivedWorkout(String workoutId, String workoutName) {
        saveCurrentFragmentState();
        fragmentStack.remove(Variables.RECEIVED_WORKOUT_TITLE);
        fragmentStack.add(0, Variables.RECEIVED_WORKOUT_TITLE);

        Bundle arguments = new Bundle();
        arguments.putString(SharedWorkout.SHARED_WORKOUT_ID, workoutId);
        arguments.putString(SharedWorkout.WORKOUT_NAME, workoutName);
        Fragment fragment = new BrowseReceivedWorkoutFragment();
        fragment.setArguments(arguments);

        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .replace(R.id.fragment_container, fragment, Variables.RECEIVED_WORKOUT_TITLE)
                .commit();
    }

    public void goToAppSettings() {
        saveCurrentFragmentState();
        fragmentStack.remove(Variables.SETTINGS_TITLE);
        fragmentStack.add(0, Variables.SETTINGS_TITLE);

        fragmentManager.beginTransaction().replace(R.id.fragment_container,
                new AppSettingsFragment(), Variables.SETTINGS_TITLE)
                .commit();
        closeDrawerFromNavigation();
    }

    public void goToAbout() {
        saveCurrentFragmentState();
        fragmentStack.remove(Variables.ABOUT_TITLE);
        fragmentStack.add(0, Variables.ABOUT_TITLE);

        fragmentManager.beginTransaction().replace(R.id.fragment_container,
                new AboutFragment(), Variables.ABOUT_TITLE)
                .commit();
        closeDrawerFromNavigation();
    }

    public void goToFaq() {
        saveCurrentFragmentState();
        fragmentStack.remove(Variables.FAQ_TITLE);
        fragmentStack.add(0, Variables.FAQ_TITLE);

        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .replace(R.id.fragment_container, new FaqFragment(), Variables.FAQ_TITLE)
                .commit();
    }
    //endregion
}
