package com.joshrap.liteweight.activities;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Build;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.navigation.NavigationView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;

import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.joshrap.liteweight.R;
import com.joshrap.liteweight.fragments.*;
import com.joshrap.liteweight.imports.Globals;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;
import com.joshrap.liteweight.network.ApiGateway;
import com.joshrap.liteweight.network.RequestFields;
import com.joshrap.liteweight.services.StopwatchService;
import com.joshrap.liteweight.services.SyncRoutineService;
import com.joshrap.liteweight.services.TimerService;
import com.joshrap.liteweight.widgets.Stopwatch;
import com.joshrap.liteweight.widgets.Timer;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class WorkoutActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;
    private AlertDialog alertDialog;
    private ActionBarDrawerToggle toggle;
    private boolean drawerListenerIsRegistered = false;
    private TextView toolbarTitleTV;
    private NavigationView nav;
    private ProgressBar progressBar;
    private Bundle state;
    private Toolbar toolbar;
    private FragmentManager fragmentManager;
    private boolean showPopupFlag;
    private ArrayList<String> fragmentStack = new ArrayList<>();
    private Timer timer;
    private Stopwatch stopwatch;
    // save these as variables since the user can click around and it is ideal to preserve the view that they altered
    private ActiveWorkoutFragment currentWorkoutFragment;
    private MyExercisesFragment myExercisesFragment;
    @Inject
    public ApiGateway apiGateway;
    @Inject
    public SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.getInjector(this).inject(this);
        createNotificationChannel();
        setContentView(R.layout.workout_activity);
        timer = new Timer(this);
        stopwatch = new Stopwatch(this);
        showPopupFlag = true;
        toolbar = findViewById(R.id.toolbar);
        toolbarTitleTV = findViewById(R.id.toolbar_title);
        progressBar = findViewById(R.id.progress_bar);
        drawer = findViewById(R.id.drawer);
        nav = findViewById(R.id.nav_view);
        state = savedInstanceState;
        fragmentManager = getSupportFragmentManager();
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false); // removes the app title from the toolbar
        }

        initViews();
    }

    @Override
    protected void onPause() {
        Intent intent = new Intent(this, SyncRoutineService.class);
        intent.putExtra(Variables.INTENT_REFRESH_TOKEN, apiGateway.getTokens().getRefreshToken());
        intent.putExtra(Variables.INTENT_ID_TOKEN, apiGateway.getTokens().getIdToken());
        try {
            intent.putExtra(RequestFields.WORKOUT, new ObjectMapper().writeValueAsString(Globals.activeWorkout.asMap()));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        startService(intent);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // stop any timer/stopwatch services that may be running.
        stopService(new Intent(this, TimerService.class));
        stopService(new Intent(this, StopwatchService.class));
        // update tokens just in case they changed in apps life cycle
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Variables.REFRESH_TOKEN_KEY, apiGateway.getTokens().getRefreshToken());
        editor.putString(Variables.ID_TOKEN_KEY, apiGateway.getTokens().getIdToken());
        editor.apply();

        super.onDestroy();
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
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        /*
            Called whenever the user clicks on a notification.
         */
        super.onNewIntent(intent); // this has to be first to not crash app when clicking notification when app is in background
        String action = intent.getAction();
        if (action != null && (action.equals(Variables.INTENT_TIMER_NOTIFICATION_CLICK)
                || action.equals(Variables.INTENT_STOPWATCH_NOTIFICATION_CLICK))) {
            // close any popup that might be showing
            Fragment currentFragment = getVisibleFragment();
            if (currentFragment instanceof FragmentWithDialog) {
                ((FragmentWithDialog) currentFragment).hideAllDialogs();
            }

            if (currentFragment instanceof NewWorkoutFragment) {
//                if (((NewWorkoutFragment) currentFragment).isModified()) {
//                    // workout is being made, so give user option to prevent navigation change
//                    showUnsavedChangesNewWorkoutPopup(true);
//                    return;
//                } else {
//                    // can't ever go back to the new workout fragment since it's only accessible within the my workout fragment
//                    fragmentStack.remove(Variables.NEW_WORKOUT_TITLE);
//                }
            }
//            else if (currentFragment instanceof EditWorkoutFragment) {
//                if (((EditWorkoutFragment) currentFragment)) {
//                    // workout is being edited, so give user option to prevent navigation change
//                    showUnsavedChangesEditWorkoutPopup(true);
//                    return;
//                } else {
//                    // can't ever go back to the edit workout fragment since it's only accessible within the my workout fragment
//                    fragmentStack.remove(Variables.EDIT_WORKOUT_TITLE);
//                }
//            }

            goToCurrentWorkout();
            enableBackButton(false);
            nav.setCheckedItem(R.id.nav_current_workout);
        }
    }

    public void initViews() {
        /*
            Called when the default exercises are present in local memory. Sets up the navigation pane.
         */
        nav.setNavigationItemSelectedListener(this);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.nav_draw_open, R.string.nav_draw_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        if (state == null) {
            // default landing fragment is current workout one
            currentWorkoutFragment = new ActiveWorkoutFragment();
            fragmentManager.beginTransaction().replace(R.id.fragment_container,
                    currentWorkoutFragment, Variables.CURRENT_WORKOUT_TITLE).commit();
            fragmentStack.add(Variables.CURRENT_WORKOUT_TITLE);
            nav.setCheckedItem(R.id.nav_current_workout);
        }
    }

    public void enableBackButton(boolean enable) {
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
        Fragment currentFrag = getVisibleFragment();
        boolean modified = fragModified(currentFrag);
        switch (menuItem.getItemId()) {
            case R.id.nav_current_workout:
                if (currentFrag instanceof NewWorkoutFragment) {
                    if (modified) {
                        showUnsavedChangesNewWorkoutPopup(false);
                    } else {
                        goToCurrentWorkout();
                    }
                } else if (!(currentFrag instanceof ActiveWorkoutFragment)) {
                    // prevent from selecting currently selected fragment
                    goToCurrentWorkout();
                }
                break;

            case R.id.nav_my_workouts:
                if (currentFrag instanceof NewWorkoutFragment) {
                    if (modified) {
                        showUnsavedChangesNewWorkoutPopup(false);
                    } else {
                        goToMyWorkouts();
                    }
                } else if (!(currentFrag instanceof MyWorkoutsFragment)) {
                    // prevent from selecting currently selected fragment
                    goToMyWorkouts();
                }
                break;

            case R.id.nav_my_exercises:
                if (currentFrag instanceof NewWorkoutFragment) {
                    if (modified) {
                        showUnsavedChangesNewWorkoutPopup(false);
                    } else {
                        goToMyExercises();
                    }
                } else if (!(currentFrag instanceof MyExercisesFragment)) {
                    // prevent from selecting currently selected fragment
                    goToMyExercises();
                }
                break;

            case R.id.nav_user_settings:
                if (currentFrag instanceof NewWorkoutFragment) {
                    if (modified) {
                        showUnsavedChangesNewWorkoutPopup(false);
                    } else {
                        goToUserSettings();
                    }
                } else if (!(currentFrag instanceof UserSettingsFragment)) {
                    // prevent from selecting currently selected fragment
                    goToUserSettings();
                }
                break;

            case R.id.nav_about:
                if (currentFrag instanceof NewWorkoutFragment) {
                    if (modified) {
                        showUnsavedChangesNewWorkoutPopup(false);
                    } else {
                        goToAbout();
                    }
                } else if (!(currentFrag instanceof AboutFragment)) {
                    // prevent from selecting currently selected fragment
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
        boolean modified = fragModified(visibleFragment);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            // if the user clicked the navigation panel, allow back press to close it.
            drawer.closeDrawer(GravityCompat.START);
            return;
        } else if (visibleFragment instanceof NewWorkoutFragment) {
            if (modified && showPopupFlag) {
                // workout is being made, so give user option to prevent fragment from closing from back press
                showUnsavedChangesNewWorkoutPopup(false);
                return;
            }
        } else if (visibleFragment instanceof EditWorkoutFragment) {
            if (modified && showPopupFlag) {
                // workout is being edited, so give user option to prevent fragment from closing from back press
                showUnsavedChangesEditWorkoutPopup(false);
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
        if (fragmentStack.size() > 0) {
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
                    goToUserSettings();
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
            }
            enableBackButton(false); // in case the user was in a page that had a back button
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

    public void showUnsavedChangesNewWorkoutPopup(final boolean timerNotificationClicked) {
        /*
            Is called whenever the user has unfinished work in the create workout fragment.
         */
        alertDialog = new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                .setTitle("Unsaved Changes")
                .setMessage(R.string.unsaved_workout_msg)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (timerNotificationClicked) {
                            fragmentStack.remove(Variables.NEW_WORKOUT_TITLE);
                            alertDialog.dismiss();
                            enableBackButton(false);
                            nav.setCheckedItem(R.id.nav_current_workout);
                            goToCurrentWorkout();
                        } else {
                            showPopupFlag = false;
                            onBackPressed();
                        }
                    }
                })
                .setNegativeButton("No", null)
                .create();
        alertDialog.show();
        // make the message font a little bigger than the default one provided by the alertdialog
        TextView messageTV = alertDialog.getWindow().findViewById(android.R.id.message);
        messageTV.setTextSize(18);
    }

    public void showUnsavedChangesEditWorkoutPopup(final boolean timerNotificationClicked) {
        /*
            Is called whenever the user has unfinished work in the edit workout fragment.
         */
        alertDialog = new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                .setTitle("Unsaved Changes")
                .setMessage(R.string.popup_message_edit_workout)
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (timerNotificationClicked) {
                        fragmentStack.remove(Variables.EDIT_WORKOUT_TITLE);
                        alertDialog.dismiss();
                        enableBackButton(false);
                        nav.setCheckedItem(R.id.nav_current_workout);
                        goToCurrentWorkout();
                    } else {
                        showPopupFlag = false;
                        onBackPressed();
                    }
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

    public boolean fragModified(Fragment aFragment) {
        /*
            Checks if passed in fragment has been modified
         */
        boolean retVal = false;
        if (aFragment == null) {
            retVal = false;
        } else if (aFragment instanceof NewWorkoutFragment) {
            retVal = true;
        } else if (aFragment instanceof EditWorkoutFragment) {
            retVal = true;
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
        // keeping the fragment as a variable to ensure the state is maintained (e.g. user can click to filter default, want to keep that)
//        if (myExercisesFragment == null) {
//
//        }
        myExercisesFragment = new MyExercisesFragment();
        fragmentManager.beginTransaction().replace(R.id.fragment_container,
                myExercisesFragment, Variables.MY_EXERCISES_TITLE)
                .commit();
    }

    public void goToExerciseDetails(String exerciseId) {
        if (fragmentStack.contains(Variables.EXERCISE_DETAILS_TITLE)) {
            fragmentStack.remove(Variables.EXERCISE_DETAILS_TITLE);
            fragmentStack.add(0, Variables.EXERCISE_DETAILS_TITLE);
        } else {
            fragmentStack.add(0, Variables.EXERCISE_DETAILS_TITLE);
        }
        // keeping the fragment as a variable to ensure the state is maintained (e.g. user can click to filter default, want to keep that)
        if (myExercisesFragment == null) {
            myExercisesFragment = new MyExercisesFragment();
        }
        Bundle arguments = new Bundle();
        arguments.putString(Variables.EXERCISE_ID, exerciseId);
        Fragment fragment = new ExerciseDetailsFragment();
        fragment.setArguments(arguments);

        fragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
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
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
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

    public void goToUserSettings() {
        if (fragmentStack.contains(Variables.SETTINGS_TITLE)) {
            fragmentStack.remove(Variables.SETTINGS_TITLE);
            fragmentStack.add(0, Variables.SETTINGS_TITLE);
        } else {
            fragmentStack.add(0, Variables.SETTINGS_TITLE);
        }
        fragmentManager.beginTransaction().replace(R.id.fragment_container,
                new UserSettingsFragment(), Variables.SETTINGS_TITLE)
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
