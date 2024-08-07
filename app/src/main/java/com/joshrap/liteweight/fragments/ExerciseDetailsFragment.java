package com.joshrap.liteweight.fragments;

import androidx.appcompat.app.AlertDialog;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import com.google.android.material.textfield.TextInputLayout;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.activities.MainActivity;
import com.joshrap.liteweight.adapters.FocusAdapter;
import com.joshrap.liteweight.managers.SelfManager;
import com.joshrap.liteweight.models.user.OwnedExerciseWorkout;
import com.joshrap.liteweight.managers.CurrentUserModule;
import com.joshrap.liteweight.utils.AndroidUtils;
import com.joshrap.liteweight.utils.ExerciseUtils;
import com.joshrap.liteweight.utils.ValidatorUtils;
import com.joshrap.liteweight.utils.WeightUtils;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;
import com.joshrap.liteweight.models.user.OwnedExercise;
import com.joshrap.liteweight.models.Result;
import com.joshrap.liteweight.models.user.User;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.inject.Inject;

import static android.content.Context.CLIPBOARD_SERVICE;
import static android.os.Looper.getMainLooper;

public class ExerciseDetailsFragment extends Fragment implements FragmentWithDialog {

    private AlertDialog alertDialog;
    private OwnedExercise exercise;
    private String exerciseId;
    private TextInputLayout exerciseNameLayout, weightLayout, setsLayout, repsLayout, detailsLayout, urlLayout;
    private EditText exerciseNameInput, weightInput, setsInput, repsInput, detailsInput, urlInput;
    private boolean metricUnits;
    private ClipboardManager clipboard;
    private List<String> focusList, selectedFocuses;
    private int focusRotationAngle;
    private RelativeLayout focusRelativeLayout;
    private TextView focusesTV;
    private final MutableLiveData<String> focusTitle = new MutableLiveData<>();
    private final List<String> existingExerciseNames = new ArrayList<>();

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
        ((MainActivity) activity).updateToolbarTitle(Variables.EXERCISE_DETAILS_TITLE);

        clipboard = (ClipboardManager) activity.getSystemService(CLIPBOARD_SERVICE);
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

        return inflater.inflate(R.layout.fragment_exercise_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FragmentActivity activity = requireActivity();

        List<String> workoutList = new ArrayList<>(exercise.getWorkouts()).stream().map(OwnedExerciseWorkout::getWorkoutName).collect(Collectors.toList());
        selectedFocuses = new ArrayList<>(exercise.getFocuses());

        focusesTV = view.findViewById(R.id.focus_list_tv);
        focusTitle.setValue(ExerciseUtils.getFocusTitle(selectedFocuses));
        focusTitle.observe(getViewLifecycleOwner(), this::setFocusTextView);

        Button deleteExercise = view.findViewById(R.id.delete_exercise_icon_btn);
        deleteExercise.setOnClickListener(v -> {
            ((MainActivity) activity).hideKeyboard();
            promptDelete();
        });

        exerciseNameLayout = view.findViewById(R.id.exercise_name_input_layout);
        weightLayout = view.findViewById(R.id.default_weight_input_layout);
        weightLayout.setHint(String.format("Default Weight (%s", metricUnits ? "kg)" : "lb)"));
        setsLayout = view.findViewById(R.id.default_sets_input_layout);
        repsLayout = view.findViewById(R.id.default_reps_input_layout);
        detailsLayout = view.findViewById(R.id.default_details_input_layout);
        urlLayout = view.findViewById(R.id.url_input_layout);

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

        detailsInput = view.findViewById(R.id.default_details_input);
        detailsInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_DETAILS_LENGTH)});
        detailsInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(detailsLayout));

        urlInput = view.findViewById(R.id.url_input);
        urlInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_URL_LENGTH)});
        urlInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(urlLayout));

        Button clipboardBtn = view.findViewById(R.id.copy_clipboard_btn);
        Button previewBtn = view.findViewById(R.id.preview_video_btn);
        previewBtn.setOnClickListener(v -> {
            alertDialog = new AlertDialog.Builder(requireContext())
                    .setTitle("Launch Video")
                    .setMessage(R.string.launch_video_msg)
                    .setPositiveButton("Yes", (dialog, which) -> ExerciseUtils.launchVideo(urlInput.getText().toString().trim(), getContext()))
                    .setNegativeButton("No", null)
                    .create();
            alertDialog.show();
        });
        clipboardBtn.setOnClickListener(v -> {
            ((MainActivity) activity).hideKeyboard();
            clipboard.setPrimaryClip(new ClipData(ClipData.newPlainText("url", urlInput.getText().toString().trim())));
            Toast.makeText(getContext(), "Link copied to clipboard.", Toast.LENGTH_SHORT).show();
        });

        TextView workoutListTv = view.findViewById(R.id.workout_list_tv);
        if (workoutList.isEmpty()) {
            workoutListTv.setText(R.string.none);
        } else {
            workoutList.sort(Comparator.comparing(String::toLowerCase));
	        StringBuilder workouts = getWorkoutsDisplay(workoutList);

	        workoutListTv.setText(workouts.toString());
        }

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

        initViews();
    }

	private static StringBuilder getWorkoutsDisplay(List<String> workoutList) {
		StringBuilder workouts = new StringBuilder();
		int maxSize = 5; // only show 5 workouts
		if (workoutList.size() <= maxSize) {
		    // don't append the "+ x more"
		    for (int i = 0; i < workoutList.size(); i++) {
		        // don't have comma at last entry
		        workouts.append(workoutList.get(i)).append((i < workoutList.size() - 1) ? ", " : "");
		    }
		} else {
		    for (int i = 0; i < maxSize; i++) {
		        // don't have comma at last entry
		        workouts.append(workoutList.get(i)).append((i < maxSize - 1) ? ", " : "");
		    }
		    workouts.append("\n + ").append(workoutList.size() - maxSize).append(" more");
		}
		return workouts;
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
        detailsInput.setText(exercise.getDefaultDetails());
        urlInput.setText(exercise.getVideoUrl());
    }

    private void saveExercise() {
        String renameError = null;
        String weightError;
        String setsError;
        String repsError;
        String detailsError;
        String urlError = null;
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

        detailsError = ValidatorUtils.validDetails(detailsInput.getText().toString().trim());
        detailsLayout.setError(detailsError);

        if (!urlInput.getText().toString().isEmpty()) {
            // try to validate the url if user has inputted something
            urlError = ValidatorUtils.validUrl(urlInput.getText().toString().trim());
        }
        urlLayout.setError(urlError);

        if (selectedFocuses.isEmpty()) {
            focusError = true;
            focusRelativeLayout.startAnimation(AndroidUtils.shakeError(4));
            Toast.makeText(getContext(), "Must select at least one focus.", Toast.LENGTH_LONG).show();
        }

        if (renameError == null && weightError == null && setsError == null &&
                repsError == null && detailsError == null && urlError == null && !focusError) {
            double weight = Double.parseDouble(weightInput.getText().toString().trim());
            if (metricUnits) {
                weight = WeightUtils.metricWeightToImperial(weight);
            }

            OwnedExercise updatedExercise = new OwnedExercise();
            updatedExercise.setName(exerciseNameInput.getText().toString().trim());
            updatedExercise.setDefaultWeight(weight);
            updatedExercise.setDefaultSets(Integer.parseInt(setsInput.getText().toString().trim()));
            updatedExercise.setDefaultReps(Integer.parseInt(repsInput.getText().toString().trim()));
            updatedExercise.setDefaultDetails(detailsInput.getText().toString().trim());
            updatedExercise.setVideoUrl(urlInput.getText().toString().trim());
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

    /**
     * Prompt user if they actually want to delete the currently selected workout
     */
    private void promptDelete() {
        // exercise name is italicized
        SpannableString span1 = new SpannableString("Are you sure you wish to permanently delete ");
        SpannableString span2 = new SpannableString(exercise.getName());
        SpannableString span3 = new SpannableString("?\n\nIf so, this exercise will be removed from ALL workouts that contain it.");
        span2.setSpan(new StyleSpan(Typeface.ITALIC), 0, span2.length(), 0);
        CharSequence title = TextUtils.concat(span1, span2, span3);

        alertDialog = new AlertDialog.Builder(requireContext())
                .setTitle("Delete Exercise")
                .setMessage(title)
                .setPositiveButton("Yes", (dialog, which) -> deleteExercise())
                .setNegativeButton("No", null)
                .create();
        alertDialog.show();
    }

    private void deleteExercise() {
        AndroidUtils.showLoadingDialog(loadingDialog, "Deleting...");
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Result<String> result = this.selfManager.deleteExercise(exerciseId);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (result.isSuccess()) {
                    ((MainActivity) requireActivity()).finishFragment();
                } else {
                    AndroidUtils.showErrorDialog(result.getErrorMessage(), getContext());

                }
            });
        });
    }
}
