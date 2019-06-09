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
import android.widget.Toast;

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
    private String DIRECTORY_NAME="bin", CURRENT_WORKOUT_LOG="currentWorkout.log";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //TODO implement timer??? For in between reps
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
        boolean exists = checkIfDirectoryExists();
        if(!exists){
            createDirectory();
        }
        toggle.syncState();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new WorkoutFragment()).commit();
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
                else{
                    goToCurrentWorkout();
                }
                break;

            case R.id.nav_my_workouts:
                if(currentFrag instanceof WorkoutFragment ){
                    if(modified){
                        Toast.makeText(this, "Workout was modified", Toast.LENGTH_SHORT).show();
                    }
                    ((WorkoutFragment) currentFrag).recordToCurrentWorkoutLog();
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
                break;

            case R.id.nav_new_workout:
                if(currentFrag instanceof WorkoutFragment ){
                    if(modified){
                        // TODO save to workout file
                        Toast.makeText(this, "Workout was modified", Toast.LENGTH_SHORT).show();
                    }
                    ((WorkoutFragment) currentFrag).recordToCurrentWorkoutLog();
                    goToNewWorkout();
                }
                else if(!(currentFrag instanceof NewWorkoutFragment)) {
                    goToNewWorkout();
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
        if(visibleFragment instanceof WorkoutFragment){
            if(modified){
                Toast.makeText(this, "Workout was modified", Toast.LENGTH_SHORT).show();
                ((WorkoutFragment) visibleFragment).recordToCurrentWorkoutLog();
            }
        }
        else if(visibleFragment instanceof NewWorkoutFragment){
            if(modified){
                Toast.makeText(this, "Creating workout was modified", Toast.LENGTH_SHORT).show();
            }
        }
        super.onPause();
    }

    @Override
    public void onResume(){
        Fragment visibleFragment = getVisibleFragment();
        if(visibleFragment instanceof WorkoutFragment){
            ((WorkoutFragment) visibleFragment).setModified(false);
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
        else if(visibleFragment instanceof WorkoutFragment){
            if(modified){
                Toast.makeText(this, "Workout was modified", Toast.LENGTH_SHORT).show();
                ((WorkoutFragment) visibleFragment).recordToCurrentWorkoutLog();
            }
        }
        else if(visibleFragment instanceof NewWorkoutFragment){
            if(modified){
                Toast.makeText(this, "Creating workout was modified", Toast.LENGTH_SHORT).show();
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

    public boolean checkIfDirectoryExists(){
        File directoryHandle = getExternalFilesDir(DIRECTORY_NAME);
        File[] contents = directoryHandle.listFiles();
        if(contents.length>0){
            return true;
        }
        else{
            return false;
        }
    }

    public void createDirectory(){
        File directoryHandle = getExternalFilesDir(DIRECTORY_NAME);
        directoryHandle.mkdirs();
        File fhandle = new File(getExternalFilesDir(DIRECTORY_NAME), CURRENT_WORKOUT_LOG);
        try {
            fhandle.createNewFile();
        } catch (Exception e) {
            Log.d("Creating file", "Error when trying to create the "+CURRENT_WORKOUT_LOG+" file!");
        }
        copyFile("Josh's Workout.txt");
        copyFile(CURRENT_WORKOUT_LOG);
    }

    public String getDirectoryName(){
        return DIRECTORY_NAME;
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
            File fhandle = new File(getExternalFilesDir(DIRECTORY_NAME), fileName);
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
        else if(aFragment instanceof WorkoutFragment){
            if(((WorkoutFragment) aFragment).isModified()){
                return true;
            }
        }
        else if(aFragment instanceof NewWorkoutFragment){
            if(((NewWorkoutFragment) aFragment).isModified()){
                return true;
            }
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
                new WorkoutFragment(), "CURRENT_WORKOUT").commit();
    }

    public void goToNewWorkout(){
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new NewWorkoutFragment(), "NEW_WORKOUT").commit();
    }

    public void goToMyWorkouts(){
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new MyWorkoutFragment(), "NY_WORKOUTS").commit();
    }


    public void showPopup(final String layout_name){
        /*
            Is called whenever the user has unfinished work in the create workout fragment.
         */
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        // TODO change this to the right view once i create the layout
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
