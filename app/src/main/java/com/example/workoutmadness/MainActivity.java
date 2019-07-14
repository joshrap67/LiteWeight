package com.example.workoutmadness;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;
    private TextView toolbarTitleTV;
    private NavigationView nav;
    private static final String WORKOUT_DIRECTORY_NAME ="Workouts", CURRENT_WORKOUT_LOG="currentWorkout.log",
            USER_SETTINGS_DIRECTORY_NAME="UserSettings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*
        <div>Icons made by <a href="https://www.flaticon.com/authors/monkik" title="monkik">monkik</a> from <a href="https://www.flaticon.com/"
        title="Flaticon">www.flaticon.com</a> is licensed by <a href="http://creativecommons.org/licenses/by/3.0/"
        title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>
         */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbarTitleTV = findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); // removes the app title from the toolbar
        drawer = findViewById(R.id.drawer);
        nav = findViewById(R.id.nav_view);
        nav.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.nav_draw_open, R.string.nav_draw_close);
        drawer.addDrawerListener(toggle);
        boolean exists = checkIfDirectoryExists(WORKOUT_DIRECTORY_NAME);
        if(!exists){
            createDirectory(WORKOUT_DIRECTORY_NAME);
        }
        toggle.syncState();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new CurrentWorkoutFragment()).commit();
            nav.setCheckedItem(R.id.nav_current_workout);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        //TODO ask if wanting to save progress instead of just losing it all?
        Fragment currentFrag = getVisibleFragment();
        boolean modified = fragModified(currentFrag);
        switch (menuItem.getItemId()) {
            case R.id.nav_current_workout:
                if(currentFrag instanceof NewWorkoutFragment){
                    if(modified){
                        showPopup("current_workout");
                    }
                    else{
                        goToCurrentWorkout();
                    }
                }
                else if(currentFrag instanceof UserSettingsFragment){
                    // check if they are modifiing the custom exercises or videos
                    if(modified){
                        // todo popup
                    }
                    else{
                        goToCurrentWorkout();
                    }
                }
                else if(!(currentFrag instanceof CurrentWorkoutFragment)){
                    goToCurrentWorkout();
                }
                break;

            case R.id.nav_my_workouts:
                if(currentFrag instanceof CurrentWorkoutFragment){
                    if(modified){
                        ((CurrentWorkoutFragment) currentFrag).recordToCurrentWorkoutLog();
                        ((CurrentWorkoutFragment) currentFrag).recordToWorkoutFile();
                    }
                    goToMyWorkouts();
                }
                else if(currentFrag instanceof NewWorkoutFragment){
                    if(modified){
                        showPopup("my_workouts");
                    }
                    else{
                        goToMyWorkouts();
                    }
                }
                else if(currentFrag instanceof UserSettingsFragment){
                    // check if they are modifying the custom exercises or videos
                    if(modified){
                        showPopup("my_workouts");
                    }
                    else{
                        goToMyWorkouts();
                    }
                }
                else if(!(currentFrag instanceof MyWorkoutFragment)){
                    goToMyWorkouts();
                }
                break;

            case R.id.nav_new_workout:
                if(currentFrag instanceof CurrentWorkoutFragment){
                    if(modified){
                        ((CurrentWorkoutFragment) currentFrag).recordToCurrentWorkoutLog();
                        ((CurrentWorkoutFragment) currentFrag).recordToWorkoutFile();
                    }

                    goToNewWorkout();
                }
                else if(currentFrag instanceof UserSettingsFragment){
                    // check if they are modifying the custom exercises or videos
                    if(modified){
                        showPopup("new_workout");
                    }
                    else{
                        goToNewWorkout();
                    }
                }
                else if(!(currentFrag instanceof NewWorkoutFragment)) {
                    goToNewWorkout();
                }
                break;

            case R.id.nav_user_settings:
                if(currentFrag instanceof CurrentWorkoutFragment){
                    if(modified){
                        ((CurrentWorkoutFragment) currentFrag).recordToCurrentWorkoutLog();
                        ((CurrentWorkoutFragment) currentFrag).recordToWorkoutFile();
                    }
                    goToUserSettings();
                }
                else if(currentFrag instanceof NewWorkoutFragment){
                    if(modified){
                        showPopup("user_settings");
                    }
                    else{
                        goToUserSettings();
                    }
                }
                else if(!(currentFrag instanceof UserSettingsFragment)) {
                    goToUserSettings();
                }
                break;

            case R.id.nav_about:
                if(currentFrag instanceof CurrentWorkoutFragment){
                    if(modified){
                        ((CurrentWorkoutFragment) currentFrag).recordToCurrentWorkoutLog();
                        ((CurrentWorkoutFragment) currentFrag).recordToWorkoutFile();
                    }
                    goToAbout();
                }
                else if(currentFrag instanceof NewWorkoutFragment){
                    if(modified){
                        showPopup("about");
                    }
                    else{
                        goToAbout();
                    }
                }
                else if(currentFrag instanceof UserSettingsFragment){
                    // check if they are modifying the custom exercises or videos
                    if(modified){
                        showPopup("about");
                    }
                    else{
                        goToAbout();
                    }
                }
                else if(!(currentFrag instanceof AboutFragment)){
                    goToAbout();
                }
                break;

        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onPause(){
        Fragment visibleFragment = getVisibleFragment();
        boolean modified=fragModified(visibleFragment);
        if(visibleFragment instanceof CurrentWorkoutFragment){
            if(modified){
                ((CurrentWorkoutFragment) visibleFragment).recordToCurrentWorkoutLog();
                ((CurrentWorkoutFragment) visibleFragment).recordToWorkoutFile();
            }
        }
        else if(visibleFragment instanceof NewWorkoutFragment){
            if(modified){
                // TODO idk
            }
        }
        super.onPause();
    }

    @Override
    public void onDestroy(){
        Fragment visibleFragment = getVisibleFragment();
        boolean modified=fragModified(visibleFragment);
        if(visibleFragment instanceof CurrentWorkoutFragment){
            if(modified){
                ((CurrentWorkoutFragment) visibleFragment).recordToCurrentWorkoutLog();
                ((CurrentWorkoutFragment) visibleFragment).recordToWorkoutFile();
            }
        }
        else if(visibleFragment instanceof NewWorkoutFragment){
            if(modified){
                // TODO idk
            }
        }
        super.onDestroy();
    }

    @Override
    public void onResume(){
        Fragment visibleFragment = getVisibleFragment();
        if(visibleFragment instanceof CurrentWorkoutFragment){
            /*
                Kind of hacky, but otherwise fragment will resume where it left off and introduce lots
                of logical errors. So just delete old fragment and launch new
            */
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new CurrentWorkoutFragment(), "CURRENT_WORKOUT").commit();
        }
        else if(visibleFragment instanceof NewWorkoutFragment){
            ((NewWorkoutFragment) visibleFragment).setModified(false);
        }
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        Fragment visibleFragment = getVisibleFragment();
        boolean modified=fragModified(visibleFragment);
        boolean quit = true;
        // TODO check if new workout is being created, if so ask if user is sure they want to quit
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            // if the user clicked the navigation panel, allow back press to close it.
            drawer.closeDrawer(GravityCompat.START);
            quit = false;
        }
        else if(visibleFragment instanceof CurrentWorkoutFragment){
            if(modified){
                ((CurrentWorkoutFragment) visibleFragment).recordToCurrentWorkoutLog();
            }
        }
        else if(visibleFragment instanceof NewWorkoutFragment){
            if(modified){
                quit = false;
            }
        }

        if(quit){
            super.onBackPressed();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        /*
            Found on SO
         */
        View v = getCurrentFocus();

        if (v != null &&
                (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) &&
                v instanceof EditText &&
                !v.getClass().getName().startsWith("android.webkit.")) {
            int scrcoords[] = new int[2];
            v.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + v.getLeft() - scrcoords[0];
            float y = ev.getRawY() + v.getTop() - scrcoords[1];

            if (x < v.getLeft() || x > v.getRight() || y < v.getTop() || y > v.getBottom())
                hideKeyboard(this);
        }
        return super.dispatchTouchEvent(ev);
    }

    public static void hideKeyboard(Activity activity) {
        /*
            Found on SO
         */
        if (activity != null && activity.getWindow() != null && activity.getWindow().getDecorView() != null) {
            InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
        }
    }

    public boolean checkIfDirectoryExists(String directoryName){
        File directoryHandle = getExternalFilesDir(directoryName);
        File[] contents = directoryHandle.listFiles();
        if(contents.length>0){
            return true;
        }
        else{
            return false;
        }
    }

    public void createDirectory(String directoryName){
        File directoryHandle = getExternalFilesDir(directoryName);
        directoryHandle.mkdirs();
        File fhandle = new File(getExternalFilesDir(directoryName), CURRENT_WORKOUT_LOG);
        try {
            fhandle.createNewFile();
        } catch (Exception e) {
            Log.d("Creating file", "Error when trying to create the "+CURRENT_WORKOUT_LOG+" file!");
        }
        copyFile("Josh's Workout.txt");
        copyFile(CURRENT_WORKOUT_LOG);
    }

    public String getWorkoutDirectoryName(){
        return WORKOUT_DIRECTORY_NAME;
    }

    public String getWorkoutLogName(){
        return CURRENT_WORKOUT_LOG;
    }

    public void copyFile(String fileName){
        /*
            Would be called first time app is installed. Copies file from asset folder to internal directory of app
         */
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try{
            File fhandle = new File(getExternalFilesDir(WORKOUT_DIRECTORY_NAME), fileName);
            writer = new BufferedWriter(new FileWriter(fhandle,false));
            reader = new BufferedReader(new InputStreamReader(getAssets().open(fileName)));
            String line;
            while((line=reader.readLine())!=null){
                writer.write(line+"\n");
            }
            writer.close();
            reader.close();
        }
        catch (Exception e){
            Log.d("ERROR","Error when trying to copy "+fileName+"\n"+e);
        }

    }

    public void updateToolbarTitle(String aTitle) {
        /*
            Called by other fragments to change the string that the toolbar displays.
         */
        toolbarTitleTV.setText(aTitle);

    }
    public boolean fragModified(Fragment aFragment){
        /*
            Checks if passed in fragment has been modified
         */
        if(aFragment==null){
            return false;
        }
        else if(aFragment instanceof CurrentWorkoutFragment){
            return ((CurrentWorkoutFragment) aFragment).isModified();
        }
        else if(aFragment instanceof NewWorkoutFragment){
            return ((NewWorkoutFragment) aFragment).isModified();
        }
        else if(aFragment instanceof UserSettingsFragment){
            return ((UserSettingsFragment) aFragment).isModified();
        }
        return false;
    }

    private Fragment getVisibleFragment() {
        /*
            Found on SO
         */
        FragmentManager fragmentManager = MainActivity.this.getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        for (Fragment fragment : fragments) {
            if (fragment != null && fragment.isVisible())
                return fragment;
        }
        return null;
    }

    public void goToCurrentWorkout(){
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new CurrentWorkoutFragment(), "CURRENT_WORKOUT").commit();
    }

    public void goToNewWorkout(){
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new NewWorkoutFragment(), "NEW_WORKOUT").commit();
    }

    public void goToMyWorkouts(){
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new MyWorkoutFragment(), "MY_WORKOUTS").commit();
    }

    public void goToUserSettings(){
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new UserSettingsFragment(), "USER_SETTINGS").commit();
    }

    public void goToAbout(){
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new AboutFragment(), "ABOUT").commit();
    }


    public void showPopup(final String layout_name){
        /*
            Is called whenever the user has unfinished work in the create workout fragment.
         */
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        final View popupView = getLayoutInflater().inflate(R.layout.quit_popup, null);
        Button confirmButton = popupView.findViewById(R.id.popupYes);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (layout_name){
                    case "current_workout":
                        goToCurrentWorkout();
                        break;
                    case "my_workouts":
                        goToMyWorkouts();
                        break;
                    case "user_settings":
                        goToUserSettings();
                        break;
                    case "about":
                        goToAbout();
                        break;
                }
                alertDialog.dismiss();
            }
        });
        Button quitButton = popupView.findViewById(R.id.popupNo);

        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nav.setCheckedItem(R.id.nav_new_workout); // since fragment didn't change, currently selected item is still new workout
                alertDialog.dismiss();
            }
        });
        alertDialog.setView(popupView);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }
}
