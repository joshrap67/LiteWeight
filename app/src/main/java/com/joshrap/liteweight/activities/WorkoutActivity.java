package com.joshrap.liteweight.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.joshrap.liteweight.R;
import com.joshrap.liteweight.database.entities.*;
import com.joshrap.liteweight.fragments.*;
import com.joshrap.liteweight.database.viewModels.*;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;
import com.joshrap.liteweight.services.StopwatchService;
import com.joshrap.liteweight.services.TimerService;
import com.joshrap.liteweight.widgets.Stopwatch;
import com.joshrap.liteweight.widgets.Timer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class WorkoutActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;
    private AlertDialog alertDialog;
    private ActionBarDrawerToggle toggle;
    private boolean drawerListenerIsRegistered = false;
    private TextView toolbarTitleTV;
    private NavigationView nav;
    private ExerciseViewModel exerciseModel;
    private ProgressBar progressBar;
    private Bundle state;
    private Toolbar toolbar;
    private FragmentManager fragmentManager;
    private SharedPreferences.Editor editor;
    private boolean showPopupFlag;
    private ArrayList<String> fragmentStack = new ArrayList<>();
    private Timer timer;
    private Stopwatch stopwatch;
    // save these as variables since the user can click around and it is ideal to preserve the view that they altered
    private CurrentWorkoutFragment currentWorkoutFragment;
    private MyExercisesFragment myExercisesFragment;
    private UpdateExercisesAsync getDefaultExercisesTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createNotificationChannel();
        setContentView(R.layout.activity_main);
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
        // get the view models
        exerciseModel = ViewModelProviders.of(this).get(ExerciseViewModel.class);
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Variables.SHARED_PREF_SETTINGS, 0);
        editor = pref.edit();
        if (pref.getBoolean(Variables.DB_EMPTY_KEY, true)) {
            setProgressBar(false);
            getDefaultExercisesTask = new UpdateExercisesAsync();
            getDefaultExercisesTask.execute();
        } else {
            initViews();
        }
    }

    @Override
    protected void onDestroy() {
        if (getDefaultExercisesTask != null) {
            getDefaultExercisesTask.cancel(true);
        }
        // stop any services that may be running.
        stopService(new Intent(this, TimerService.class));
        stopService(new Intent(this, StopwatchService.class));
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

    private class UpdateExercisesAsync extends AsyncTask<Void, Void, Void> {
        /*
            Called when the exercise table is empty (such as when app first launches)
         */
        @Override
        protected void onPreExecute() {
            setProgressBar(true);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // update the exercises in the database using the default exercise file in the app's asset folder
            BufferedReader reader;
            try {
                reader = new BufferedReader(new InputStreamReader(getAssets().open(Variables.DEFAULT_EXERCISES_FILE)));
                String line;
                while ((line = reader.readLine()) != null) {
                    String name = line.split(Variables.SPLIT_DELIM)[Variables.NAME_INDEX];
                    String video = line.split(Variables.SPLIT_DELIM)[Variables.VIDEO_INDEX];
                    String focuses = line.split(Variables.SPLIT_DELIM)[Variables.FOCUS_INDEX_FILE];
                    ExerciseEntity entity = new ExerciseEntity(name, focuses, video, true, 0, 0, 0, 0);
                    exerciseModel.insert(entity);
                }
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            editor.putBoolean(Variables.DB_EMPTY_KEY, false);
            editor.apply();
            setProgressBar(false);
            initViews();
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
                if (((NewWorkoutFragment) currentFragment).isModified()) {
                    // workout is being made, so give user option to prevent navigation change
                    showUnsavedChangesNewWorkoutPopup(true);
                    return;
                } else {
                    // can't ever go back to the new workout fragment since it's only accessible within the my workout fragment
                    fragmentStack.remove(Variables.NEW_WORKOUT_TITLE);
                }
            } else if (currentFragment instanceof EditWorkoutFragment) {
                if (((EditWorkoutFragment) currentFragment).isEditing()) {
                    // workout is being edited, so give user option to prevent navigation change
                    showUnsavedChangesEditWorkoutPopup(true);
                    return;
                } else {
                    // can't ever go back to the edit workout fragment since it's only accessible within the my workout fragment
                    fragmentStack.remove(Variables.EDIT_WORKOUT_TITLE);
                }
            }

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
            currentWorkoutFragment = new CurrentWorkoutFragment();
            fragmentManager.beginTransaction().replace(R.id.fragment_container,
                    currentWorkoutFragment, Variables.CURRENT_WORKOUT_TITLE).commit();
            fragmentStack.add(Variables.CURRENT_WORKOUT_TITLE);
            nav.setCheckedItem(R.id.nav_current_workout);
        }
    }

    public void setProgressBar(boolean hide) {
        /*
            Used in tandem with async tasks. When background work is being done
            the progress bar is set to true to show user a loading animation.
         */
        progressBar.setVisibility((hide) ? View.VISIBLE : View.GONE);
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
                toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });
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
                } else if (!(currentFrag instanceof CurrentWorkoutFragment)) {
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
                } else if (!(currentFrag instanceof MyWorkoutFragment)) {
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
    public boolean dispatchTouchEvent(MotionEvent ev) {
        /*
            Found on Stack Overflow. Used to hide the keyboard.
         */
        View view = getCurrentFocus();
        if (view != null &&
                (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) &&
                view instanceof EditText &&
                !view.getClass().getName().startsWith("android.webkit.")) {
            int scrcoords[] = new int[2];
            view.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + view.getLeft() - scrcoords[0];
            float y = ev.getRawY() + view.getTop() - scrcoords[1];

            if (x < view.getLeft() || x > view.getRight() || y < view.getTop() || y > view.getBottom())
                hideKeyboard(this);
        }
        return super.dispatchTouchEvent(ev);
    }

    public static void hideKeyboard(Activity activity) {
        /*
            Found on Stack Overflow. Hides keyboard when clicking outside focus
         */
        if (activity != null && activity.getWindow() != null && activity.getWindow().getDecorView() != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
        }
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
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
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

    public void createWorkout() {
        /*
            Used when the user has no workouts, a button appears in the fragment that automatically takes them to the
            workout creator
         */
        goToNewWorkout();
    }

    public boolean fragModified(Fragment aFragment) {
        /*
            Checks if passed in fragment has been modified
         */
        boolean retVal = false;
        if (aFragment == null) {
            retVal = false;
        } else if (aFragment instanceof NewWorkoutFragment) {
            retVal = ((NewWorkoutFragment) aFragment).isModified();
        } else if (aFragment instanceof EditWorkoutFragment) {
            retVal = ((EditWorkoutFragment) aFragment).isEditing();
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
            currentWorkoutFragment = new CurrentWorkoutFragment();
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
        if (myExercisesFragment == null) {
            myExercisesFragment = new MyExercisesFragment();
        }
        fragmentManager.beginTransaction().replace(R.id.fragment_container,
                myExercisesFragment, Variables.MY_EXERCISES_TITLE)
                .commit();
    }

    public void goToNewWorkout() {
        if (fragmentStack.contains(Variables.NEW_WORKOUT_TITLE)) {
            fragmentStack.remove(Variables.NEW_WORKOUT_TITLE);
            fragmentStack.add(0, Variables.NEW_WORKOUT_TITLE);
        } else {
            fragmentStack.add(0, Variables.NEW_WORKOUT_TITLE);
        }
        fragmentManager.beginTransaction().replace(R.id.fragment_container,
                new NewWorkoutFragment(), Variables.NEW_WORKOUT_TITLE)
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
                new MyWorkoutFragment(), Variables.MY_WORKOUT_TITLE)
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
