package com.joshrap.liteweight.fragments;

import static android.os.Looper.getMainLooper;

import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import com.google.android.material.textfield.TextInputLayout;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.activities.MainActivity;
import com.joshrap.liteweight.adapters.FocusAdapter;
import com.joshrap.liteweight.adapters.SaveExerciseLinkAdapter;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;
import com.joshrap.liteweight.managers.CurrentUserModule;
import com.joshrap.liteweight.managers.SelfManager;
import com.joshrap.liteweight.models.Result;
import com.joshrap.liteweight.models.user.Link;
import com.joshrap.liteweight.models.user.OwnedExercise;
import com.joshrap.liteweight.models.user.User;
import com.joshrap.liteweight.utils.AndroidUtils;
import com.joshrap.liteweight.utils.ExerciseUtils;
import com.joshrap.liteweight.utils.ValidatorUtils;
import com.joshrap.liteweight.utils.WeightUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.inject.Inject;

public class EditExerciseFragment extends Fragment implements FragmentWithDialog {

    private AlertDialog alertDialog;
    private OwnedExercise exercise;
    private String exerciseId;
    private TextInputLayout exerciseNameLayout, weightLayout, setsLayout, repsLayout, notesLayout;
    private EditText exerciseNameInput, weightInput, setsInput, repsInput, notesInput;
    private boolean metricUnits;
    private List<String> focusList, selectedFocuses;
    private int focusRotationAngle;
    private RelativeLayout focusRelativeLayout, linksLayout;
    private TextView focusesTV;
    private final MutableLiveData<String> focusTitle = new MutableLiveData<>();
    private final List<String> existingExerciseNames = new ArrayList<>();
    private SaveExerciseLinkAdapter linksAdapter;

    @Inject
    AlertDialog loadingDialog;
    @Inject
    SelfManager selfManager;
    @Inject
    CurrentUserModule currentUserModule;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentActivity activity = requireActivity();
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        Injector.getInjector(getContext()).inject(this);
        ((MainActivity) activity).toggleBackButton(true);
        ((MainActivity) activity).updateToolbarTitle(Variables.EDIT_EXERCISE_TITLE);

        if (this.getArguments() != null) {
            exerciseId = this.getArguments().getString(Variables.EXERCISE_ID);
        } else {
            return null;
        }

        User user = currentUserModule.getUser();
        exercise = new OwnedExercise(user.getExercise(exerciseId));
        metricUnits = user.getSettings().isMetricUnits();
        existingExerciseNames.addAll(user.getExercises().stream().map(OwnedExercise::getName).collect(Collectors.toList()));
        focusList = Variables.FOCUS_LIST;

        return inflater.inflate(R.layout.fragment_edit_exercise, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FragmentActivity activity = requireActivity();

        selectedFocuses = new ArrayList<>(exercise.getFocuses());

        focusesTV = view.findViewById(R.id.focus_list_tv);
        focusTitle.setValue(ExerciseUtils.getFocusTitle(selectedFocuses));
        focusTitle.observe(getViewLifecycleOwner(), this::setFocusTextView);

        exerciseNameLayout = view.findViewById(R.id.exercise_name_input_layout);
        weightLayout = view.findViewById(R.id.default_weight_input_layout);
        weightLayout.setHint(String.format("Default Weight (%s", metricUnits ? "kg)" : "lb)"));
        setsLayout = view.findViewById(R.id.default_sets_input_layout);
        repsLayout = view.findViewById(R.id.default_reps_input_layout);
        notesLayout = view.findViewById(R.id.notes_input_layout);
        linksLayout = view.findViewById(R.id.links_container);

        exerciseNameInput = view.findViewById(R.id.exercise_name_input);
        exerciseNameInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_EXERCISE_NAME)});
        exerciseNameInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(exerciseNameLayout));

        weightInput = view.findViewById(R.id.default_weight_input);
        weightInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_WEIGHT_DIGITS)});
        weightInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(weightLayout));

        setsInput = view.findViewById(R.id.default_sets_input);
        setsInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_SETS_DIGITS)});
        setsInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(setsLayout));

        repsInput = view.findViewById(R.id.default_reps_input);
        repsInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_REPS_DIGITS)});
        repsInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(repsLayout));

        notesInput = view.findViewById(R.id.notes_input);
        notesInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_NOTES_LENGTH)});
        notesInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(notesLayout));

        Button saveButton = view.findViewById(R.id.save_fab);
        saveButton.setOnClickListener(v -> {
            ((MainActivity) activity).hideKeyboard();
            saveExercise();
        });

        RecyclerView focusRecyclerView = view.findViewById(R.id.pick_focuses_recycler_view);
        FocusAdapter addFocusAdapter = new FocusAdapter(focusList, selectedFocuses, focusTitle);

        focusRecyclerView.setAdapter(addFocusAdapter);
        focusRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        ImageButton focusRowIcon = view.findViewById(R.id.focus_icon_btn);
        focusRelativeLayout = view.findViewById(R.id.focus_title_container);

        View.OnClickListener focusLayoutClicked = v -> {
            ((MainActivity) activity).hideKeyboard();
            boolean visible = focusRecyclerView.getVisibility() == View.VISIBLE;
            focusRecyclerView.setVisibility(visible ? View.GONE : View.VISIBLE);
            focusRotationAngle = focusRotationAngle == 0 ? 180 : 0;
            focusRowIcon.animate().rotation(focusRotationAngle).setDuration(400).start();
            if (visible) {
                // provide smooth animation when closing
                TransitionManager.beginDelayedTransition((ViewGroup) view.getParent(), new AutoTransition());
            }
        };
        focusRelativeLayout.setOnClickListener(focusLayoutClicked);
        focusRowIcon.setOnClickListener(focusLayoutClicked);

        RecyclerView linksRecyclerView = view.findViewById(R.id.exercise_links_recycler_view);
        linksAdapter = new SaveExerciseLinkAdapter(exercise.getLinks(), this::promptEditLink);

        linksRecyclerView.setAdapter(linksAdapter);
        linksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ImageButton addLinkBtn = view.findViewById(R.id.add_link_icon_btn);
        addLinkBtn.setOnClickListener(x -> promptAddLink());

        initViews();
    }

    private void setFocusTextView(String title) {
        if (title == null) {
            focusesTV.setText(R.string.none);
        } else {
            focusesTV.setText(title);
        }
    }

    @Override
    public void hideAllDialogs() {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private void initViews() {
        exerciseNameInput.setText(exercise.getName());
        weightInput.setText(WeightUtils.getFormattedWeightForEditText(WeightUtils.getConvertedWeight(metricUnits, exercise.getDefaultWeight())));
        setsInput.setText(String.format(Locale.getDefault(), Integer.toString(exercise.getDefaultSets())));
        repsInput.setText(String.format(Locale.getDefault(), Integer.toString(exercise.getDefaultReps())));
        notesInput.setText(exercise.getNotes());
    }

    private void saveExercise() {
        String renameError = null;
        String weightError;
        String setsError;
        String repsError;
        String notesError;
        boolean urlError = false;
        boolean focusError = false;

        if (!exerciseNameInput.getText().toString().equals(exercise.getName())) {
            // make sure that if the user doesn't change the name that they can still update other fields
            renameError = ValidatorUtils.validNewExerciseName(exerciseNameInput.getText().toString().trim(), existingExerciseNames);
            exerciseNameLayout.setError(renameError);
        }

        weightError = ValidatorUtils.validWeight(weightInput.getText().toString().trim());
        weightLayout.setError(weightError);

        setsError = ValidatorUtils.validSets(setsInput.getText().toString().trim());
        setsLayout.setError(setsError);

        repsError = ValidatorUtils.validReps(repsInput.getText().toString().trim());
        repsLayout.setError(repsError);

        notesError = ValidatorUtils.validNotes(notesInput.getText().toString().trim());
        notesLayout.setError(notesError);

        if (selectedFocuses.isEmpty()) {
            focusError = true;
            focusRelativeLayout.startAnimation(AndroidUtils.shakeError(4));
            Toast.makeText(getContext(), "Must select at least one focus.", Toast.LENGTH_LONG).show();
        }

        if (exercise.getLinks().size() > Variables.MAX_LINKS) {
            urlError = true;
            linksLayout.startAnimation(AndroidUtils.shakeError(4));
            Toast.makeText(getContext(), String.format("Cannot have more than %s links.", Variables.MAX_LINKS), Toast.LENGTH_LONG).show();
        }

        if (renameError == null && weightError == null && setsError == null &&
                repsError == null && notesError == null && !urlError && !focusError) {
            double weight = Double.parseDouble(weightInput.getText().toString().trim());
            if (metricUnits) {
                weight = WeightUtils.metricWeightToImperial(weight);
            }

            OwnedExercise updatedExercise = new OwnedExercise();
            updatedExercise.setName(exerciseNameInput.getText().toString().trim());
            updatedExercise.setDefaultWeight(weight);
            updatedExercise.setDefaultSets(Integer.parseInt(setsInput.getText().toString().trim()));
            updatedExercise.setDefaultReps(Integer.parseInt(repsInput.getText().toString().trim()));
            updatedExercise.setNotes(notesInput.getText().toString().trim());
            updatedExercise.setLinks(exercise.getLinks().stream().map(x -> new Link(x.getUrl(), x.getLabel())).collect(Collectors.toList()));
            updatedExercise.setFocuses(selectedFocuses);

            // no errors, so go ahead and save
            AndroidUtils.showLoadingDialog(loadingDialog, "Saving...");
            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                Result<OwnedExercise> result = this.selfManager.updateExercise(exerciseId, updatedExercise);
                Handler handler = new Handler(getMainLooper());
                handler.post(() -> {
                    loadingDialog.dismiss();
                    if (result.isSuccess()) {
                        Toast.makeText(getContext(), "Exercise successfully updated.", Toast.LENGTH_LONG).show();
                    } else {
                        AndroidUtils.showErrorDialog(result.getErrorMessage(), getContext());
                    }
                });
            });
        }
    }

    private void promptAddLink() {
        View popupView = getLayoutInflater().inflate(R.layout.popup_save_link, null);
        EditText urlInput = popupView.findViewById(R.id.url_input);
        TextInputLayout urlInputLayout = popupView.findViewById(R.id.url_input_layout);
        urlInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_URL_LENGTH)});
        urlInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(urlInputLayout));

        EditText labelInput = popupView.findViewById(R.id.label_name_input);
        TextInputLayout labelInputLayout = popupView.findViewById(R.id.label_input_layout);
        labelInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_LABEL_LENGTH)});
        labelInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(labelInputLayout));

        alertDialog = new AlertDialog.Builder(requireContext())
                .setTitle("New Link")
                .setView(popupView)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();
        alertDialog.setOnShowListener(dialogInterface -> {
            Button saveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveButton.setOnClickListener(view -> {
                String newUrl = urlInput.getText().toString().trim();
                String newLabel = labelInput.getText().toString().trim();

                String urlMsg = ValidatorUtils.validUrl(newUrl);
                String labelMsg = ValidatorUtils.validLinkLabel(newLabel);

                urlInputLayout.setError(urlMsg);
                labelInputLayout.setError(labelMsg);

                if (urlMsg == null && labelMsg == null) {
                    Link link = new Link(newUrl, newLabel);
                    exercise.getLinks().add(link);
                    linksAdapter.notifyItemInserted(exercise.getLinks().size() - 1);
                    alertDialog.dismiss();
                }
            });
        });
        alertDialog.show();
    }

    private void promptEditLink(Link link) {
        View popupView = getLayoutInflater().inflate(R.layout.popup_save_link, null);
        EditText urlInput = popupView.findViewById(R.id.url_input);
        TextInputLayout urlInputLayout = popupView.findViewById(R.id.url_input_layout);
        urlInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_URL_LENGTH)});
        urlInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(urlInputLayout));
        urlInput.setText(link.getUrl());

        EditText labelInput = popupView.findViewById(R.id.label_name_input);
        TextInputLayout labelInputLayout = popupView.findViewById(R.id.label_input_layout);
        labelInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_LABEL_LENGTH)});
        labelInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(labelInputLayout));
        labelInput.setText(link.getLabel());

        alertDialog = new AlertDialog.Builder(requireContext())
                .setTitle("Edit Link")
                .setView(popupView)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();
        alertDialog.setOnShowListener(dialogInterface -> {
            Button saveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveButton.setOnClickListener(view -> {
                String newUrl = urlInput.getText().toString().trim();
                String newLabel = labelInput.getText().toString().trim();

                String urlMsg = ValidatorUtils.validUrl(newUrl);
                String labelMsg = ValidatorUtils.validLinkLabel(newLabel);

                urlInputLayout.setError(urlMsg);
                labelInputLayout.setError(labelMsg);

                if (urlMsg != null && labelMsg != null) {
                    link.setUrl(newUrl);
                    link.setLabel(newLabel);
                }
            });
        });
        alertDialog.show();
    }
}
