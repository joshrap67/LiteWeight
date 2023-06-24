package com.joshrap.liteweight.activities;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
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

import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.fragments.*;
import com.joshrap.liteweight.managers.SelfManager;
import com.joshrap.liteweight.messages.activitymessages.AcceptedFriendRequestMessage;
import com.joshrap.liteweight.messages.activitymessages.CanceledFriendRequestMessage;
import com.joshrap.liteweight.messages.activitymessages.DeclinedFriendRequestMessage;
import com.joshrap.liteweight.messages.activitymessages.NewFriendRequestMessage;
import com.joshrap.liteweight.messages.activitymessages.ReceivedWorkoutMessage;
import com.joshrap.liteweight.messages.activitymessages.RemovedFriendMessage;
import com.joshrap.liteweight.messages.activitymessages.TimerRestartMessage;
import com.joshrap.liteweight.messages.fragmentmessages.AcceptedFriendRequestFragmentMessage;
import com.joshrap.liteweight.messages.fragmentmessages.CanceledFriendRequestFragmentMessage;
import com.joshrap.liteweight.messages.fragmentmessages.DeclinedFriendRequestFragmentMessage;
import com.joshrap.liteweight.messages.fragmentmessages.NewFriendRequestFragmentMessage;
import com.joshrap.liteweight.messages.fragmentmessages.ReceivedWorkoutFragmentMessage;
import com.joshrap.liteweight.messages.fragmentmessages.RemovedFriendFragmentMessage;
import com.joshrap.liteweight.models.Result;
import com.joshrap.liteweight.models.workout.Workout;
import com.joshrap.liteweight.managers.CurrentUserModule;
import com.joshrap.liteweight.services.SyncWorkoutService;
import com.joshrap.liteweight.utils.AndroidUtils;
import com.joshrap.liteweight.utils.ImageUtils;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;
import com.joshrap.liteweight.models.user.FriendRequest;
import com.joshrap.liteweight.models.user.SharedWorkoutInfo;
import com.joshrap.liteweight.models.user.User;
import com.joshrap.liteweight.models.UserAndWorkout;
import com.joshrap.liteweight.services.StopwatchService;
import com.joshrap.liteweight.services.TimerService;
import com.joshrap.liteweight.widgets.Stopwatch;
import com.joshrap.liteweight.widgets.Timer;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import lombok.Getter;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private boolean drawerListenerIsRegistered, shouldFinish;
    private TextView toolbarTitleTV, usernameTV;
    private NavigationView nav;
    private Toolbar toolbar;
    private FragmentManager fragmentManager;
    private ArrayList<String> fragmentStack; // stack of fragment ids
    private Map<String, Fragment.SavedState> fragmentSavedStatesMap;
    private ImageView profilePicture;
    private Workout lastSyncedWorkout; // used to determine if current workout needs to be updated on app close
    private int lastSyncedCurrentDay, lastSyncedCurrentWeek;
    private ActivityResultLauncher<String> requestNotificationPermissionLauncher;
    private ConstraintLayout navHeaderLayout;
    private ProgressBar loadingBar;

    @Getter
    private Timer timer;
    @Getter
    private Stopwatch stopwatch;

    @Inject
    SelfManager selfManager;
    @Inject
    SharedPreferences sharedPreferences;
    @Inject
    AlertDialog loadingDialog;
    @Inject
    CurrentUserModule currentUserModule;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(null); // bit of a hack, but don't want to used save instance state since if app killed by OS I get NPEs from fragments
        Injector.getInjector(this).inject(this);

        boolean darkThemeEnabled = sharedPreferences.getBoolean(Variables.DARK_THEME_ENABLED, true);
        if (darkThemeEnabled) {
            setTheme(R.style.AppTheme);
        } else {
            setTheme(R.style.AppThemeLight);
        }
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        toolbar = findViewById(R.id.toolbar);
        toolbarTitleTV = findViewById(R.id.toolbar_title);
        drawer = findViewById(R.id.drawer);
        nav = findViewById(R.id.nav_view);
        View headerView = nav.getHeaderView(0);
        navHeaderLayout = headerView.findViewById(R.id.nav_header_layout);
        usernameTV = headerView.findViewById(R.id.username_tv);
        profilePicture = headerView.findViewById(R.id.profile_picture_image);
        loadingBar = findViewById(R.id.loading_progress_bar);

        requestNotificationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                createNotificationChannel();
            } else {
                Toast.makeText(this, "Certain features of this app will not work without notifications.", Toast.LENGTH_LONG).show();
            }
        });

        loadCurrentUserAndWorkout();
    }

    private void loadCurrentUserAndWorkout() {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Result<UserAndWorkout> result = this.selfManager.getUserAndCurrentWorkout();
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                if (result.isSuccess()) {
                    if (result.getData().getUser() == null) {
                        // user is authenticated and verified, but has no account in the DB. Load activity to create this
                        launchAccountNotCreatedActivity();
                    } else {
                        // user does have an account, so load activity
                        loadActivity();
                    }
                } else {
                    launchSignInActivity(result.getErrorMessage());
                }

                loadingBar.setVisibility(View.GONE);
            });
        });
    }

    // called once main dependency - currentUserAndWorkout - is loaded
    private void loadActivity() {
        String notificationAction = null;
        if (getIntent().getExtras() != null) {
            // app was launched from a notification, we will route according to this
            notificationAction = getIntent().getExtras().getString(Variables.NOTIFICATION_ACTION);
        }

        User user = currentUserModule.getUser();
        if (currentUserModule.isWorkoutPresent()) {
            lastSyncedWorkout = new Workout(currentUserModule.getCurrentWorkout());
            lastSyncedCurrentDay = currentUserModule.getCurrentDay();
            lastSyncedCurrentWeek = currentUserModule.getCurrentWeek();
        } else {
            lastSyncedWorkout = null;
        }

        long timerDuration = sharedPreferences.getLong(Variables.TIMER_DURATION, Variables.DEFAULT_TIMER_VALUE);
        timer = new Timer(timerDuration);
        stopwatch = new Stopwatch();
        fragmentStack = new ArrayList<>();
        fragmentSavedStatesMap = new HashMap<>();
        drawerListenerIsRegistered = false;
        fragmentManager = getSupportFragmentManager();

        nav.setNavigationItemSelectedListener(this);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false); // removes the app title from the toolbar
        }

        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.nav_draw_open, R.string.nav_draw_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // default landing fragment is current workout
        fragmentManager.beginTransaction().replace(R.id.fragment_container, new CurrentWorkoutFragment(), Variables.CURRENT_WORKOUT_TITLE).commit();
        fragmentStack.add(Variables.CURRENT_WORKOUT_TITLE);
        nav.setCheckedItem(R.id.nav_current_workout);

        // doing this here just because otherwise there is a barely noticeable delay when first launching the app
        // as the title gets set slightly after other elements are visible
        toolbarTitleTV.setText(currentUserModule.isWorkoutPresent() ? currentUserModule.getCurrentWorkout().getName() : getString(R.string.app_name));

        navHeaderLayout.getBackground().setAlpha(190); // to allow for username to be seen easier against the background image
        navHeaderLayout.setOnClickListener(view -> {
            goToMyAccount();
            drawer.closeDrawer(GravityCompat.START);
            nav.setCheckedItem(R.id.nav_my_account);
        });

        usernameTV.setText(user.getUsername());
        Picasso.get()
                .load(ImageUtils.getProfilePictureUrl(user.getProfilePicture()))
                .error(R.drawable.picture_load_error)
                .networkPolicy(NetworkPolicy.NO_CACHE) // on first loading in app, always fetch online
                .into(profilePicture);

        setupNotifications();
        linkFirebaseToken();
        updateFriendsListIndicator();
        updateReceivedWorkoutNotificationIndicator();
        if (notificationAction != null) {
            // the user clicked on a notification which created this activity, so route to the appropriate fragment
            navigateToFragmentFromNotification(notificationAction);
        }
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    private void launchSignInActivity(String errorMessage) {
        Intent intent = new Intent(this, SignInActivity.class);
        if (errorMessage != null) {
            intent.putExtra(Variables.INTENT_ERROR_MESSAGE, errorMessage);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        shouldFinish = true;
    }

    private void launchAccountNotCreatedActivity() {
        Intent intent = new Intent(this, CreateAccountActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        shouldFinish = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (shouldFinish) {
            finish();
        }
    }

    private void setupNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            createNotificationChannel();
        }
    }

    /**
     * Called whenever the user clicks on a notification while the app is running or paused.
     * The models should already be updated from the event bus.
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
        // close any popup that might be showing to avoid memory leaks
        closeAllOpenDialogs();
        switch (action) {
            case Variables.INTENT_TIMER_NOTIFICATION_CLICK:
            case Variables.INTENT_STOPWATCH_NOTIFICATION_CLICK:
                goToCurrentWorkout();
                nav.setCheckedItem(R.id.nav_current_workout);
                break;
            case Variables.RECEIVED_WORKOUT_CLICK:
                nav.setCheckedItem(R.id.nav_received_workouts);
                goToReceivedWorkouts();
                break;
            case Variables.ACCEPTED_FRIEND_REQUEST_CLICK:
                goToFriendsList(null);
                break;
            case Variables.NEW_FRIEND_REQUEST_CLICK:
                Bundle extrasFriendRequest = new Bundle(); // to start the fragment on the friend request tab
                extrasFriendRequest.putInt(Variables.FRIEND_LIST_POSITION, FriendsListFragment.REQUESTS_POSITION);
                goToFriendsList(extrasFriendRequest);
                break;
        }
    }

    /**
     * This is called whenever user opens notification while app was terminated.
     * No need to update any models since that would have been taken care of from the app load
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
        // stop any timer/stopwatch services that may be running.
        stopService(new Intent(this, TimerService.class));
        stopService(new Intent(this, StopwatchService.class));
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            // if timer finished and user hasn't acknowledged the notification yet, just clear it on app termination
            notificationManager.cancel(TimerService.timerFinishedId);
        }
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        hideKeyboard();
        int itemId = menuItem.getItemId();
        if (itemId == R.id.nav_current_workout) {
            if (!(getVisibleFragment() instanceof CurrentWorkoutFragment)) {
                goToCurrentWorkout();
            }
        } else if (itemId == R.id.nav_my_workouts) {
            if (!(getVisibleFragment() instanceof MyWorkoutsFragment)) {
                goToMyWorkouts();
            }
        } else if (itemId == R.id.nav_my_exercises) {
            if (!(getVisibleFragment() instanceof MyExercisesFragment)) {
                goToMyExercises();
            }
        } else if (itemId == R.id.nav_received_workouts) {
            if (!(getVisibleFragment() instanceof ReceivedWorkoutsFragment)) {
                goToReceivedWorkouts();
            }
        } else if (itemId == R.id.nav_friends_list) {
            if (!(getVisibleFragment() instanceof FriendsListFragment)) {
                goToFriendsList(null);
            }
        } else if (itemId == R.id.nav_about) {
            if (!(getVisibleFragment() instanceof AboutFragment)) {
                goToAbout();
            }
        } else if (itemId == R.id.nav_my_account) {
            if (!(getVisibleFragment() instanceof MyAccountFragment)) {
                goToMyAccount();
            }
        }
        return true;
    }

    final OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
        // using this callback instead of method override means I can handle back presses in the fragments
        @Override
        public void handleOnBackPressed() {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                // if the user clicked the navigation panel, allow back press to close it.
                drawer.closeDrawer(GravityCompat.START);
                return;
            }

            hideKeyboard();
            if (fragmentStack.size() > 1) {
                // there's at least two fragments on the stack, so pressing back button will pop the one on the top of the stack
                popFragStack();
            } else {
                ActivityCompat.finishAffinity(MainActivity.this);
            }

        }
    };

    private void syncCurrentWorkout() {
        if (currentUserModule.isWorkoutPresent() &&
                (Workout.workoutsDifferent(lastSyncedWorkout, currentUserModule.getCurrentWorkout())
                        || (currentUserModule.getCurrentWeek() != lastSyncedCurrentWeek || currentUserModule.getCurrentDay() != lastSyncedCurrentDay))) {
            // we assume it always succeeds
            lastSyncedWorkout = new Workout(currentUserModule.getCurrentWorkout());
            lastSyncedCurrentDay = currentUserModule.getCurrentDay();
            lastSyncedCurrentWeek = currentUserModule.getCurrentWeek();

            Intent intent = new Intent(this, SyncWorkoutService.class);
            try {
                intent.putExtra(Variables.INTENT_WORKOUT, new ObjectMapper().writeValueAsString(currentUserModule.getCurrentWorkout()));
                intent.putExtra(Variables.INTENT_CURRENT_DAY, currentUserModule.getCurrentDay());
                intent.putExtra(Variables.INTENT_CURRENT_WEEK, currentUserModule.getCurrentWeek());
                startService(intent);
            } catch (JsonProcessingException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
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
            case Variables.FRIENDS_LIST_TITLE:
                goToFriendsList(null);
                nav.setCheckedItem(R.id.nav_friends_list);
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
                nav.setCheckedItem(R.id.nav_my_account);
                goToMyAccount();
                break;
            case Variables.SETTINGS_TITLE:
                goToSettings();
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
                    then go back again to get rid of it without ever showing the fragment again.

                    This would happen for example if clicking on notification when on the edit workout fragment. When clicking back the edit workout fragment
                    is immediately discarded it.
                 */
                onBackPressed();
                break;
        }
    }

    public void logout() {
        // stop any timer/stopwatch services that may be running.
        stopService(new Intent(this, TimerService.class));
        stopService(new Intent(this, StopwatchService.class));
        timer.stopTimer();
        stopwatch.stopStopwatch();
        syncCurrentWorkout();
        currentUserModule.clear();

        AndroidUtils.showLoadingDialog(loadingDialog, "Logging out...");
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            // blind send for now for unlinking firebase token
            selfManager.unlinkFirebaseMessagingToken();
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                EventBus.getDefault().unregister(this);

                // clear all notifications
                NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null) {
                    notificationManager.cancelAll();
                }

                FirebaseAuth.getInstance().signOut();

                launchSignInActivity(null);
            });
        });
    }

    // utilized when deleting account
    public void forceKill() {
        // stop any timer/stopwatch services that may be running.
        stopService(new Intent(this, TimerService.class));
        stopService(new Intent(this, StopwatchService.class));
        timer.stopTimer();
        stopwatch.stopStopwatch();
        currentUserModule.clear();
        EventBus.getDefault().unregister(this);

        // clear all notifications
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }
        FirebaseAuth.getInstance().signOut();

        launchSignInActivity(null);
    }

    // service that continues the stopwatch's progress
    public void startStopwatchService() {
        Intent serviceIntent = new Intent(this, StopwatchService.class);
        serviceIntent.putExtra(Variables.INTENT_ABSOLUTE_START_TIME, stopwatch.startTimeAbsolute);
        serviceIntent.putExtra(Variables.INTENT_STOPWATCH_INITIAL_ELAPSED_TIME, stopwatch.initialElapsedTime);
        startForegroundService(serviceIntent);
    }

    public void cancelStopwatchService() {
        stopService(new Intent(this, StopwatchService.class));

        // get rid of any notifications that are still showing now that the stopwatch is on the screen
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(StopwatchService.stopwatchRunningId);
    }

    public void startTimerService() {
        Intent serviceIntent = new Intent(this, TimerService.class);
        serviceIntent.putExtra(Variables.INTENT_ABSOLUTE_START_TIME, timer.startTimeAbsolute);
        serviceIntent.putExtra(Variables.INTENT_TIMER_INITIAL_TIME_REMAINING, timer.initialTimeRemaining);
        serviceIntent.putExtra(Variables.INTENT_TIMER_DURATION, timer.timerDuration);
        startForegroundService(serviceIntent);
    }

    public void cancelTimerService() {
        stopService(new Intent(this, TimerService.class));

        // get rid of any notifications that are still showing now that the timer is on the screen
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(TimerService.timerRunningId);
        notificationManager.cancel(TimerService.timerFinishedId);
    }

    //region Subscriptions

    // individual fragments handle the ui changes, these methods should only be updating the models.
    // Admittedly this is where mvvm could take over but im not redoing the entire app
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleTimerRestartMessage(TimerRestartMessage event) {
        timer.initialTimeRemaining = event.getTimeRemaining();
        timer.startTimeAbsolute = event.getStartTimeAbsolute();
        // if receiving this we can assume the timer finished and should be restarted
        timer.startTimer();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleNewFriendRequestMessage(NewFriendRequestMessage event) {
        FriendRequest friendRequest = event.getFriendRequest();
        currentUserModule.getUser().addFriendRequest(friendRequest);
        updateFriendsListIndicator();

        // send broadcast to any fragments waiting on this model update
        NewFriendRequestFragmentMessage fragmentMessage = new NewFriendRequestFragmentMessage(friendRequest);
        EventBus.getDefault().post(fragmentMessage);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleCanceledFriendRequestMessage(CanceledFriendRequestMessage event) {
        String userIdToRemove = event.getUserIdToRemove();
        currentUserModule.getUser().removeFriendRequest(userIdToRemove);
        updateFriendsListIndicator();

        // send broadcast to any fragments waiting on this model update
        CanceledFriendRequestFragmentMessage fragmentMessage = new CanceledFriendRequestFragmentMessage(userIdToRemove);
        EventBus.getDefault().post(fragmentMessage);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleDeclinedFriendRequestMessage(DeclinedFriendRequestMessage event) {
        String userIdToRemove = event.getUserIdToRemove();
        currentUserModule.getUser().removeFriend(userIdToRemove);

        // send broadcast to any fragments waiting on this model update
        DeclinedFriendRequestFragmentMessage fragmentMessage = new DeclinedFriendRequestFragmentMessage(userIdToRemove);
        EventBus.getDefault().post(fragmentMessage);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleRemovedAsFriendMessage(RemovedFriendMessage event) {
        String userIdToRemove = event.getUserIdToRemove();
        currentUserModule.getUser().removeFriend(userIdToRemove);

        // send broadcast to any fragments waiting on this model update
        RemovedFriendFragmentMessage fragmentMessage = new RemovedFriendFragmentMessage(userIdToRemove);
        EventBus.getDefault().post(fragmentMessage);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleAcceptedFriendRequestMessage(AcceptedFriendRequestMessage message) {
        String userIdAccepted = message.getAcceptedUserId();
        currentUserModule.getUser().getFriend(userIdAccepted).setConfirmed(true);

        // send broadcast to any fragments waiting on this model update
        AcceptedFriendRequestFragmentMessage fragmentMessage = new AcceptedFriendRequestFragmentMessage(userIdAccepted);
        EventBus.getDefault().post(fragmentMessage);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleReceivedWorkoutMessage(ReceivedWorkoutMessage event) {
        SharedWorkoutInfo sharedWorkoutInfo = event.getSharedWorkoutInfo();

        currentUserModule.getUser().addReceivedWorkout(sharedWorkoutInfo);
        updateReceivedWorkoutNotificationIndicator();

        // send broadcast to any fragments waiting on this model update
        ReceivedWorkoutFragmentMessage fragmentMessage = new ReceivedWorkoutFragmentMessage(sharedWorkoutInfo);
        EventBus.getDefault().post(fragmentMessage);
    }

    //endregion

    private void linkFirebaseToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                return;
            }

            // Get new Instance ID token
            String token = task.getResult();
            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                // blind send for now for updating notification token
                selfManager.setFirebaseMessagingToken(token);
            });
        });
    }


    private void createNotificationChannel() {
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
        stopwatchRunningChannel.setSound(null, null);
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

    private void closeAllOpenDialogs() {
        Fragment currentFragment = getVisibleFragment();
        if (currentFragment instanceof FragmentWithDialog) {
            ((FragmentWithDialog) currentFragment).hideAllDialogs();
        }
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

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view != null) {
            view.clearFocus();
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Called by other fragments to change the string that the toolbar displays.
     *
     * @param title new title for the toolbar.
     */
    public void updateToolbarTitle(String title) {
        toolbarTitleTV.setText(title);
    }

    public void updateProfilePicture(Uri uri) {
        profilePicture.setImageURI(uri);
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
        onBackPressed();
    }

    public void updateFriendsListIndicator() {
        // check if there are any unseen notifications for friend requests
        TextView view = (TextView) nav.getMenu().findItem(R.id.nav_friends_list).getActionView();
        view.setText(null);
        for (FriendRequest friendRequest : currentUserModule.getUser().getFriendRequests()) {
            if (!friendRequest.isSeen()) {
                view.setText(R.string.alert);
                return;
            }
        }
    }

    public void clearAccountNotificationIndicator() {
        TextView view = (TextView) nav.getMenu().findItem(R.id.nav_friends_list).getActionView();
        view.setText(null);
    }

    public void updateReceivedWorkoutNotificationIndicator() {
        // check if there are any unseen notifications for received workouts
        TextView view = (TextView) nav.getMenu().findItem(R.id.nav_received_workouts).getActionView();
        long unseenCount = currentUserModule.getUser().getReceivedWorkouts().stream().filter(x -> !x.isSeen()).count();
        view.setText(unseenCount > 0 ? String.valueOf(unseenCount) : null);
    }

    public void updateReceivedWorkoutNotificationIndicator(long count) {
        // fragments manually set the indicator in cases of blind sends (ik ik, MVVM is where this could shine)
        TextView view = (TextView) nav.getMenu().findItem(R.id.nav_received_workouts).getActionView();
        view.setText(count > 0 ? String.valueOf(count) : null);
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

    public void goToCreateWorkout() {
        saveCurrentFragmentState();
        fragmentStack.remove(Variables.CREATE_WORKOUT_TITLE);
        fragmentStack.add(0, Variables.CREATE_WORKOUT_TITLE);

        Bundle arguments = new Bundle();
        arguments.putBoolean(Variables.EXISTING_WORKOUT, false);
        Fragment fragment = new PendingWorkoutFragment();
        fragment.setArguments(arguments);

        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.zoom_out, R.anim.fragment_exit)
                .replace(R.id.fragment_container, fragment, Variables.CREATE_WORKOUT_TITLE)
                .commit();
    }

    public void goToEditWorkout() {
        saveCurrentFragmentState();
        fragmentStack.remove(Variables.EDIT_WORKOUT_TITLE);
        fragmentStack.add(0, Variables.EDIT_WORKOUT_TITLE);

        Bundle arguments = new Bundle();
        arguments.putBoolean(Variables.EXISTING_WORKOUT, true);
        Fragment fragment = new PendingWorkoutFragment();
        fragment.setArguments(arguments);

        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.zoom_out, R.anim.fragment_exit)
                .replace(R.id.fragment_container, fragment, Variables.EDIT_WORKOUT_TITLE)
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
                .setCustomAnimations(R.anim.zoom_out, R.anim.fragment_exit)
                .replace(R.id.fragment_container, fragment, Variables.EXERCISE_DETAILS_TITLE)
                .commit();
    }

    public void goToNewExercise() {
        saveCurrentFragmentState();
        fragmentStack.remove(Variables.NEW_EXERCISE_TITLE);
        fragmentStack.add(0, Variables.NEW_EXERCISE_TITLE);


        Fragment fragment = new NewExerciseFragment();
        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.zoom_out, R.anim.fragment_exit)
                .replace(R.id.fragment_container, fragment, Variables.NEW_EXERCISE_TITLE)
                .commit();
    }

    public void goToMyAccount() {
        saveCurrentFragmentState();
        fragmentStack.remove(Variables.ACCOUNT_TITLE);
        fragmentStack.add(0, Variables.ACCOUNT_TITLE);

        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, new MyAccountFragment(), Variables.ACCOUNT_TITLE)
                .commit();
        closeDrawerFromNavigation();
    }

    public void goToSettings() {
        saveCurrentFragmentState();
        fragmentStack.remove(Variables.SETTINGS_TITLE);
        fragmentStack.add(0, Variables.SETTINGS_TITLE);

        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.zoom_out, R.anim.fragment_exit)
                .replace(R.id.fragment_container, new SettingsFragment(), Variables.SETTINGS_TITLE)
                .commit();
    }

    public void goToChangePassword() {
        saveCurrentFragmentState();
        fragmentStack.remove(Variables.CHANGE_PASSWORD);
        fragmentStack.add(0, Variables.CHANGE_PASSWORD);

        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.zoom_out, R.anim.fragment_exit)
                .replace(R.id.fragment_container, new ChangePasswordFragment(), Variables.CHANGE_PASSWORD)
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
                .replace(R.id.fragment_container, fragment, Variables.FRIENDS_LIST_TITLE)
                .commit();
        closeDrawerFromNavigation();
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
        arguments.putString(Variables.SHARED_WORKOUT_ID, workoutId);
        arguments.putString(Variables.WORKOUT_NAME, workoutName);
        Fragment fragment = new BrowseReceivedWorkoutFragment();
        fragment.setArguments(arguments);

        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.zoom_out, R.anim.fragment_exit)
                .replace(R.id.fragment_container, fragment, Variables.RECEIVED_WORKOUT_TITLE)
                .commit();
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
                .setCustomAnimations(R.anim.zoom_out, R.anim.fragment_exit)
                .replace(R.id.fragment_container, new FaqFragment(), Variables.FAQ_TITLE)
                .commit();
    }
    //endregion
}
