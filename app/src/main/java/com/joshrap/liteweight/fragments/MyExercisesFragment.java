package com.joshrap.liteweight.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.joshrap.liteweight.*;
import com.joshrap.liteweight.activities.WorkoutActivity;
import com.joshrap.liteweight.adapters.ExerciseAdapter;
import com.joshrap.liteweight.utils.AndroidUtils;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;
import com.joshrap.liteweight.models.OwnedExercise;
import com.joshrap.liteweight.models.User;
import com.joshrap.liteweight.models.UserWithWorkout;
import com.joshrap.liteweight.network.repos.UserRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

public class MyExercisesFragment extends Fragment implements FragmentWithDialog {

    private static final String SELECTED_FOCUS_KEY = "selectedFocus";
    private String selectedFocus;
    private User user;
    private HashMap<String, ArrayList<OwnedExercise>> totalExercises; // focus to exercise list
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
                AndroidUtils.showErrorDialog("Too many exercises", "You already have the max number (" + Variables.MAX_NUMBER_OF_FREE_EXERCISES + ") of exercises allowed.", getContext());
            } else if (user.getPremiumToken() != null
                    && user.getOwnedExercises().size() >= Variables.MAX_NUMBER_OF_EXERCISES) {
                AndroidUtils.showErrorDialog("Too many exercises", "You already have the max number (" + Variables.MAX_NUMBER_OF_EXERCISES + ") of exercises allowed.", getContext());
            } else {
                // no errors so let user create new exercise
                ((WorkoutActivity) getActivity()).goToNewExercise();
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
    }

    private void populateFocusListView() {
        ListView listView = getView().findViewById(R.id.focus_list);
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
        ListView listView = getView().findViewById(R.id.exercise_list);
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
}

