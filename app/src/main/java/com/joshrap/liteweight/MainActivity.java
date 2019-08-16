package com.joshrap.liteweight;

import android.app.Activity;
import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.joshrap.liteweight.Database.Entities.*;
import com.joshrap.liteweight.Fragments.*;
import com.joshrap.liteweight.Database.ViewModels.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;
    private TextView toolbarTitleTV;
    private NavigationView nav;
    private ExerciseViewModel exerciseModel;
    private ProgressBar progressBar;
    private Bundle state;
    private Toolbar toolbar;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*
        <div>Icons made by <a href="https://www.flaticon.com/authors/monkik" title="monkik">monkik</a> from <a href="https://www.flaticon.com/"
        title="Flaticon">www.flaticon.com</a> is licensed by <a href="http://creativecommons.org/licenses/by/3.0/"
        title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>
         */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        toolbarTitleTV = findViewById(R.id.toolbar_title);
        progressBar = findViewById(R.id.progress_bar);
        drawer = findViewById(R.id.drawer);
        nav = findViewById(R.id.nav_view);
        state = savedInstanceState;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); // removes the app title from the toolbar
        // get the view models
        exerciseModel = ViewModelProviders.of(this).get(ExerciseViewModel.class);
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Variables.SHARED_PREF_NAME, 0);
        editor = pref.edit();
        if(pref.getBoolean(Variables.DB_EMPTY_KEY,true)){
            Log.d("TAG","Exercise table empty!");
            setProgressBar(false);
            UpdateExercisesAsync task = new UpdateExercisesAsync();
            task.execute();
        }
        else{
            Log.d("TAG","Exercise table not empty!");
            initViews();
        }
    }

    private class UpdateExercisesAsync extends AsyncTask<Void, Void, Void> {
        /*
            Called when the exercise table is empty (such as when app first launches)
         */
        @Override
        protected void onPreExecute(){
            setProgressBar(true);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // update the exercises in the database using the default exercise file in the app's asset folder
            BufferedReader reader;
            try{
                reader = new BufferedReader(new InputStreamReader(getAssets().open(Variables.DEFAULT_EXERCISES_FILE)));
                String line;
                while((line=reader.readLine())!=null){
                    String name = line.split(Variables.SPLIT_DELIM)[Variables.NAME_INDEX];
                    String video = line.split(Variables.SPLIT_DELIM)[Variables.VIDEO_INDEX];
                    String focuses = line.split(Variables.SPLIT_DELIM)[Variables.FOCUS_INDEX_FILE];
                    ExerciseEntity entity = new ExerciseEntity(name,focuses,video,true,0,0,0,0);
                    exerciseModel.insert(entity);
                }
                reader.close();
            }
            catch (Exception e){
                Log.d("ERROR","Error when trying to read default exercise file!\n"+e);
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

    public void initViews(){
        /*
            Called when the exercise table in the database is not empty. Sets up the navigation pane.
         */
        nav.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.nav_draw_open, R.string.nav_draw_close);
        drawer.addDrawerListener(toggle);
        // TODO handle configuration changes!
        toggle.syncState();
        if (state == null) {
            // default landing fragment is current workout one
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new CurrentWorkoutFragment()).commit();
            nav.setCheckedItem(R.id.nav_current_workout);
        }
    }

    public void setProgressBar(boolean status){
        /*
            Used in tandem with async tasks. When in the background, will set the progress bar to true to show user loading
            animation.
         */
        if(status){
            progressBar.setVisibility(View.VISIBLE);
        }
        else{
            progressBar.setVisibility(View.GONE);
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
                if(currentFrag instanceof NewWorkoutFragment){
                    if(modified){
                        showPopup("current_workout");
                    }
                    else{
                        goToCurrentWorkout();
                    }
                }
                else if(!(currentFrag instanceof CurrentWorkoutFragment)){
                    // prevent from selecting currently selected fragment
                    goToCurrentWorkout();
                }
                break;

            case R.id.nav_my_workouts:
                if(currentFrag instanceof NewWorkoutFragment){
                    if(modified){
                        showPopup("my_workouts");
                    }
                    else{
                        goToMyWorkouts();
                    }
                }
                else if(!(currentFrag instanceof MyWorkoutFragment)){
                    // prevent from selecting currently selected fragment
                    goToMyWorkouts();
                }
                break;

            case R.id.nav_new_workout:
                if(!(currentFrag instanceof NewWorkoutFragment)) {
                    // prevent from selecting currently selected fragment
                    goToNewWorkout();
                }
                break;

            case R.id.nav_user_settings:
                if(currentFrag instanceof NewWorkoutFragment){
                    if(modified){
                        showPopup("user_settings");
                    }
                    else{
                        goToUserSettings();
                    }
                }
                else if(!(currentFrag instanceof UserSettingsFragment)) {
                    // prevent from selecting currently selected fragment
                    goToUserSettings();
                }
                break;

            case R.id.nav_about:
                if(currentFrag instanceof NewWorkoutFragment){
                    if(modified){
                        showPopup("about");
                    }
                    else{
                        goToAbout();
                    }
                }
                else if(!(currentFrag instanceof AboutFragment)){
                    // prevent from selecting currently selected fragment
                    goToAbout();
                }
                break;

        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onResume(){
        /*
            Kind of hacky, but otherwise fragment will resume where it left off and introduce lots
            of logical errors. So just deleteWorkoutEntity old fragment and launch new
        */
        Fragment visibleFragment = getVisibleFragment();
        if(visibleFragment instanceof CurrentWorkoutFragment){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new CurrentWorkoutFragment(), Variables.CURRENT_WORKOUT_TITLE).commit();
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
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            // if the user clicked the navigation panel, allow back press to close it.
            drawer.closeDrawer(GravityCompat.START);
            quit = false;
        }
        else if(visibleFragment instanceof NewWorkoutFragment){
            if(modified){
                // workout is being made, so give user option to prevent app from closing from back press
                quit = false;
            }
        }
        if(quit){
            super.onBackPressed();
        }
        else{
            showPopup("quit");
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        /*
            Found on Stack Overflow. Used to hide the keyboard.
         */
        View view = getCurrentFocus();
        if(view != null &&
                (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) &&
                view instanceof EditText &&
                !view.getClass().getName().startsWith("android.webkit.")) {
            int scrcoords[] = new int[2];
            view.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + view.getLeft() - scrcoords[0];
            float y = ev.getRawY() + view.getTop() - scrcoords[1];

            if(x < view.getLeft() || x > view.getRight() || y < view.getTop() || y > view.getBottom())
                hideKeyboard(this);
        }
        return super.dispatchTouchEvent(ev);
    }

    public static void hideKeyboard(Activity activity) {
        /*
            Found on Stack Overflow. Hides keyboard when clicking outside focus
         */
        if (activity != null && activity.getWindow() != null && activity.getWindow().getDecorView() != null) {
            InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
        }
    }

    public void showPopup(final String layout_name){
        /*
            Is called whenever the user has unfinished work in the create workout fragment.
         */
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        final View popupView = getLayoutInflater().inflate(R.layout.popup_quit, null);
        Button confirmButton = popupView.findViewById(R.id.popup_yes);
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
                    case "quit":
                        MainActivity.super.onBackPressed();
                        break;
                }
                alertDialog.dismiss();
            }
        });
        Button quitButton = popupView.findViewById(R.id.popup_no);

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
        if(aFragment == null){
            return false;
        }
        else if(aFragment instanceof NewWorkoutFragment){
            return ((NewWorkoutFragment) aFragment).isModified();
        }
        return false;
    }

    private Fragment getVisibleFragment() {
        /*
            Returns the current fragment in focus
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
                new CurrentWorkoutFragment(), Variables.CURRENT_WORKOUT_TITLE).commit();
    }

    public void goToNewWorkout(){
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new NewWorkoutFragment(), Variables.NEW_WORKOUT_TITLE).commit();
    }

    public void goToMyWorkouts(){
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new MyWorkoutFragment(), Variables.MY_WORKOUT_TITLE).commit();
    }

    public void goToUserSettings(){
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new UserSettingsFragment(), Variables.SETTINGS_TITLE).commit();
    }

    public void goToAbout(){
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new AboutFragment(), Variables.ABOUT_TITLE).commit();
    }
}
