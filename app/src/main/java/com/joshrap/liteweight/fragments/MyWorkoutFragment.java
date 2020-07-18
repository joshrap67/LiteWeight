package com.joshrap.liteweight.fragments;

import android.app.AlertDialog;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.joshrap.liteweight.*;
import com.joshrap.liteweight.activities.WorkoutActivity;
import com.joshrap.liteweight.database.entities.MetaEntity;
import com.joshrap.liteweight.database.viewModels.MetaViewModel;
import com.joshrap.liteweight.database.viewModels.WorkoutViewModel;
import com.joshrap.liteweight.helpers.InputHelper;
import com.joshrap.liteweight.helpers.StatisticsHelper;
import com.joshrap.liteweight.imports.Globals;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

public class MyWorkoutFragment extends Fragment implements FragmentWithDialog {
    private View view;
    private TextView selectedWorkoutTV, statisticsTV, defaultTV;
    private ListView workoutListView;
    private ViewGroup fragmentContainer;
    private AlertDialog alertDialog;
    private ArrayAdapter<String> arrayAdapter;
    private HashMap<String, MetaEntity> workoutNameToEntity = new HashMap<>();
    private WorkoutViewModel workoutModel;
    private MetaViewModel metaModel;
    private ArrayList<MetaEntity> metaEntities = new ArrayList<>();
    private ArrayList<String> workoutNames = new ArrayList<>();
    private SimpleDateFormat formatter = new SimpleDateFormat(Variables.DATE_PATTERN);
    private GetAllMetaTask getAllMetaTask;
    private DeleteWorkoutAsync deleteWorkoutTask;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.default_layout, container, false);
        fragmentContainer = container;
        defaultTV = view.findViewById(R.id.default_text_view);
        defaultTV.setVisibility(View.GONE);
        ((WorkoutActivity) getActivity()).updateToolbarTitle(Variables.MY_WORKOUT_TITLE);
        metaModel = ViewModelProviders.of(getActivity()).get(MetaViewModel.class);
        workoutModel = ViewModelProviders.of(getActivity()).get(WorkoutViewModel.class);
        getAllMetaTask = new GetAllMetaTask();
        getAllMetaTask.execute();
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getAllMetaTask != null) {
            getAllMetaTask.cancel(true);
        }
        if (deleteWorkoutTask != null) {
            deleteWorkoutTask.cancel(true);
        }
    }

    @Override
    public void hideAllDialogs() {
        /*
            Close any dialogs that might be showing. This is essential when clicking a notification that takes
            the user to a new page.
         */
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private class GetAllMetaTask extends AsyncTask<Void, Void, ArrayList<MetaEntity>> {

        @Override
        protected ArrayList<MetaEntity> doInBackground(Void... voids) {
            // get the current workout from the database
            return metaModel.getAllMetadata();
        }

        @Override
        protected void onPostExecute(ArrayList<MetaEntity> result) {
            if (!result.isEmpty()) {
                metaEntities = result;
                ((WorkoutActivity) getActivity()).setProgressBar(false);
                initViews();
            } else {
                defaultTV.setVisibility(View.VISIBLE);
                FloatingActionButton createWorkoutBtn = view.findViewById(R.id.create_workout_btn);
                createWorkoutBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((WorkoutActivity) getActivity()).goToNewWorkout();
                    }
                });
            }
        }
    }

    private void initViews() {
        /*
            Once at least one workout is found, change layouts and initialize all views
         */
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.fragment_my_workouts, fragmentContainer, false);
        ViewGroup rootView = (ViewGroup) getView();
        rootView.removeAllViews();
        rootView.addView(view);

        ImageButton workoutOptionsButton = view.findViewById(R.id.workout_options_btn);
        final PopupMenu dropDownMenu = new PopupMenu(getContext(), workoutOptionsButton);
        final Menu menu = dropDownMenu.getMenu();
        final int editIndex = 0;
        final int renameIndex = 1;
        final int resetIndex = 2;
        final int deleteIndex = 3;
        menu.add(0, editIndex, 0, "Edit Workout");
        menu.add(0, renameIndex, 0, "Rename Workout");
        menu.add(0, resetIndex, 0, "Reset Statistics");
        menu.add(0, deleteIndex, 0, "Delete Workout");

        dropDownMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case editIndex:
                        ((WorkoutActivity) getActivity()).goToEditWorkout();
                        return true;
                    case renameIndex:
                        promptRename();
                        return true;
                    case resetIndex:
                        promptReset();
                        return true;
                    case deleteIndex:
                        promptDelete();
                        return true;
                }
                return false;
            }
        });
        workoutOptionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dropDownMenu.show();
            }
        });

        workoutListView = view.findViewById(R.id.workout_list);
        selectedWorkoutTV = view.findViewById(R.id.selected_workout_text_view);
        statisticsTV = view.findViewById(R.id.stat_text_view);

        for (MetaEntity entity : metaEntities) {
            if (entity.getCurrentWorkout()) {
                Globals.currentWorkout = entity;
                selectedWorkoutTV.setText(Globals.currentWorkout.getWorkoutName());
                updateStatisticsTV();
            } else {
                workoutNames.add(entity.getWorkoutName());
            }
            workoutNameToEntity.put(entity.getWorkoutName(), entity);
        }
        sortWorkouts();
        // set up the create workout button
        FloatingActionButton createWorkoutBtn = view.findViewById(R.id.new_workout_btn);
        createWorkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((WorkoutActivity) getActivity()).goToNewWorkout();
            }
        });

        // set up the list view
        arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_activated_1, workoutNames);
        workoutListView.setAdapter(arrayAdapter);
        workoutListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        workoutListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectWorkout(workoutListView.getItemAtPosition(position).toString());
            }
        });
        workoutListView.setItemChecked(0, true); // programmatically select current workout in list
    }

    private void sortWorkouts() {
        /*
            Currently sorts by date last accessed
         */
        workoutNames.clear();
        Collections.sort(metaEntities, Collections.reverseOrder());
        for (MetaEntity entity : metaEntities) {
            if (!entity.getWorkoutName().equals(Globals.currentWorkout.getWorkoutName())) {
                workoutNames.add(entity.getWorkoutName());
            }
        }
        workoutNames.add(0, Globals.currentWorkout.getWorkoutName()); // selected always on top
    }

    private void selectWorkout(String workoutName) {
        /*
            Selects a workout from the list and handles any updates to the DB
         */
        // handle the currently selected workout
        Globals.currentWorkout.setCurrentWorkout(false);
        Date date = new Date();
        Globals.currentWorkout.setDateLast(formatter.format(date));
        metaModel.update(Globals.currentWorkout);

        // handle the newly selected workout
        Globals.currentWorkout = workoutNameToEntity.get(workoutName);
        Globals.currentWorkout.setCurrentWorkout(true);
        metaModel.update(Globals.currentWorkout);
        selectedWorkoutTV.setText(workoutName);
        // since we only sort by date last modified, just put selected at index 1 to push other elements down
        workoutNames.remove(workoutName);
        workoutNames.add(0, workoutName);
        arrayAdapter.notifyDataSetChanged();
        updateStatisticsTV();
        workoutListView.setItemChecked(0, true); // programmatically select current workout in list
    }

    private void updateStatisticsTV() {
        /*
            Displays statistics for the currently selected workout
         */
        int timesCompleted = Globals.currentWorkout.getTimesCompleted();
        double percentage = Globals.currentWorkout.getPercentageExercisesCompleted();
        String formattedPercentage;
        if (percentage > 0.0 && percentage < 100.0) {
            formattedPercentage = String.format("%.3f", percentage) + "%";
        } else if (percentage == 0.0) {
            formattedPercentage = "0%";
        } else {
            formattedPercentage = "100%";
        }
        String formattedType = null;
        if (Globals.currentWorkout.getWorkoutType().equals(Variables.WORKOUT_FIXED)) {
            formattedType = "Fixed";
        } else if (Globals.currentWorkout.getWorkoutType().equals(Variables.WORKOUT_FLEXIBLE)) {
            formattedType = "Flexible";
        }
        int days = Globals.currentWorkout.getMaxDayIndex() + 1;
        String msg = "Workout Type: " + formattedType + "\n" +
                "Times Completed: " + timesCompleted + "\n" +
                "Average Percentage of Exercises Completed: " + formattedPercentage + "\n" +
                "Total Number of Days in Workout: " + days + "\n" +
                "Most Worked Focus: " + Globals.currentWorkout.getMostFrequentFocus().replaceAll(",", ", ");
        statisticsTV.setText(msg);
    }

    private void promptReset() {
        /*
            Prompt the user if they actually want to reset the selected workout
         */
        String message = "Are you sure you wish to reset the statistics for \"" +
                Globals.currentWorkout.getWorkoutName() + "\"?\n\n" +
                "Doing so will reset the times completed and the percentage of exercises completed.";
        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle("Reset Statistics")
                .setMessage(message)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        StatisticsHelper.resetEntireWorkout(Globals.currentWorkout, metaModel);
                        updateStatisticsTV();
                    }
                })
                .setNegativeButton("No", null)
                .create();
        alertDialog.show();
        // make the message font a little bigger than the default one provided by the alertdialog
        TextView messageTV = alertDialog.getWindow().findViewById(android.R.id.message);
        messageTV.setTextSize(18);
    }

    private void promptRename() {
        /*
            Prompt the user if they want to rename the current workout
         */
        View popupView = getActivity().getLayoutInflater().inflate(R.layout.popup_rename_workout, null);
        final EditText renameInput = popupView.findViewById(R.id.rename_workout_input);
        renameInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_WORKOUT_NAME)});
        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle("Rename \"" + Globals.currentWorkout.getWorkoutName() + "\"")
                .setView(popupView)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button saveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String oldName = Globals.currentWorkout.getWorkoutName();
                        String newName = renameInput.getText().toString().trim();
                        String errorMsg = InputHelper.validWorkoutName(newName, workoutNames);
                        if (errorMsg != null) {
                            renameInput.setError(errorMsg);
                        } else {
                            workoutNames.remove(oldName);
                            workoutNames.add(0, newName);
                            selectedWorkoutTV.setText(newName);
                            arrayAdapter.notifyDataSetChanged();

                            Globals.currentWorkout.setWorkoutName(newName);
                            workoutModel.updateWorkoutName(oldName, newName);
                            alertDialog.dismiss();
                        }
                    }
                });
            }
        });
        alertDialog.show();


        // make the message font a little bigger than the default one provided by the alertdialog
        TextView messageTV = alertDialog.getWindow().findViewById(android.R.id.message);
        messageTV.setTextSize(18);
    }

    private void promptDelete() {
        /*
            Prompt user if they actually want to delete the currently selected workout
         */
        String message = "Are you sure you wish to permanently delete \"" + Globals.currentWorkout.getWorkoutName() + "\"?" +
                "\n\nIf so, all statistics for it will also be deleted.";
        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle("Delete Workout")
                .setMessage(message)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteWorkout();
                    }
                })
                .setNegativeButton("No", null)
                .create();
        alertDialog.show();
        // make the message font a little bigger than the default one provided by the alertdialog
        TextView messageTV = alertDialog.getWindow().findViewById(android.R.id.message);
        messageTV.setTextSize(18);
    }

    private void deleteWorkout() {
        /*
            Deletes the currently selected workout from the DB by using an async task
         */
        metaEntities.remove(Globals.currentWorkout);
        deleteWorkoutTask = new DeleteWorkoutAsync();
        deleteWorkoutTask.execute(Globals.currentWorkout);
    }

    private class DeleteWorkoutAsync extends AsyncTask<MetaEntity, Void, Void> {

        @Override
        protected Void doInBackground(MetaEntity... param) {
            metaModel.delete(param[0]);
            workoutModel.deleteEntireWorkout(param[0].getWorkoutName());
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            workoutNames.remove(0); // remove the old selected workout
            if (!workoutNames.isEmpty()) {
                // still more workouts left, so select the one at the top
                Globals.currentWorkout = workoutNameToEntity.get(workoutNames.get(0)); // get the top of the list
                Globals.currentWorkout.setCurrentWorkout(true);
                Date date = new Date();
                Globals.currentWorkout.setDateLast(formatter.format(date));
                metaModel.update(Globals.currentWorkout);
                selectedWorkoutTV.setText(Globals.currentWorkout.getWorkoutName());
                arrayAdapter.notifyDataSetChanged();
            } else {
                // signal to go make a new workout, all workouts have been deleted
                Globals.currentWorkout = null;
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.default_layout, fragmentContainer, false);
                FloatingActionButton createWorkoutBtn = view.findViewById(R.id.create_workout_btn);
                createWorkoutBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((WorkoutActivity) getActivity()).createWorkout();
                    }
                });
                ViewGroup rootView = (ViewGroup) getView();
                rootView.removeAllViews();
                rootView.addView(view);
            }
        }
    }
}
