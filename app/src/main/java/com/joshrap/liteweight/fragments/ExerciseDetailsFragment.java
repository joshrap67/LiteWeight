package com.joshrap.liteweight.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.activities.WorkoutActivity;
import com.joshrap.liteweight.utils.AndroidUtils;
import com.joshrap.liteweight.utils.ExerciseUtils;
import com.joshrap.liteweight.utils.ValidatorUtils;
import com.joshrap.liteweight.utils.WeightUtils;
import com.joshrap.liteweight.utils.WorkoutUtils;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;
import com.joshrap.liteweight.models.OwnedExercise;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.User;
import com.joshrap.liteweight.models.UserWithWorkout;
import com.joshrap.liteweight.network.repos.UserRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import static android.content.Context.CLIPBOARD_SERVICE;
import static android.os.Looper.getMainLooper;

public class ExerciseDetailsFragment extends Fragment implements FragmentWithDialog {

    private AlertDialog alertDialog;
    private User user;
    private OwnedExercise originalExercise;
    private String exerciseId;
    private TextInputLayout exerciseNameLayout, weightLayout, setsLayout, repsLayout, detailsLayout, urlLayout;
    private EditText exerciseNameInput, weightInput, setsInput, repsInput, detailsInput, urlInput;
    private boolean metricUnits;
    private ClipboardManager clipboard;
    private UserWithWorkout userWithWorkout;
    @Inject
    ProgressDialog loadingDialog;
    @Inject
    SharedPreferences sharedPreferences;
    @Inject
    UserRepository userRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Injector.getInjector(getContext()).inject(this);
        ((WorkoutActivity) getActivity()).toggleBackButton(true);
        ((WorkoutActivity) getActivity()).updateToolbarTitle(Variables.EXERCISE_DETAILS_TITLE);

        clipboard = (ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE);
        if (this.getArguments() != null) {
            exerciseId = this.getArguments().getString(Variables.EXERCISE_ID);
        } else {
            return null;
        }

        userWithWorkout = ((WorkoutActivity) getActivity()).getUserWithWorkout();
        user = userWithWorkout.getUser();
        metricUnits = user.getUserPreferences().isMetricUnits();


        return inflater.inflate(R.layout.fragment_exercise_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        List<String> workoutList = new ArrayList<>(user.getOwnedExercises().get(exerciseId).getWorkouts().values());
        originalExercise = user.getOwnedExercises().get(exerciseId);

        ImageButton deleteExercise = view.findViewById(R.id.delete_exercise);
        deleteExercise.setOnClickListener(v -> promptDelete());

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

        final ImageButton clipboardBtn = view.findViewById(R.id.clipboard_btn);
        final ImageButton previewBtn = view.findViewById(R.id.preview_btn);
        previewBtn.setOnClickListener(v -> ExerciseUtils.launchVideo(urlInput.getText().toString().trim(), getContext()));
        if (originalExercise.getVideoUrl().isEmpty()) {
            clipboardBtn.setVisibility(View.GONE);
            previewBtn.setVisibility(View.GONE);
        }
        clipboardBtn.setOnClickListener(v -> {
            clipboard.setPrimaryClip(new ClipData(ClipData.newPlainText("url", originalExercise.getVideoUrl())));
            Toast.makeText(getContext(), "Link copied to clipboard!", Toast.LENGTH_SHORT).show();
        });
        urlInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (v.hasFocus()) {
                clipboardBtn.setVisibility(View.GONE);
                previewBtn.setVisibility(View.GONE);
            } else {
                clipboardBtn.setVisibility(View.VISIBLE);
                previewBtn.setVisibility(View.VISIBLE);
            }
        });
        TextView workoutListTv = view.findViewById(R.id.workout_list_tv);
        if (workoutList.isEmpty()) {
            workoutListTv.setText("Exercise is not apart of any workouts.");
        } else {
            Collections.sort(workoutList, String::compareToIgnoreCase);
            StringBuilder workouts = new StringBuilder();
            int maxSize = 5; // only show 5 workouts
            if (maxSize >= workoutList.size()) {
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

            workoutListTv.setText(workouts.toString());
        }

        TextView focusesTv = view.findViewById(R.id.focuses);
        StringBuilder focuses = new StringBuilder();
        int count = 0;
        for (String focus : originalExercise.getFocuses()) {
            focuses.append(focus).append((count < originalExercise.getFocuses().size() - 1) ? ", " : "");
            count++;
        }
        focusesTv.setText(focuses.toString());

        Button saveButton = view.findViewById(R.id.save_btn);
        saveButton.setOnClickListener(v -> saveExercise());

        initViews();
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
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private void initViews() {
        exerciseNameInput.setText(originalExercise.getExerciseName());
        weightInput.setText(WeightUtils.getFormattedWeightForEditText(WeightUtils.getConvertedWeight(metricUnits, originalExercise.getDefaultWeight())));
        setsInput.setText(Integer.toString(originalExercise.getDefaultSets()));
        repsInput.setText(Integer.toString(originalExercise.getDefaultReps()));
        detailsInput.setText(originalExercise.getDefaultDetails());
        urlInput.setText(originalExercise.getVideoUrl());
    }

    private void saveExercise() {
        String renameError = null;
        String weightError;
        String setsError;
        String repsError;
        String detailsError;
        String urlError = null;

        if (!exerciseNameInput.getText().toString().equals(originalExercise.getExerciseName())) {
            // make sure that if the user doesn't change the name that they can still update other fields
            List<String> exerciseNames = new ArrayList<>();
            for (String exerciseId : user.getOwnedExercises().keySet()) {
                exerciseNames.add(user.getOwnedExercises().get(exerciseId).getExerciseName());
            }
            renameError = ValidatorUtils.validNewExerciseName(exerciseNameInput.getText().toString().trim(), exerciseNames);
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
        if (renameError == null && weightError == null && setsError == null &&
                repsError == null && detailsError == null && urlError == null) {
            OwnedExercise updatedExercise = OwnedExercise.getExerciseForUpdate(originalExercise);
            double weight = Double.parseDouble(weightInput.getText().toString().trim());
            if (metricUnits) {
                weight = WeightUtils.metricWeightToImperial(weight);
            }

            updatedExercise.setExerciseName(exerciseNameInput.getText().toString().trim());
            updatedExercise.setDefaultWeight(weight);
            updatedExercise.setDefaultSets(Integer.parseInt(setsInput.getText().toString().trim()));
            updatedExercise.setDefaultReps(Integer.parseInt(repsInput.getText().toString().trim()));
            updatedExercise.setDefaultDetails(detailsInput.getText().toString().trim());
            updatedExercise.setVideoUrl(urlInput.getText().toString().trim());

            // no errors, so go ahead and save
            AndroidUtils.showLoadingDialog(loadingDialog, "Saving...");
            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                ResultStatus<User> resultStatus = this.userRepository.updateExercise(exerciseId, updatedExercise);
                Handler handler = new Handler(getMainLooper());
                handler.post(() -> {
                    loadingDialog.dismiss();
                    if (resultStatus.isSuccess()) {
                        Toast.makeText(getContext(), "Exercise successfully updated.", Toast.LENGTH_LONG).show();
                        user.getOwnedExercises().put(exerciseId, resultStatus.getData().getOwnedExercises().get(exerciseId));

                        originalExercise = user.getOwnedExercises().get(exerciseId);
                        initViews();
                    } else {
                        AndroidUtils.showErrorDialog("Exercise Update Error", resultStatus.getErrorMessage(), getContext());
                    }
                });
            });
        }
    }

    /**
     * Prompt user if they actually want to delete the currently selected workout
     */
    private void promptDelete() {
        String message = "Are you sure you wish to permanently delete \"" + originalExercise.getExerciseName() + "\"?" +
                "\n\nIf so, this exercise will be removed from ALL workouts that contain it.";
        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle("Delete Exercise")
                .setMessage(message)
                .setPositiveButton("Yes", (dialog, which) -> deleteExercise())
                .setNegativeButton("No", null)
                .create();
        alertDialog.show();
    }

    private void deleteExercise() {
        AndroidUtils.showLoadingDialog(loadingDialog, "Deleting...");
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<String> resultStatus = this.userRepository.deleteExercise(exerciseId);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (resultStatus.isSuccess()) {
                    // deleted successfully, so delete everything
                    user.getOwnedExercises().remove(exerciseId);
                    if (userWithWorkout.getWorkout() != null) {
                        WorkoutUtils.deleteExerciseFromRoutine(exerciseId, userWithWorkout.getWorkout().getRoutine());
                    }
                    ((WorkoutActivity) getActivity()).finishFragment();
                } else {
                    AndroidUtils.showErrorDialog("Delete Exercise Error", resultStatus.getErrorMessage(), getContext());

                }
            });
        });
    }
}
