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
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.fragments.*;
import com.joshrap.liteweight.helpers.ImageHelper;
import com.joshrap.liteweight.helpers.JsonParser;
import com.joshrap.liteweight.imports.Globals;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;
import com.joshrap.liteweight.models.FriendRequest;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.Tokens;
import com.joshrap.liteweight.network.RequestFields;
import com.joshrap.liteweight.network.repos.UserRepository;
import com.joshrap.liteweight.services.StopwatchService;
import com.joshrap.liteweight.services.SyncRoutineService;
import com.joshrap.liteweight.services.TimerService;
import com.joshrap.liteweight.widgets.Stopwatch;
import com.joshrap.liteweight.widgets.Timer;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

public class WorkoutActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;
    private AlertDialog alertDialog;
    private ActionBarDrawerToggle toggle;
    private boolean drawerListenerIsRegistered = false;
    private TextView toolbarTitleTV, notificationTV;
    private NavigationView nav;
    private FragmentManager fragmentManager;
    private boolean showPopupFlag;
    private ArrayList<String> fragmentStack = new ArrayList<>();
    private Timer timer;
    private Stopwatch stopwatch;
    private ProgressDialog loadingDialog;
    // save these as variables since the user can click around and it is ideal to preserve the view that they altered
    private ActiveWorkoutFragment currentWorkoutFragment;
    @Inject
    public Tokens tokens;
    @Inject
    public UserRepository userRepository;
    @Inject
    public SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String action = null;
        String jsonData = null;
        if (getIntent().getExtras() != null) {
            action = getIntent().getAction();
            jsonData = getIntent().getExtras().getString(Variables.INTENT_NOTIFICATION_DATA);
        }
        loadingDialog = new ProgressDialog(this);
        Injector.getInjector(this).inject(this);

        IntentFilter receiverActions = new IntentFilter();
        receiverActions.addAction(Variables.NEW_FRIEND_REQUEST_CLICK);
        receiverActions.addAction(Variables.NEW_FRIEND_REQUEST_BROADCAST);
        receiverActions.addAction(Variables.CANCELED_FRIEND_REQUEST);
        LocalBroadcastManager.getInstance(this).registerReceiver(notificationReceiver,
                receiverActions);

        setContentView(R.layout.activity_workout);
        timer = new Timer(this);
        stopwatch = new Stopwatch(this);
        showPopupFlag = true;
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbarTitleTV = findViewById(R.id.toolbar_title);
        drawer = findViewById(R.id.drawer);
        nav = findViewById(R.id.nav_view);
        TextView logoutButton = findViewById(R.id.log_out_btn);
        nav.setNavigationItemSelectedListener(this);
        fragmentManager = getSupportFragmentManager();
        logoutButton.setOnClickListener(view -> promptLogout());
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
            currentWorkoutFragment = new ActiveWorkoutFragment();
            fragmentManager.beginTransaction().replace(R.id.fragment_container,
                    currentWorkoutFragment, Variables.CURRENT_WORKOUT_TITLE).commit();
            fragmentStack.add(Variables.CURRENT_WORKOUT_TITLE);
            nav.setCheckedItem(R.id.nav_current_workout);
        }
        View headerView = nav.getHeaderView(0);
        ConstraintLayout headerLayout = headerView.findViewById(R.id.nav_header);
        headerLayout.setOnClickListener(view -> {
            goToAccountSettings();
            drawer.closeDrawer(GravityCompat.START);
        });
        notificationTV = headerView.findViewById(R.id.notification_tv);
        final ImageView profilePicture = headerView.findViewById(R.id.profile_picture);
        Picasso.get()
                .load(ImageHelper.getIconUrl(Globals.user.getIcon()))
                .error(R.drawable.new_icon_round)
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
        final TextView usernameTV = headerView.findViewById(R.id.username_tv);
        usernameTV.setText(Globals.user.getUsername());

        createNotificationChannel();
        updateEndpointToken();
        if (action != null && jsonData != null) {
            // means the user clicked on a notification which created this activity, so take them to the appropriate fragment
            navigateToFragmentFromNotification(action, jsonData);
        }
        updateNotificationIndicator();
    }

    private void navigateToFragmentFromNotification(String action, String jsonData) {
        if (action == null) {
            return;
        }
        // note that this is called whenever user opens notification while app was terminated. So no need to update any models
        if (Variables.NEW_FRIEND_REQUEST_CLICK.equals(action)) {
            Bundle extras = new Bundle(); // to start the fragment on the friend request tab
            extras.putInt(Variables.FRIEND_LIST_POSITION, FriendsListFragment.REQUESTS_POSITION);
            goToFriendsList(extras);
        }
    }

    @Override
    protected void onPause() {
        if (Globals.activeWorkout != null) {
            Intent intent = new Intent(this, SyncRoutineService.class);
            intent.putExtra(Variables.INTENT_REFRESH_TOKEN, tokens.getRefreshToken());
            intent.putExtra(Variables.INTENT_ID_TOKEN, tokens.getIdToken());
            try {
                intent.putExtra(RequestFields.WORKOUT, new ObjectMapper().writeValueAsString(Globals.activeWorkout.asMap()));
                startService(intent);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // stop any timer/stopwatch services that may be running.
        stopService(new Intent(this, TimerService.class));
        stopService(new Intent(this, StopwatchService.class));
        // todo clear any timer notification
        // update tokens just in case they changed in apps life cycle
        LocalBroadcastManager.getInstance(this).unregisterReceiver(notificationReceiver);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Variables.REFRESH_TOKEN_KEY, tokens.getRefreshToken());
        editor.putString(Variables.ID_TOKEN_KEY, tokens.getIdToken());
        editor.apply();

        super.onDestroy();
    }

    private void showLoadingDialog(String message) {
        loadingDialog.setMessage(message);
        loadingDialog.show();
    }

    private void logout() {
        // stop any timer/stopwatch services that may be running.
        stopService(new Intent(this, TimerService.class));
        stopService(new Intent(this, StopwatchService.class));
        if (Globals.activeWorkout != null) {
            Intent intent = new Intent(this, SyncRoutineService.class);
            intent.putExtra(Variables.INTENT_REFRESH_TOKEN, tokens.getRefreshToken());
            intent.putExtra(Variables.INTENT_ID_TOKEN, tokens.getIdToken());
            try {
                intent.putExtra(RequestFields.WORKOUT, new ObjectMapper().writeValueAsString(Globals.activeWorkout.asMap()));
                startService(intent);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        showLoadingDialog("Logging out...");
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            // blind send for now for removing notification endpoint id
            ResultStatus<String> resultStatus = userRepository.removeEndpointId();
            // doing this all in the same thread to avoid potential race condition of deleting tokens while trying to make api call
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                // todo remove any notifications
                loadingDialog.dismiss();
                // clear the current tokens
                LocalBroadcastManager.getInstance(this).unregisterReceiver(notificationReceiver);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove(Variables.REFRESH_TOKEN_KEY);
                editor.remove(Variables.ID_TOKEN_KEY);
                editor.apply();
                // clear all notifications
                NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null) {
                    notificationManager.cancelAll();
                }
                // since tokens are singleton, need to update it to null here
                tokens.setRefreshToken(null);
                tokens.setIdToken(null);
                timer.stopTimer();
                stopwatch.stopStopwatch();


                Globals.user = null;
                Globals.activeWorkout = null;
                Globals.timerServiceRunning = false;
                Globals.stopwatchServiceRunning = false;
                // take user back to sign in activity
                Intent intent = new Intent(this, SignInActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            });
        });
    }

    private void updateEndpointToken() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("WorkoutActivity", "getInstanceId failed", task.getException());
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

    private void createNotificationChannel() {
        /*
            Sets up a notification channel for each channel in the app. Each channel is preset with
            notification options but these can always be changed by the user.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // channel for when the timer is running but not finished
            NotificationChannel timerRunningChannel = new NotificationChannel(
                    Variables.TIMER_RUNNING_CHANNEL,
                    "Timer",
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
                    "Stopwatch",
                    NotificationManager.IMPORTANCE_DEFAULT);
            manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(stopwatchRunningChannel);

            // channel for incoming friend requests
            NotificationChannel friendRequestChannel = new NotificationChannel(
                    Variables.FRIEND_REQUEST_CHANNEL,
                    "Friend Requests",
                    NotificationManager.IMPORTANCE_DEFAULT);
            manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(friendRequestChannel);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        /*
            Called whenever the user clicks on a notification.
         */
        super.onNewIntent(intent); // this has to be first to not crash app when clicking notification when app is in background
        String action = intent.getAction();
        // todo move this to broadcast receiver
        if (action != null && (action.equals(Variables.INTENT_TIMER_NOTIFICATION_CLICK)
                || action.equals(Variables.INTENT_STOPWATCH_NOTIFICATION_CLICK))) {
            // close any popup that might be showing
            closeAllOpenDialogs();
            goToCurrentWorkout();
            nav.setCheckedItem(R.id.nav_current_workout);
        }
    }

    private final BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                return;
            }
            // todo receive new icon broadcast
            if (action.equals(Variables.NEW_FRIEND_REQUEST_CLICK)) {
                // called when app is in background (not terminated) and user clicks notification
                FriendRequest friendRequest;
                try {
                    // sanity check update, this should always be taken care of in the branch below but doing it again to be sure
                    friendRequest = new FriendRequest(JsonParser.deserialize((String) intent.getExtras().get(Variables.INTENT_NOTIFICATION_DATA)));
                    Globals.user.getFriendRequests().put(friendRequest.getUsername(), friendRequest);

                    Fragment visibleFragment = getVisibleFragment();
                    if (visibleFragment instanceof FriendsListFragment) {
                        ((FriendsListFragment) visibleFragment).addFriendRequestToList(friendRequest);
                    } else {
                        // not currently on the friends list fragment, so go there
                        closeAllOpenDialogs();
                        Bundle extras = new Bundle(); // to start the fragment on the friend request tab
                        extras.putInt(Variables.FRIEND_LIST_POSITION, FriendsListFragment.REQUESTS_POSITION);
                        goToFriendsList(extras);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (action.equals(Variables.NEW_FRIEND_REQUEST_BROADCAST)) {
                // called when user has app open in foreground
                FriendRequest friendRequest;
                try {
                    friendRequest = new FriendRequest(JsonParser.deserialize((String) intent.getExtras().get(Variables.INTENT_NOTIFICATION_DATA)));
                    Globals.user.getFriendRequests().put(friendRequest.getUsername(), friendRequest);

                    Fragment visibleFragment = getVisibleFragment();
                    if (visibleFragment instanceof FriendsListFragment) {
                        ((FriendsListFragment) visibleFragment).addFriendRequestToList(friendRequest);
                        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        if (mNotificationManager != null) {
                            // user is on this page, so no need to show a push notification
                            mNotificationManager.cancel(friendRequest.getUsername().hashCode());
                        }
                    }
                    updateNotificationIndicator();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (action.equals(Variables.CANCELED_FRIEND_REQUEST)) {
                String usernameToRemove = intent.getExtras().get(Variables.INTENT_NOTIFICATION_DATA).toString();
                Globals.user.getFriendRequests().remove(usernameToRemove);
                updateNotificationIndicator();
                Fragment visibleFragment = getVisibleFragment();
                if (visibleFragment instanceof FriendsListFragment) {
                    ((FriendsListFragment) visibleFragment).removeFriendRequestFromList(usernameToRemove);
                }
            }
        }
    };

    private void closeAllOpenDialogs() {
        Fragment currentFragment = getVisibleFragment();
        if (currentFragment instanceof FragmentWithDialog) {
            ((FragmentWithDialog) currentFragment).hideAllDialogs();
        }

        // i no longer care about asking if user is editing or not. If they click the notification and have unsaved changes they're SOL
        if (currentFragment instanceof NewWorkoutFragment) {
            // can't ever go back to the new workout fragment since it's only accessible within the my workout fragment
            fragmentStack.remove(Variables.NEW_WORKOUT_TITLE);
        } else if (currentFragment instanceof EditWorkoutFragment) {
            fragmentStack.remove(Variables.EDIT_WORKOUT_TITLE);
        }
    }

    public void toggleBackButton(boolean enable) {
        /*
            Found on SO. Shows the back button instead of the hamburger icon for the drawer menu
         */
        if (enable) {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            toggle.setDrawerIndicatorEnabled(false);
            // Show back button
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if (!drawerListenerIsRegistered) {
                toggle.setToolbarNavigationClickListener(v -> onBackPressed());
                drawerListenerIsRegistered = true;
            }
        } else {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            // Remove back button
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            toggle.setDrawerIndicatorEnabled(true);
            // Remove the/any drawer toggle listener
            toggle.setToolbarNavigationClickListener(null);
            drawerListenerIsRegistered = false;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        /*
            Called whenever an element in the nav menu is selected
         */
        switch (menuItem.getItemId()) {
            case R.id.nav_current_workout:
                if (!(getVisibleFragment() instanceof ActiveWorkoutFragment)) {
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
        drawer.closeDrawer(GravityCompat.START);
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
                showUnsavedChangesNewWorkoutPopup();
                return;
            }
        } else if (visibleFragment instanceof EditWorkoutFragment) {
            if (modified && showPopupFlag) {
                // workout is being edited, so give user option to prevent fragment from closing from back press
                showUnsavedChangesEditWorkoutPopup();
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

    public void popFragStack() {
        /*
            Uses a custom stack to handle navigation of fragments. Always pops the top and then goes to
            which ever fragment is now on the top of the stack.
            The frag stack will only have one instance of the fragments in it at all times, handled by the
            navigation methods.
         */
        showPopupFlag = true;
        fragmentStack.remove(0);

        String frag = fragmentStack.get(0);
        switch (frag) {
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

    public void promptLogout() {
        /*
            Is called whenever the user has unfinished work in the create workout fragment.
         */
        alertDialog = new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out? If so, all your data will be saved in the cloud.")
                .setPositiveButton("Yes", (dialog, which) -> logout())
                .setNegativeButton("No", null)
                .create();
        alertDialog.show();
        // make the message font a little bigger than the default one provided by the alertdialog
        TextView messageTV = alertDialog.getWindow().findViewById(android.R.id.message);
        messageTV.setTextSize(18);
    }

    public void showUnsavedChangesNewWorkoutPopup() {
        /*
            Is called whenever the user has unfinished work in the create workout fragment.
         */
        alertDialog = new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                .setTitle("Unsaved Changes")
                .setMessage(R.string.unsaved_workout_msg)
                .setPositiveButton("Yes", (dialog, which) -> {
                    showPopupFlag = false;
                    onBackPressed();
                })
                .setNegativeButton("No", null)
                .create();
        alertDialog.show();
        // make the message font a little bigger than the default one provided by the alertdialog
        TextView messageTV = alertDialog.getWindow().findViewById(android.R.id.message);
        messageTV.setTextSize(18);
    }

    public void showUnsavedChangesEditWorkoutPopup() {
        /*
            Is called whenever the user has unfinished work in the edit workout fragment.
         */
        alertDialog = new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                .setTitle("Unsaved Changes")
                .setMessage(R.string.popup_message_edit_workout)
                .setPositiveButton("Yes", (dialog, which) -> {
                    showPopupFlag = false;
                    onBackPressed();
                })
                .setNegativeButton("No", null)
                .create();
        alertDialog.show();
        // make the message font a little bigger than the default one provided by the alertdialog
        TextView messageTV = alertDialog.getWindow().findViewById(android.R.id.message);
        messageTV.setTextSize(18);
    }

    public void updateToolbarTitle(String aTitle) {
        /*
            Called by other fragments to change the string that the toolbar displays.
         */
        toolbarTitleTV.setText(aTitle);

    }

    public Timer getTimer() {
        /*
            Returns the timer associated with this activity to any fragments needing it. Because the
            timer is attached to this activity, it will continue to run in the background until the app is
            terminated.
         */
        return timer;
    }

    public Stopwatch getStopwatch() {
        /*
            Returns the stopwatch associated with this activity to any fragments needing it. Because the
            stopwatch is attached to this activity, it will continue to run in the background until the app is
            terminated.
         */
        return stopwatch;
    }

    public boolean isFragmentModified(Fragment aFragment) {
        /*
            Checks if passed in fragment has been modified
         */
        boolean retVal = false;
        if (aFragment instanceof NewWorkoutFragment) {
            retVal = ((NewWorkoutFragment) aFragment).isModified();
        } else if (aFragment instanceof EditWorkoutFragment) {
            retVal = ((EditWorkoutFragment) aFragment).isModified();
        }
        return retVal;
    }

    private Fragment getVisibleFragment() {
        /*
            Returns the current fragment in focus
         */
        List<Fragment> fragments = fragmentManager.getFragments();
        for (Fragment fragment : fragments) {
            if (fragment != null && fragment.isVisible())
                return fragment;
        }
        return null;
    }

    public void finishCreateWorkout() {
        /*
            Called by the new workout fragment once a workout is successfully made. It destroys that fragment and handles
            the back stack appropriately.
         */
        showPopupFlag = false;
        onBackPressed();
    }

    public void closeExerciseDetails() {
        /*
            Called by the new workout fragment once a exercise is successfully destroyed.
            It destroys that fragment and handles the back stack appropriately.
         */
        showPopupFlag = false;
        onBackPressed();
    }

    public void updateNotificationIndicator() {
        // check if there are any unseen notifications
        boolean showAlert = false;
        for (String username : Globals.user.getFriendRequests().keySet()) {
            if (!Globals.user.getFriendRequests().get(username).isSeen()) {
                showAlert = true;
            }
        }
        // todo do this for workouts received as well

        notificationTV.setVisibility(showAlert ? View.VISIBLE : View.GONE);
    }

    // region Navigation Methods
    public void goToCurrentWorkout() {
        if (fragmentStack.contains(Variables.CURRENT_WORKOUT_TITLE)) {
            fragmentStack.remove(Variables.CURRENT_WORKOUT_TITLE);
            fragmentStack.add(0, Variables.CURRENT_WORKOUT_TITLE);
        } else {
            fragmentStack.add(0, Variables.CURRENT_WORKOUT_TITLE);
        }
        // keeping the fragment as a variable to ensure the timer display seamlessly updates when going back to it
        if (currentWorkoutFragment == null) {
            currentWorkoutFragment = new ActiveWorkoutFragment();
        }
        fragmentManager.beginTransaction().replace(R.id.fragment_container,
                currentWorkoutFragment, Variables.CURRENT_WORKOUT_TITLE)
                .commit();
    }

    public void goToMyExercises() {
        if (fragmentStack.contains(Variables.MY_EXERCISES_TITLE)) {
            fragmentStack.remove(Variables.MY_EXERCISES_TITLE);
            fragmentStack.add(0, Variables.MY_EXERCISES_TITLE);
        } else {
            fragmentStack.add(0, Variables.MY_EXERCISES_TITLE);
        }

        fragmentManager.beginTransaction().replace(R.id.fragment_container,
                new MyExercisesFragment(), Variables.MY_EXERCISES_TITLE)
                .commit();
    }

    public void goToExerciseDetails(String exerciseId) {
        if (fragmentStack.contains(Variables.EXERCISE_DETAILS_TITLE)) {
            fragmentStack.remove(Variables.EXERCISE_DETAILS_TITLE);
            fragmentStack.add(0, Variables.EXERCISE_DETAILS_TITLE);
        } else {
            fragmentStack.add(0, Variables.EXERCISE_DETAILS_TITLE);
        }

        Bundle arguments = new Bundle();
        arguments.putString(Variables.EXERCISE_ID, exerciseId);
        Fragment fragment = new ExerciseDetailsFragment();
        fragment.setArguments(arguments);

        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .replace(R.id.fragment_container, fragment, Variables.EXERCISE_DETAILS_TITLE)
                .commit();
    }

    public void goToNewWorkout() {
        if (fragmentStack.contains(Variables.NEW_WORKOUT_TITLE)) {
            fragmentStack.remove(Variables.NEW_WORKOUT_TITLE);
            fragmentStack.add(0, Variables.NEW_WORKOUT_TITLE);
        } else {
            fragmentStack.add(0, Variables.NEW_WORKOUT_TITLE);
        }
        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .replace(R.id.fragment_container, new NewWorkoutFragment(), Variables.NEW_WORKOUT_TITLE)
                .commit();
    }

    public void goToEditWorkout() {
        if (fragmentStack.contains(Variables.EDIT_WORKOUT_TITLE)) {
            fragmentStack.remove(Variables.EDIT_WORKOUT_TITLE);
            fragmentStack.add(0, Variables.EDIT_WORKOUT_TITLE);
        } else {
            fragmentStack.add(0, Variables.EDIT_WORKOUT_TITLE);
        }
        fragmentManager.beginTransaction().replace(R.id.fragment_container,
                new EditWorkoutFragment(), Variables.EDIT_WORKOUT_TITLE)
                .commit();
    }

    public void goToMyWorkouts() {
        if (fragmentStack.contains(Variables.MY_WORKOUT_TITLE)) {
            fragmentStack.remove(Variables.MY_WORKOUT_TITLE);
            fragmentStack.add(0, Variables.MY_WORKOUT_TITLE);
        } else {
            fragmentStack.add(0, Variables.MY_WORKOUT_TITLE);
        }
        fragmentManager.beginTransaction().replace(R.id.fragment_container,
                new MyWorkoutsFragment(), Variables.MY_WORKOUT_TITLE)
                .commit();
    }

    public void goToAppSettings() {
        if (fragmentStack.contains(Variables.SETTINGS_TITLE)) {
            fragmentStack.remove(Variables.SETTINGS_TITLE);
            fragmentStack.add(0, Variables.SETTINGS_TITLE);
        } else {
            fragmentStack.add(0, Variables.SETTINGS_TITLE);
        }
        fragmentManager.beginTransaction().replace(R.id.fragment_container,
                new AppSettingsFragment(), Variables.SETTINGS_TITLE)
                .commit();
    }

    public void goToAccountPreferences() {
        if (fragmentStack.contains(Variables.ACCOUNT_PREFS_TITLE)) {
            fragmentStack.remove(Variables.ACCOUNT_PREFS_TITLE);
            fragmentStack.add(0, Variables.ACCOUNT_PREFS_TITLE);
        } else {
            fragmentStack.add(0, Variables.ACCOUNT_PREFS_TITLE);
        }
        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .replace(R.id.fragment_container, new AccountPreferencesFragment(), Variables.ACCOUNT_PREFS_TITLE)
                .commit();
    }

    public void goToAccountSettings() {
        if (fragmentStack.contains(Variables.ACCOUNT_TITLE)) {
            fragmentStack.remove(Variables.ACCOUNT_TITLE);
            fragmentStack.add(0, Variables.ACCOUNT_TITLE);
        } else {
            fragmentStack.add(0, Variables.ACCOUNT_TITLE);
        }
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, new MyAccountFragment(), Variables.ACCOUNT_TITLE)
                .commit();
    }

    public void goToFriendsList(Bundle extras) {
        if (fragmentStack.contains(Variables.FRIENDS_LIST_TITLE)) {
            fragmentStack.remove(Variables.FRIENDS_LIST_TITLE);
            fragmentStack.add(0, Variables.FRIENDS_LIST_TITLE);
        } else {
            fragmentStack.add(0, Variables.FRIENDS_LIST_TITLE);
        }
        Fragment fragment = new FriendsListFragment();
        if (extras != null) {
            fragment.setArguments(extras);
        }
        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .replace(R.id.fragment_container, fragment, Variables.FRIENDS_LIST_TITLE)
                .commit();
    }

    public void goToAbout() {
        if (fragmentStack.contains(Variables.ABOUT_TITLE)) {
            fragmentStack.remove(Variables.ABOUT_TITLE);
            fragmentStack.add(0, Variables.ABOUT_TITLE);
        } else {
            fragmentStack.add(0, Variables.ABOUT_TITLE);
        }
        fragmentManager.beginTransaction().replace(R.id.fragment_container,
                new AboutFragment(), Variables.ABOUT_TITLE)
                .commit();
    }
    //endregion
}
