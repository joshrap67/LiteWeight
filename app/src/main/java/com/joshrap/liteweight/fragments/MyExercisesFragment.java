package com.joshrap.liteweight.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.joshrap.liteweight.*;
import com.joshrap.liteweight.activities.WorkoutActivity;
import com.joshrap.liteweight.adapters.ExerciseAdapter;
import com.joshrap.liteweight.utils.AndroidUtils;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.utils.ValidatorUtils;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;
import com.joshrap.liteweight.models.OwnedExercise;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.User;
import com.joshrap.liteweight.models.UserWithWorkout;
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

    private static final String SELECTED_FOCUS_KEY = "selectedFocus";
    private String selectedFocus;
    private User user;
    private HashMap<String, ArrayList<OwnedExercise>> totalExercises; // focus to exercise list
    private AlertDialog alertDialog;
    private List<String> focusList;
    @Inject
    ProgressDialog loadingDialog;
    @Inject
    UserRepository userRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Injector.getInjector(getContext()).inject(this);

        ((WorkoutActivity) getActivity()).updateToolbarTitle(Variables.MY_EXERCISES_TITLE);
        ((WorkoutActivity) getActivity()).toggleBackButton(false);

        focusList = Variables.FOCUS_LIST;
        UserWithWorkout userWithWorkout = ((WorkoutActivity) getActivity()).getUserWithWorkout();
        totalExercises = new HashMap<>();
        user = userWithWorkout.getUser();
        for (String focus : focusList) {
            // init the map of a specific focus to the list of exercises it contains
            totalExercises.put(focus, new ArrayList<>());
        }

        for (String exerciseId : user.getOwnedExercises().keySet()) {
            OwnedExercise ownedExercise = user.getOwnedExercises().get(exerciseId);
            List<String> focusesOfExercise = new ArrayList<>(ownedExercise.getFocuses());
            for (String focus : focusesOfExercise) {
                if (!focusList.contains(focus)) {
                    // somehow found a new focus, so init the hash map with it (this should never happen but just in case)
                    focusList.add(focus);
                    totalExercises.put(focus, new ArrayList<>());
                }
                totalExercises.get(focus).add(ownedExercise);
            }
        }
        return inflater.inflate(R.layout.fragment_my_exercises, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FloatingActionButton createBtn = view.findViewById(R.id.new_exercise_btn);
        createBtn.setOnClickListener(v -> {

            if (user.getPremiumToken() == null
                    && user.getOwnedExercises().size() >= Variables.MAX_NUMBER_OF_FREE_EXERCISES) {
                AndroidUtils.showErrorDialog("Too many exercises", "You already have the max number (" + Variables.MAX_NUMBER_OF_FREE_EXERCISES + ") of free exercises allowed. Upgrade to premium for more.", getContext());
            } else if (user.getPremiumToken() != null
                    && user.getOwnedExercises().size() >= Variables.MAX_NUMBER_OF_EXERCISES) {
                AndroidUtils.showErrorDialog("Too many exercises", "You already have the max number (" + Variables.MAX_NUMBER_OF_EXERCISES + ") of exercises allowed.", getContext());
            } else {
                // no errors so let user create new exercise
                newExercisePopup();
            }
        });
        Collections.sort(focusList);
        if (savedInstanceState != null) {
            selectedFocus = savedInstanceState.getString(SELECTED_FOCUS_KEY);
        } else {
            selectedFocus = focusList.get(0); // initially select first focus if this is first time using this fragment
        }
        populateFocusListView();
        populateExercisesListView();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SELECTED_FOCUS_KEY, selectedFocus);
    }

    @Override
    public void onPause() {
        hideAllDialogs();
        super.onPause();
    }

    @Override
    public void hideAllDialogs() {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }

    private void populateFocusListView() {
        final ListView listView = getView().findViewById(R.id.focus_list);
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
        listView.setItemChecked(focusList.indexOf(selectedFocus), true);
    }

    /**
     * Populates the exercise list view based on the selected focus
     */
    private void populateExercisesListView() {
        final ListView listView = getView().findViewById(R.id.exercise_list);
        if (totalExercises.get(selectedFocus) == null) {
            return;
        }
        ArrayList<OwnedExercise> exercisesForSelectedFocus = new ArrayList<>(totalExercises.get(selectedFocus));
        Collections.sort(exercisesForSelectedFocus);

        ExerciseAdapter exerciseAdapter = new ExerciseAdapter(getContext(), exercisesForSelectedFocus);
        listView.setAdapter(exerciseAdapter);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            OwnedExercise exercise = (OwnedExercise) listView.getItemAtPosition(position);
            ((WorkoutActivity) getActivity()).goToExerciseDetails(exercise.getExerciseId());
        });
    }

    private void newExercisePopup() {
        final ArrayList<String> selectedFocuses = new ArrayList<>();
        View popupView = getLayoutInflater().inflate(R.layout.popup_new_exercise, null);
        final TextInputLayout nameLayout = popupView.findViewById(R.id.exercise_name_input_layout);
        final EditText exerciseNameInput = popupView.findViewById(R.id.exercise_name_input);
        exerciseNameInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_EXERCISE_NAME)});
        exerciseNameInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(nameLayout));

        RecyclerView focusRecyclerView = popupView.findViewById(R.id.pick_focuses_recycler_view);
        AddFocusAdapter addFocusAdapter = new AddFocusAdapter(focusList, selectedFocuses);
        focusRecyclerView.setAdapter(addFocusAdapter);
        focusRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle("Create Exercise")
                .setView(popupView)
                .setNegativeButton("Back", null)
                .setPositiveButton("Create", null)
                .create();
        alertDialog.setOnShowListener(dialogInterface -> {
            Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                String exerciseName = exerciseNameInput.getText().toString().trim();
                Set<String> allExerciseNames = new HashSet<>();
                for (String focus : totalExercises.keySet()) {
                    for (OwnedExercise ownedExercise : totalExercises.get(focus)) {
                        allExerciseNames.add(ownedExercise.getExerciseName());
                    }
                }
                String nameError = ValidatorUtils.validNewExerciseName(exerciseName, new ArrayList<>(allExerciseNames));
                if (selectedFocuses.isEmpty()) {
                    Toast.makeText(getContext(), "Select at least one focus!", Toast.LENGTH_SHORT).show();
                } else if (nameError == null) {
                    alertDialog.dismiss();
                    AndroidUtils.showLoadingDialog(loadingDialog, "Creating exercise...");
                    Executor executor = Executors.newSingleThreadExecutor();
                    executor.execute(() -> {
                        ResultStatus<OwnedExercise> resultStatus = this.userRepository.newExercise(exerciseName, selectedFocuses);
                        Handler handler = new Handler(getMainLooper());
                        handler.post(() -> {
                            loadingDialog.dismiss();
                            if (resultStatus.isSuccess()) {
                                OwnedExercise ownedExercise = resultStatus.getData();
                                user.getOwnedExercises().put(ownedExercise.getExerciseId(), ownedExercise);
                                ((WorkoutActivity) getActivity()).goToExerciseDetails(ownedExercise.getExerciseId());
                            } else {
                                AndroidUtils.showErrorDialog("Create Exercise Error", resultStatus.getErrorMessage(), getContext());
                            }
                        });
                    });
                } else {
                    // there was an error with the name
                    nameLayout.setError(nameError);
                }
            });
        });
        alertDialog.show();
    }

    private class AddFocusAdapter extends
            RecyclerView.Adapter<AddFocusAdapter.ViewHolder> {
        class ViewHolder extends RecyclerView.ViewHolder {
            CheckBox focusCheckbox;

            ViewHolder(View itemView) {
                super(itemView);
                focusCheckbox = itemView.findViewById(R.id.focus_checkbox);
            }
        }

        private List<String> focuses;
        private List<String> selectedFocuses;

        AddFocusAdapter(List<String> focuses, List<String> selectedFocuses) {
            this.focuses = focuses;
            this.selectedFocuses = selectedFocuses;
        }

        @Override
        public AddFocusAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View focusView = inflater.inflate(R.layout.row_add_focus, parent, false);
            return new ViewHolder(focusView);
        }

        @Override
        public void onBindViewHolder(AddFocusAdapter.ViewHolder holder, int position) {
            final String focus = focuses.get(position);
            final CheckBox focusCheckbox = holder.focusCheckbox;
            focusCheckbox.setText(focus);
            focusCheckbox.setOnClickListener(v -> {
                if (!focusCheckbox.isChecked()) {
                    selectedFocuses.remove(focus);
                } else {
                    selectedFocuses.add(focus);
                }
            });
        }

        @Override
        public int getItemCount() {
            return focuses.size();
        }
    }
}

