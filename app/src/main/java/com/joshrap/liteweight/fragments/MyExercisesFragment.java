package com.joshrap.liteweight.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.widget.SwitchCompat;

import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.joshrap.liteweight.*;
import com.joshrap.liteweight.activities.WorkoutActivity;
import com.joshrap.liteweight.adapters.ExerciseAdapter;
import com.joshrap.liteweight.imports.Globals;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.helpers.InputHelper;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;
import com.joshrap.liteweight.models.ExerciseUser;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.User;
import com.joshrap.liteweight.network.repos.UserRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import static android.os.Looper.getMainLooper;

public class MyExercisesFragment extends Fragment implements FragmentWithDialog {
    private ProgressDialog loadingDialog;
    private View view;
    private boolean filterDefault;
    private int customExerciseCount = 0;
    private String selectedFocus;
    private ArrayList<String> focusList = new ArrayList<>();
    private HashMap<String, ArrayList<ExerciseUser>> totalExercises = new HashMap<>(); // focus to exercise
    private AlertDialog alertDialog;
    @Inject
    UserRepository userRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((WorkoutActivity) getActivity()).updateToolbarTitle(Variables.MY_EXERCISES_TITLE);
        ((WorkoutActivity) getActivity()).toggleBackButton(false);
        Injector.getInjector(getContext()).inject(this);
        // TODO injection or view model???
        User user = Globals.user;
        for (String exerciseId : user.getUserExercises().keySet()) {
            ExerciseUser exerciseUser = user.getUserExercises().get(exerciseId);
            List<String> focusesForExercise = new ArrayList<>(exerciseUser.getFocuses());
            if (!exerciseUser.isDefaultExercise()) {
                // do the count here to avoid double counting if the exercise is in more than one focus
                customExerciseCount++;
            }
            for (String focus : focusesForExercise) {
                if (!focusList.contains(focus)) {
                    // found a new focus, so init the hash map with it
                    focusList.add(focus);
                    totalExercises.put(focus, new ArrayList<>());
                }
                totalExercises.get(focus).add(exerciseUser);
            }
        }
        view = inflater.inflate(R.layout.fragment_my_exercises, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadingDialog = new ProgressDialog(getActivity());
        loadingDialog.setCancelable(false);

        filterDefault = false; // TODO get from view model
        SwitchCompat filterSwitch = view.findViewById(R.id.filter_switch);
        filterSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // allow for custom exercises to be filtered
            filterDefault = isChecked;
            populateFocusListView();
        });

        FloatingActionButton createBtn = view.findViewById(R.id.new_exercise_btn);
        createBtn.setOnClickListener(v -> {
            if (!(customExerciseCount > Variables.MAX_NUMBER_OF_CUSTOM_EXERCISES)) {
                newExercisePopup();
            } else {
                // TODO premium check
                Toast.makeText(getContext(), "You already have the max number (" + Variables.MAX_NUMBER_OF_CUSTOM_EXERCISES +
                        ") of custom exercises allowed!", Toast.LENGTH_SHORT).show();
            }
        });
        Collections.sort(focusList);
        selectedFocus = focusList.get(0); // initially select first focus (todo get from view model)
        populateFocusListView();
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

    private void populateFocusListView() {
        /*
            Populates the focus list view
         */
        final ListView listView = view.findViewById(R.id.focus_list);
        ArrayAdapter arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_activated_1, focusList);
        listView.setAdapter(arrayAdapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            selectedFocus = listView.getItemAtPosition(position).toString();
            populateExercisesListView();
            // provide a "clicking" animation
            Animation animation1 = new AlphaAnimation(0.3f, 1.0f);
            animation1.setDuration(50);
            view.startAnimation(animation1);
        });
        // programmatically select selected focus
        listView.performItemClick(listView.getAdapter().getView(focusList.indexOf(selectedFocus), null, null),
                focusList.indexOf(selectedFocus), focusList.indexOf(selectedFocus));
        listView.setSelection(focusList.indexOf(selectedFocus));
        populateExercisesListView();
    }

    private void populateExercisesListView() {
        /*
            Populates the exercise list view based on the selected focus
         */
        final ListView listView = view.findViewById(R.id.exercise_list);
        ArrayList<ExerciseUser> exercisesForSelectedFocus = new ArrayList<>();
        if (totalExercises.get(selectedFocus) == null) {
            return;
        }
        if (!filterDefault) {
            exercisesForSelectedFocus.addAll(totalExercises.get(selectedFocus));
        } else {
            for (ExerciseUser exerciseUser : totalExercises.get(selectedFocus)) {
                if (!exerciseUser.isDefaultExercise()) {
                    exercisesForSelectedFocus.add(exerciseUser);
                }
            }
        }
        Collections.sort(exercisesForSelectedFocus);
        ExerciseAdapter exerciseAdapter = new ExerciseAdapter(getContext(), exercisesForSelectedFocus);
        listView.setAdapter(exerciseAdapter);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            ExerciseUser exercise = (ExerciseUser) listView.getItemAtPosition(position);
            ((WorkoutActivity) getActivity()).goToExerciseDetails(exercise.getExerciseId());
        });
    }

    // region Popup methods
    private void newExercisePopup() {
        /*
            Popup for creating a new exercise
         */
        final ArrayList<String> selectedFocuses = new ArrayList<>();
        View popupView = getLayoutInflater().inflate(R.layout.popup_new_exercise, null);
        final EditText exerciseNameInput = popupView.findViewById(R.id.exercise_name_input);
        exerciseNameInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_EXERCISE_NAME)});
        final TextInputLayout nameLayout = popupView.findViewById(R.id.exercise_name_input_layout);

        TableLayout focusTable = popupView.findViewById(R.id.table_layout);
        for (int i = 0; i < focusList.size(); i++) {
            // add a checkbox for each focus that is available
            TableRow row = new TableRow(getActivity());
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(lp);
            final CheckBox focus = new CheckBox(getContext());
            focus.setText(focusList.get(i));
            focus.setTextSize(20);
            focus.setOnClickListener(v -> {
                if (!focus.isChecked()) {
                    selectedFocuses.remove(focus.getText().toString());
                } else {
                    selectedFocuses.add(focus.getText().toString());
                }
            });
            row.addView(focus);
            focusTable.addView(row, i);
        }
        // view is all set up, so now create the dialog with it
        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle("Create Exercise")
                .setView(popupView)
                .setNegativeButton("Back", null)
                .setPositiveButton("Save", null)
                .create();
        alertDialog.setOnShowListener(dialogInterface -> {
            Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                String exerciseName = exerciseNameInput.getText().toString().trim();
                Set<String> allExerciseNames = new HashSet<>();
                for (String focus : totalExercises.keySet()) {
                    for (ExerciseUser exerciseUser : totalExercises.get(focus)) {
                        allExerciseNames.add(exerciseUser.getExerciseName());
                    }
                }
                String nameError = InputHelper.validNewExerciseName(exerciseName, new ArrayList<>(allExerciseNames));
                if (selectedFocuses.isEmpty()) {
                    Toast.makeText(getContext(), "Select at least one focus!", Toast.LENGTH_SHORT).show();
                } else if (nameError == null) {
                    alertDialog.dismiss();
                    showLoadingDialog();
                    Executor executor = Executors.newSingleThreadExecutor();
                    executor.execute(() -> {
                        ResultStatus<ExerciseUser> resultStatus = this.userRepository.newExercise(exerciseName, selectedFocuses);
                        Handler handler = new Handler(getMainLooper());
                        handler.post(() -> {
                            loadingDialog.dismiss();
                            if (resultStatus.isSuccess()) {
                                ExerciseUser exerciseUser = resultStatus.getData();
                                Globals.user.getUserExercises().put(exerciseUser.getExerciseId(), exerciseUser);
                                ((WorkoutActivity) getActivity()).goToExerciseDetails(exerciseUser.getExerciseId());
                            } else {
                                showErrorMessage("Exercise Add Error", resultStatus.getErrorMessage());
                            }
                        });
                    });
                    Toast.makeText(getContext(), "Exercise successfully created!", Toast.LENGTH_SHORT).show();
                } else {
                    // there was an error with the name
                    nameLayout.setError(nameError);
                }
            });
        });
        alertDialog.show();
    }

    private void showErrorMessage(String title, String message) {
        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Ok", null)
                .create();
        alertDialog.show();
        // make the message font a little bigger than the default one provided by the alertdialog
        TextView messageTV = alertDialog.getWindow().findViewById(android.R.id.message);
        messageTV.setTextSize(18);
    }

    private void showLoadingDialog() {
        loadingDialog.setMessage("Loading...");
        loadingDialog.show();
    }
}
