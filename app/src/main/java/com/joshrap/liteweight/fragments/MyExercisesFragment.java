package com.joshrap.liteweight.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.joshrap.liteweight.*;
import com.joshrap.liteweight.activities.MainActivity;
import com.joshrap.liteweight.adapters.ExerciseAdapter;
import com.joshrap.liteweight.managers.CurrentUserModule;
import com.joshrap.liteweight.utils.AndroidUtils;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.models.user.OwnedExercise;
import com.joshrap.liteweight.models.user.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

public class MyExercisesFragment extends Fragment {

    private static final String SELECTED_FOCUS_KEY = "selectedFocus";
    private String selectedFocus;
    private boolean isPremium;
    private HashMap<String, ArrayList<OwnedExercise>> totalExercises; // focus to exercise list
    private List<String> focusList;

    @Inject
    CurrentUserModule currentUserModule;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        Injector.getInjector(getContext()).inject(this);

        ((MainActivity) getActivity()).updateToolbarTitle(Variables.MY_EXERCISES_TITLE);
        ((MainActivity) getActivity()).toggleBackButton(false);

        focusList = Variables.FOCUS_LIST;
        totalExercises = new HashMap<>();
        User user = currentUserModule.getUser();
        isPremium = user.getPremiumToken() != null; // todo abstract into method
        for (String focus : focusList) {
            // init the map of a specific focus to the list of exercises it contains
            totalExercises.put(focus, new ArrayList<>());
        }

        for (OwnedExercise exercise : user.getExercises()) {
            List<String> focusesOfExercise = new ArrayList<>(exercise.getFocuses());
            for (String focus : focusesOfExercise) {
                if (!focusList.contains(focus)) {
                    // somehow found a new focus, so init the hash map with it (this should never happen but just in case)
                    focusList.add(focus);
                    totalExercises.put(focus, new ArrayList<>());
                }
                totalExercises.get(focus).add(exercise);
            }
        }
        return inflater.inflate(R.layout.fragment_my_exercises, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FloatingActionButton createBtn = view.findViewById(R.id.new_exercise_fab);
        createBtn.setOnClickListener(v -> {
            if (!isPremium && currentUserModule.getUser().getTotalExerciseCount() >= Variables.MAX_NUMBER_OF_FREE_EXERCISES) {
                AndroidUtils.showErrorDialog("You already have the max number (" + Variables.MAX_NUMBER_OF_FREE_EXERCISES + ") of exercises allowed.", getContext());
            } else if (isPremium && currentUserModule.getUser().getTotalExerciseCount() >= Variables.MAX_NUMBER_OF_EXERCISES) {
                AndroidUtils.showErrorDialog("You already have the max number (" + Variables.MAX_NUMBER_OF_EXERCISES + ") of exercises allowed.", getContext());
            } else {
                // no errors
                ((MainActivity) getActivity()).goToNewExercise();
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

    private void populateFocusListView() {
        ListView listView = getView().findViewById(R.id.focus_list_view);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_activated_1, focusList);
        listView.setAdapter(arrayAdapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            selectedFocus = listView.getItemAtPosition(position).toString();
            populateExercisesListView();
        });
        // programmatically select selected focus
        listView.setItemChecked(focusList.indexOf(selectedFocus), true);
    }

    /**
     * Populates the exercise list view based on the selected focus
     */
    private void populateExercisesListView() {
        ListView listView = getView().findViewById(R.id.exercise_list_view);
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
            ((MainActivity) getActivity()).goToExerciseDetails(exercise.getId());
        });
    }
}

