package com.joshrap.liteweight.fragments;

import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.joshrap.liteweight.database.entities.ExerciseEntity;
import com.joshrap.liteweight.database.entities.WorkoutEntity;
import com.joshrap.liteweight.database.viewModels.ExerciseViewModel;
import com.joshrap.liteweight.database.viewModels.MetaViewModel;
import com.joshrap.liteweight.database.viewModels.WorkoutViewModel;
import com.joshrap.liteweight.helpers.ExerciseHelper;
import com.joshrap.liteweight.helpers.WorkoutHelper;
import com.joshrap.liteweight.imports.Globals;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;
import com.joshrap.liteweight.activities.MainActivity;
import com.joshrap.liteweight.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class EditWorkoutFragment extends Fragment implements FragmentWithDialog {
    private View view;
    private ListView exerciseListView;
    private PendingExerciseAdapter exerciseAdapter;
    private TableLayout pickExerciseTable;
    private Button doneEditingBtn;
    private ImageButton previousDayBtn, nextDayBtn;
    private FloatingActionButton addExercisesBtn;
    private TextView dayTitle;
    private AlertDialog alertDialog;
    private WorkoutViewModel workoutModel;
    private MetaViewModel metaModel;
    private ExerciseViewModel exerciseViewModel;
    private String spinnerFocus;
    private boolean firstDay, lastDay, editing;
    private ScrollView addExercisesScrollView;
    private int maxDayIndex, currentDayIndex, numDays, netChange;
    private ArrayList<String> focusList = new ArrayList<>();
    private HashMap<Integer, ArrayList<String>> pendingWorkout = new HashMap<>();
    private HashMap<String, ArrayList<String>> allExercises = new HashMap<>();
    private HashMap<String, ExerciseEntity> exerciseNameToEntity = new HashMap<>();
    private HashMap<Integer, ArrayList<String>> originalWorkout = new HashMap<>();
    private HashMap<Integer, ArrayList<String>> deletedExercises = new HashMap<>();
    private HashMap<Integer, ArrayList<String>> newExercises = new HashMap<>();
    private GetAllExercisesTask getAllExercisesTask;
    private GetWorkoutTask getWorkoutTask;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.edit_workout, container, false);
        addExercisesBtn = view.findViewById(R.id.add_exercises);
        previousDayBtn = view.findViewById(R.id.previous_day_button);
        nextDayBtn = view.findViewById(R.id.next_day_button);
        dayTitle = view.findViewById(R.id.day_text_view);
        doneEditingBtn = view.findViewById(R.id.done_editing);
        hideViews();

        ((MainActivity) getActivity()).enableBackButton(true);
        ((MainActivity) getActivity()).updateToolbarTitle(Globals.currentWorkout.getWorkoutName());
        metaModel = ViewModelProviders.of(getActivity()).get(MetaViewModel.class);
        workoutModel = ViewModelProviders.of(getActivity()).get(WorkoutViewModel.class);
        exerciseViewModel = ViewModelProviders.of(getActivity()).get(ExerciseViewModel.class);
        getExercises();
        return view;
    }

    private void hideViews() {
        // set to invisible initially so there isn't a weird flash after DB results load
        previousDayBtn.setVisibility(View.INVISIBLE);
        nextDayBtn.setVisibility(View.INVISIBLE);
        dayTitle.setVisibility(View.INVISIBLE);
        doneEditingBtn.setVisibility(View.INVISIBLE);
    }

    private void showViews() {
        // after the DB results are in re show the views (hide save button until changes are made)
        previousDayBtn.setVisibility(View.VISIBLE);
        nextDayBtn.setVisibility(View.VISIBLE);
        dayTitle.setVisibility(View.VISIBLE);
        doneEditingBtn.setVisibility(View.INVISIBLE);
    }

    private void getExercises() {
        getAllExercisesTask = new GetAllExercisesTask();
        getAllExercisesTask.execute();
    }

    @Override
    public void hideAllDialogs() {
        /*
            Hides any dialog that is currently opened.
         */
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }

    @Override
    public void onStop() {
        if (getAllExercisesTask != null) {
            getAllExercisesTask.cancel(true);
        }
        if (getWorkoutTask != null) {
            getWorkoutTask.cancel(true);
        }
        super.onStop();

    }

    private class GetAllExercisesTask extends AsyncTask<Void, Void, ArrayList<ExerciseEntity>> {

        @Override
        protected void onPreExecute() {
            if (getActivity() != null) {
                ((MainActivity) getActivity()).setProgressBar(true);
            }
        }

        @Override
        protected ArrayList<ExerciseEntity> doInBackground(Void... voids) {
            return exerciseViewModel.getAllExercises();
        }

        @Override
        protected void onPostExecute(ArrayList<ExerciseEntity> result) {
            if (!result.isEmpty()) {
                for (ExerciseEntity entity : result) {
                    String[] focuses = entity.getFocus().split(Variables.FOCUS_DELIM_DB);
                    for (String focus : focuses) {
                        // need to populate all focuses that are in the DB
                        if (!focusList.contains(focus)) {
                            focusList.add(focus);
                            allExercises.put(focus, new ArrayList<String>());
                        }
                        allExercises.get(focus).add(entity.getExerciseName());
                    }
                    exerciseNameToEntity.put(entity.getExerciseName(), entity);
                }
                getWorkoutTask = new GetWorkoutTask();
                getWorkoutTask.execute();
            }
        }
    }

    private class GetWorkoutTask extends AsyncTask<Void, Void, ArrayList<WorkoutEntity>> {

        @Override
        protected ArrayList<WorkoutEntity> doInBackground(Void... voids) {
            // get the entirety of the current workout from the database
            return workoutModel.getExercises(Globals.currentWorkout.getWorkoutName());
        }

        @Override
        protected void onPostExecute(ArrayList<WorkoutEntity> result) {
            if (getActivity() != null) {
                ((MainActivity) getActivity()).setProgressBar(false);
            }
            initEdit(result);
        }
    }

    private void initEdit(ArrayList<WorkoutEntity> rawData) {
        /*
            Received the workout from DB, so now change layouts to the edit one and init all views and variables
         */
        showViews();
        currentDayIndex = 0;
        maxDayIndex = Globals.currentWorkout.getMaxDayIndex();
        numDays = Globals.currentWorkout.getNumDays();
        for (int i = 0; i <= maxDayIndex; i++) {
            // init the hash tables
            pendingWorkout.put(i, new ArrayList<String>());
            deletedExercises.put(i, new ArrayList<String>());
            newExercises.put(i, new ArrayList<String>());
            originalWorkout.put(i, new ArrayList<String>());
        }
        for (WorkoutEntity entity : rawData) {
            // add the exercises to the hash tables
            pendingWorkout.get(entity.getDay()).add(entity.getExercise());
            originalWorkout.get(entity.getDay()).add(entity.getExercise());
        }
        exerciseListView = view.findViewById(R.id.list_view);
        Collections.sort(pendingWorkout.get(currentDayIndex), String.CASE_INSENSITIVE_ORDER);
        exerciseAdapter = new PendingExerciseAdapter(getContext(), pendingWorkout.get(currentDayIndex));
        exerciseListView.setAdapter(exerciseAdapter);
        setButtons();
    }

    private void setButtons() {
        /*
            Setup buttons to allow for cycling through all the days of the workout. Logic is included to ensure that the user
            does not go beyond the bounds of the days specified by their input. Also ensure that each day has at
            least one exercise before allowing output to DB.
         */
        firstDay = true;
        addExercisesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupAddExercises();
            }
        });
        dayTitle.setText(WorkoutHelper.generateDayTitle(currentDayIndex, numDays));
        previousDayBtn.setVisibility(View.INVISIBLE);
        if (maxDayIndex == 0) {
            // in case some jabroni only wants to workout one day total
            nextDayBtn.setVisibility(View.INVISIBLE);
        }
        previousDayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentDayIndex > 0) {
                    currentDayIndex--;
                }
                if (lastDay) {
                    lastDay = false;
                    nextDayBtn.setImageResource(R.drawable.next_icon);
                    nextDayBtn.setVisibility(View.VISIBLE);
                }

                if (currentDayIndex == 0) {
                    previousDayBtn.setVisibility(View.INVISIBLE);
                    firstDay = true;
                }
                exerciseAdapter = new PendingExerciseAdapter(getContext(), pendingWorkout.get(currentDayIndex));
                exerciseListView.setAdapter(exerciseAdapter);
                dayTitle.setText(WorkoutHelper.generateDayTitle(currentDayIndex, numDays));
            }
        });
        nextDayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentDayIndex < maxDayIndex) {
                    currentDayIndex++;
                    if (firstDay) {
                        firstDay = false;
                        previousDayBtn.setVisibility(View.VISIBLE);
                    }
                    if (currentDayIndex == maxDayIndex) {
                        lastDay = true;
                        nextDayBtn.setVisibility(View.INVISIBLE);
                    }
                    exerciseAdapter = new PendingExerciseAdapter(getContext(), pendingWorkout.get(currentDayIndex));
                    exerciseListView.setAdapter(exerciseAdapter);
                    dayTitle.setText(WorkoutHelper.generateDayTitle(currentDayIndex, numDays));
                }
            }
        });
        doneEditingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean ready = true;
                for (int i = 0; i < pendingWorkout.size(); i++) {
                    if (pendingWorkout.get(i) == null) {
                        ready = false;
                    } else if (pendingWorkout.get(i).isEmpty()) {
                        ready = false;
                    }
                }
                if (ready) {
                    writeToDatabase();
                    Toast.makeText(getContext(), "Workout successfully edited!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Ensure each day has at least one exercise!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void writeToDatabase() {
        /*
            Updates the workout in the DB
         */
        for (int i = 0; i <= maxDayIndex; i++) {
            for (String exercise : deletedExercises.get(i)) {
                workoutModel.deleteSpecificExerciseFromWorkout(Globals.currentWorkout.getWorkoutName(), exercise, i);
            }
            for (String exercise : newExercises.get(i)) {
                if (!originalWorkout.get(i).contains(exercise)) {
                    // don't update if for some reason they removed an exercise and added it back, want to keep the original status
                    WorkoutEntity newEntity = new WorkoutEntity(exercise, Globals.currentWorkout.getWorkoutName(), i, false);
                    workoutModel.insert(newEntity);
                }
            }
        }
        String mostCommonFocus = ExerciseHelper.mostFrequentFocus(pendingWorkout, exerciseNameToEntity, focusList);
        Globals.currentWorkout.setMostFrequentFocus(mostCommonFocus);
        metaModel.update(Globals.currentWorkout);
        // reset the original workout for future changes (so save button shows correctly)
        originalWorkout = new HashMap<>();
        for (int i = 0; i <= maxDayIndex; i++) {
            for (String exercise : pendingWorkout.get(i)) {
                if (originalWorkout.get(i) == null) {
                    originalWorkout.put(i, new ArrayList<String>());
                }
                originalWorkout.get(i).add(exercise);
            }
        }
        editing = false;
        netChange = 0;
        doneEditingBtn.setVisibility(View.GONE);
    }

    private void checkForChanges() {
        /*
            Only show the save button if there have been changes made to the workout. If the net change
            is not 0, then we always show it since that means exercises have been added or removed causing the
            number of exercises to change. If it is 0, loop through the original workout to check if the pending
            workout is the same as the original, if so do not show the save button.
         */
        if (netChange != 0) {
            // always show save button
            doneEditingBtn.setVisibility(View.VISIBLE);
        } else {
            // potential for there to be change, so check to see if workout is same as original
            boolean changeFound = false;
            for (int i = 0; i <= maxDayIndex; i++) {
                for (String exercise : pendingWorkout.get(i)) {
                    if (!originalWorkout.get(i).contains(exercise)) {
                        changeFound = true;
                    }
                }
            }
            editing = changeFound;
            doneEditingBtn.setVisibility((editing) ? View.VISIBLE : View.GONE);
        }
    }

    private void popupAddExercises() {
        /*
            User has indicated they wish to add exercises to this specific day. Show a popup that provides a spinner
            that is programmed to list all exercises for a given exercise focus.
         */
        View popupView = getLayoutInflater().inflate(R.layout.popup_add_exercise, null);
        addExercisesScrollView = popupView.findViewById(R.id.scroll_view);
        pickExerciseTable = popupView.findViewById(R.id.main_table);
        final Spinner focusSpinner = popupView.findViewById(R.id.focus_spinner);
        SearchView searchView = popupView.findViewById(R.id.search_input);
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // initially populate the scroll view with all exercises
                ArrayList<String> sortedExercises = new ArrayList<>();
                for (String focus : allExercises.keySet()) {
                    for (String exercise : allExercises.get(focus)) {
                        if (!sortedExercises.contains(exercise)) {
                            sortedExercises.add(exercise);
                        }
                    }
                }
                Collections.sort(sortedExercises, String.CASE_INSENSITIVE_ORDER);
                pickExerciseTable.removeAllViews();
                updateExercisesFromSearch(sortedExercises);
                focusSpinner.setVisibility(View.GONE);
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                focusSpinner.setVisibility(View.VISIBLE);
                pickExerciseTable.removeAllViews();
                updateExerciseChoices();
                return false;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                ArrayList<String> sortedExercises = new ArrayList<>();
                for (String focus : allExercises.keySet()) {
                    for (String exercise : allExercises.get(focus)) {
                        if (!sortedExercises.contains(exercise) &&
                                Pattern.compile(Pattern.quote(query), Pattern.CASE_INSENSITIVE).matcher(exercise).find()) {
                            sortedExercises.add(exercise);
                        }
                    }
                }
                Collections.sort(sortedExercises, String.CASE_INSENSITIVE_ORDER);
                pickExerciseTable.removeAllViews();
                updateExercisesFromSearch(sortedExercises);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // show suggestions as the user types
                ArrayList<String> sortedExercises = new ArrayList<>();
                for (String focus : allExercises.keySet()) {
                    for (String exercise : allExercises.get(focus)) {
                        if (!sortedExercises.contains(exercise) &&
                                Pattern.compile(Pattern.quote(newText), Pattern.CASE_INSENSITIVE).matcher(exercise).find()) {
                            sortedExercises.add(exercise);
                        }
                    }
                }
                Collections.sort(sortedExercises, String.CASE_INSENSITIVE_ORDER);
                pickExerciseTable.removeAllViews();
                updateExercisesFromSearch(sortedExercises);
                return false;
            }
        });

        Collections.sort(focusList);
        ArrayAdapter<String> focusAdapter = new ArrayAdapter<String>(
                getContext(), android.R.layout.simple_spinner_dropdown_item, focusList);
        focusSpinner.setAdapter(focusAdapter);
        focusSpinner.setOnItemSelectedListener(new SpinnerListener());
        // initially select first item from spinner, then always select the one the user last clicked
        focusSpinner.setSelection((spinnerFocus == null) ? 0 : focusList.indexOf(spinnerFocus));
        // view is all set up, so now create the dialog with it
        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle("Add Exercises")
                .setView(popupView)
                .setPositiveButton("Done", null)
                .create();
        alertDialog.show();
    }

    private void updateExercisesFromSearch(ArrayList<String> exercises) {
        /*
            Populates the list with all exercises that have matched a search input.
         */
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (exercises.isEmpty()) {
            TextView notFoundTV = new TextView(getActivity().getApplicationContext());
            notFoundTV.setText(getActivity().getResources().getString(R.string.exercise_not_found));
            pickExerciseTable.addView(notFoundTV, 0);
        }
        for (int i = 0; i < exercises.size(); i++) {
            final View row = inflater.inflate(R.layout.row_add_exercise, null);
            final CheckBox exerciseCheckbox = row.findViewById(R.id.exercise_checkbox);
            String exerciseName = exercises.get(i);
            exerciseCheckbox.setText(exerciseName);
            if (pendingWorkout.get(currentDayIndex).contains(exerciseName)) {
                exerciseCheckbox.setChecked(true);
            }
            exerciseCheckbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (exerciseCheckbox.isChecked()) {
                        netChange++;
                        pendingWorkout.get(currentDayIndex).add(exerciseCheckbox.getText().toString());
                        newExercises.get(currentDayIndex).add(exerciseCheckbox.getText().toString());
                    } else {
                        netChange--;
                        pendingWorkout.get(currentDayIndex).remove(exerciseCheckbox.getText().toString());
                        newExercises.get(currentDayIndex).remove(exerciseCheckbox.getText().toString());
                    }
                    Collections.sort(pendingWorkout.get(currentDayIndex), String.CASE_INSENSITIVE_ORDER);
                    checkForChanges();
                    exerciseAdapter.notifyDataSetChanged();
                }
            });
            pickExerciseTable.addView(row, i);
        }
        // scroll to top of list view once all the displayed exercises are shown
        addExercisesScrollView.fullScroll(View.FOCUS_UP);
        addExercisesScrollView.smoothScrollTo(0, 0);
    }

    private void updateExerciseChoices() {
        /*
            Given a value from the exercise focus spinner, list all the exercises associated with it.
         */
        ArrayList<String> sortedExercises = new ArrayList<>();
        for (String exercise : allExercises.get(spinnerFocus)) {
            sortedExercises.add(exercise);
        }
        Collections.sort(sortedExercises, String.CASE_INSENSITIVE_ORDER);
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int i = 0; i < sortedExercises.size(); i++) {
            final View row = inflater.inflate(R.layout.row_add_exercise, null);
            final CheckBox exerciseCheckbox = row.findViewById(R.id.exercise_checkbox);
            String exerciseName = sortedExercises.get(i);
            exerciseCheckbox.setText(exerciseName);
            if (pendingWorkout.get(currentDayIndex).contains(exerciseName)) {
                exerciseCheckbox.setChecked(true);
            }
            exerciseCheckbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (exerciseCheckbox.isChecked()) {
                        netChange++;
                        pendingWorkout.get(currentDayIndex).add(exerciseCheckbox.getText().toString());
                        newExercises.get(currentDayIndex).add(exerciseCheckbox.getText().toString());
                    } else {
                        netChange--;
                        pendingWorkout.get(currentDayIndex).remove(exerciseCheckbox.getText().toString());
                        newExercises.get(currentDayIndex).remove(exerciseCheckbox.getText().toString());
                    }
                    Collections.sort(pendingWorkout.get(currentDayIndex), String.CASE_INSENSITIVE_ORDER);
                    checkForChanges();
                    exerciseAdapter.notifyDataSetChanged();
                }
            });
            pickExerciseTable.addView(row, i);
        }
        // scroll to top of list view once all the displayed exercises are shown
        addExercisesScrollView.fullScroll(View.FOCUS_UP);
        addExercisesScrollView.smoothScrollTo(0, 0);
    }

    public boolean isEditing() {
        return editing;
    }

    private class SpinnerListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            spinnerFocus = parent.getItemAtPosition(pos).toString();
            pickExerciseTable.removeAllViews();
            updateExerciseChoices();
        }

        public void onNothingSelected(AdapterView parent) {
        }
    }

    private class PendingExerciseAdapter extends ArrayAdapter<String> {
        private Context context;
        private List<String> exerciseList;

        private PendingExerciseAdapter(@NonNull Context context, ArrayList<String> list) {
            super(context, 0, list);
            this.context = context;
            this.exerciseList = list;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View listItem = convertView;
            if (listItem == null) {
                listItem = LayoutInflater.from(context).inflate(R.layout.row_exercise_pending, parent, false);
            }
            final String currentExercise = exerciseList.get(position);
            TextView exerciseTV = listItem.findViewById(R.id.exercise_name);
            exerciseTV.setText(currentExercise);

            ImageButton deleteButton = listItem.findViewById(R.id.delete_exercise);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editing = true;
                    pendingWorkout.get(currentDayIndex).remove(currentExercise);
                    newExercises.get(currentDayIndex).remove(currentExercise);
                    deletedExercises.get(currentDayIndex).add(currentExercise);
                    netChange--;
                    checkForChanges();
                    notifyDataSetChanged();
                }
            });
            return listItem;
        }
    }
}
