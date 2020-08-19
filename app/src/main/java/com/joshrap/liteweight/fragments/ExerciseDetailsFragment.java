package com.joshrap.liteweight.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.activities.WorkoutActivity;
import com.joshrap.liteweight.helpers.ExerciseHelper;
import com.joshrap.liteweight.helpers.InputHelper;
import com.joshrap.liteweight.helpers.WeightHelper;
import com.joshrap.liteweight.imports.Globals;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;
import com.joshrap.liteweight.models.ExerciseUser;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.User;
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
    private static final int weightHelpMode = 0, setsRepsHelpMode = 1, detailsHelpMode = 2;

    private AlertDialog alertDialog;
    private User user;
    private ProgressDialog loadingDialog;
    private ExerciseUser originalExercise;
    private String exerciseId;
    private TextInputLayout exerciseNameLayout, weightLayout, setsLayout, repsLayout, detailsLayout, urlLayout;
    private EditText exerciseNameInput, weightInput, setsInput, repsInput, detailsInput, urlInput;
    private boolean metricUnits;
    private ClipboardManager clipboard;
    private List<String> exerciseNames;
    @Inject
    SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Injector.getInjector(getContext()).inject(this);
        ((WorkoutActivity) getActivity()).enableBackButton(true);
        ((WorkoutActivity) getActivity()).updateToolbarTitle(Variables.EXERCISE_DETAILS_TITLE);

        metricUnits = sharedPreferences.getBoolean(Variables.UNIT_KEY, false);
        clipboard = (ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE);
        if (this.getArguments() != null) {
            exerciseId = this.getArguments().getString(Variables.EXERCISE_ID);
        }
        // TODO injection or view model???
        user = Globals.user;
        exerciseNames = new ArrayList<>();
        for (String exerciseId : user.getUserExercises().keySet()) {
            exerciseNames.add(user.getUserExercises().get(exerciseId).getExerciseName());
        }

        return inflater.inflate(R.layout.fragment_exercise_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        List<String> workoutList = new ArrayList<>(user.getUserExercises().get(exerciseId).getWorkouts().values());
        originalExercise = user.getUserExercises().get(exerciseId);
        loadingDialog = new ProgressDialog(getActivity());
        loadingDialog.setCancelable(false);

        exerciseNameLayout = view.findViewById(R.id.exercise_name_input_layout);
        exerciseNameInput = view.findViewById(R.id.exercise_name_input);
        exerciseNameInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_EXERCISE_NAME)});
        exerciseNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (exerciseNameLayout.isErrorEnabled()) {
                    // if an error is present, stop showing the error message once the user types (acknowledged it)
                    exerciseNameLayout.setErrorEnabled(false);
                    exerciseNameLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        ImageButton deleteExercise = view.findViewById(R.id.delete_exercise);
        if (originalExercise.isDefaultExercise()) {
            deleteExercise.setVisibility(View.GONE);
        }

        weightLayout = view.findViewById(R.id.default_weight_input_layout);
        weightLayout.setHint("Default Weight (" + (metricUnits ? "kg)" : "lb)"));
        setsLayout = view.findViewById(R.id.default_sets_input_layout);
        repsLayout = view.findViewById(R.id.default_reps_input_layout);
        detailsLayout = view.findViewById(R.id.default_details_input_layout);
        urlLayout = view.findViewById(R.id.url_input_layout);

        weightInput = view.findViewById(R.id.default_weight_input);
        weightInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_WEIGHT_DIGITS)});
        weightInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (weightLayout.isErrorEnabled()) {
                    // if an error is present, stop showing the error message once the user types (acknowledged it)
                    weightLayout.setErrorEnabled(false);
                    weightLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        setsInput = view.findViewById(R.id.default_sets_input);
        setsInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_SETS_DIGITS)});
        setsInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (setsLayout.isErrorEnabled()) {
                    // if an error is present, stop showing the error message once the user types (acknowledged it)
                    setsLayout.setErrorEnabled(false);
                    setsLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        repsInput = view.findViewById(R.id.default_reps_input);
        repsInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_REPS_DIGITS)});
        repsInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (repsLayout.isErrorEnabled()) {
                    // if an error is present, stop showing the error message once the user types (acknowledged it)
                    repsLayout.setErrorEnabled(false);
                    repsLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        detailsInput = view.findViewById(R.id.default_details_input);
        detailsInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_DETAILS_LENGTH)});
        detailsInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (detailsLayout.isErrorEnabled()) {
                    // if an error is present, stop showing the error message once the user types (acknowledged it)
                    detailsLayout.setErrorEnabled(false);
                    detailsLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        urlInput = view.findViewById(R.id.url_input);
        urlInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_URL_LENGTH)});
        urlInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (urlLayout.isErrorEnabled()) {
                    // if an error is present, stop showing the error message once the user types (acknowledged it)
                    urlLayout.setErrorEnabled(false);
                    urlLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        final ImageButton clipboardBtn = view.findViewById(R.id.clipboard_btn);
        final ImageButton previewBtn = view.findViewById(R.id.preview_btn);
        previewBtn.setOnClickListener(v -> ExerciseHelper.launchVideo(urlInput.getText().toString().trim(), getContext()));
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
            workoutListTv.setText("Exercise is not apart of any workouts");
        } else {
            Collections.sort(workoutList);
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
                workouts.append("\n + " + (workoutList.size() - maxSize) + " more");
            }

            workoutListTv.setText(workouts.toString());
        }

        if (originalExercise.isDefaultExercise()) {
            exerciseNameLayout.setVisibility(View.GONE);
        }

        TextView defaultExerciseTv = view.findViewById(R.id.default_exercise_tv);
        defaultExerciseTv.setText(originalExercise.getExerciseName());
        defaultExerciseTv.setVisibility(originalExercise.isDefaultExercise() ? View.VISIBLE : View.GONE);

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

        ImageView weightHelpButton = view.findViewById(R.id.default_weight_help);
        weightHelpButton.setOnClickListener(v -> showHelpPopup(weightHelpMode));
        ImageView repsSetsHelpButton = view.findViewById(R.id.default_sets_reps_help);
        repsSetsHelpButton.setOnClickListener(v -> showHelpPopup(setsRepsHelpMode));
        ImageView detailsHelpButton = view.findViewById(R.id.default_details_help);
        detailsHelpButton.setOnClickListener(v -> showHelpPopup(detailsHelpMode));

        initViews();
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

    private void initViews() {
        exerciseNameInput.setText(originalExercise.getExerciseName());
        weightInput.setText(WeightHelper.getFormattedWeight(WeightHelper.getConvertedWeight(metricUnits, originalExercise.getDefaultWeight())));
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


        if (!originalExercise.isDefaultExercise()) {
            renameError = InputHelper.validNewExerciseName(exerciseNameInput.getText().toString().trim(), exerciseNames);
            exerciseNameLayout.setError(renameError);
        }

        weightError = InputHelper.validWeight(weightInput.getText().toString().trim());
        weightLayout.setError(weightError);

        setsError = InputHelper.validSets(setsInput.getText().toString().trim());
        setsLayout.setError(setsError);

        repsError = InputHelper.validReps(repsInput.getText().toString().trim());
        repsLayout.setError(repsError);

        detailsError = InputHelper.validDetails(detailsInput.getText().toString().trim());
        detailsLayout.setError(detailsError);

        if (!urlInput.getText().toString().isEmpty()) {
            // try to validate the url if user has inputted something
            urlError = InputHelper.validUrl(urlInput.getText().toString().trim());
        }
        urlLayout.setError(urlError);
        if (renameError == null && weightError == null && setsError == null &&
                repsError == null && detailsError == null && urlError == null) {
            ExerciseUser updatedExercise = ExerciseUser.getExerciseForUpdate(originalExercise);
            if (!updatedExercise.isDefaultExercise()) {
                updatedExercise.setExerciseName(exerciseNameInput.getText().toString().trim());
            }
            updatedExercise.setDefaultWeight(Double.parseDouble(weightInput.getText().toString().trim()));
            updatedExercise.setDefaultSets(Integer.parseInt(setsInput.getText().toString().trim()));
            updatedExercise.setDefaultReps(Integer.parseInt(repsInput.getText().toString().trim()));
            updatedExercise.setDefaultDetails(detailsInput.getText().toString().trim());
            updatedExercise.setVideoUrl(urlInput.getText().toString().trim());

            // no errors, so go ahead and save
            showLoadingDialog();
            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                ResultStatus<User> resultStatus = UserRepository.updateExercise(exerciseId, updatedExercise);
                Handler handler = new Handler(getMainLooper());
                handler.post(() -> {
                    loadingDialog.dismiss();
                    if (resultStatus.isSuccess()) {
                        Toast.makeText(getContext(), "Exercise successfully updated.", Toast.LENGTH_LONG).show();
                        user = resultStatus.getData();
                        Globals.user = user;

                        originalExercise = user.getUserExercises().get(exerciseId);
                        initViews();
                    } else {
                        showErrorMessage("Exercise Update Error", resultStatus.getErrorMessage());
                    }
                });
            });
        }

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

    private void deleteExercise() {

    }

    private void showLoadingDialog() {
        loadingDialog.setMessage("Loading...");
        loadingDialog.show();
    }

    private void showHelpPopup(int mode) {
        String msg = "";
        String title = "";
        switch (mode) {
            case weightHelpMode:
                title = "Default Weight";
                msg = getString(R.string.default_weight_help_msg);
                break;
            case setsRepsHelpMode:
                title = "Sets and Reps";
                msg = getString(R.string.default_sets_reps_help_msg);
                break;
            case detailsHelpMode:
                title = "Details";
                msg = getString(R.string.default_details_help_msg);
                break;
        }

        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("Back", null)
                .create();
        alertDialog.show();
    }
}
