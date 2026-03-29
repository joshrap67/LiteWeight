package com.joshrap.liteweight.fragments.dialogs;

import static android.os.Looper.getMainLooper;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.adapters.FocusAdapter;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.managers.CurrentUserModule;
import com.joshrap.liteweight.managers.SelfManager;
import com.joshrap.liteweight.models.Result;
import com.joshrap.liteweight.models.user.OwnedExercise;
import com.joshrap.liteweight.utils.AndroidUtils;
import com.joshrap.liteweight.utils.ValidatorUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

public class CreateExerciseDialog extends DialogFragment {
    private static final String TITLE = "title";
    private static final String POSITIVE_BTN = "positive_btn";
    private static final String NEGATIVE_BTN = "negative_btn";
    private static final String INITIAL_EXERCISE_NAME = "initial_exercise_name";

    private CreateExerciseDialog.Listener listener;
    private boolean creationDisabled;

    @Inject
    CurrentUserModule currentUserModule;
    @Inject
    SelfManager selfManager;

    public interface Listener {
        void onSubmit(OwnedExercise ownedExercise);
    }

    public static final class Builder {
        private String title = "";
        private String initialExerciseId;
        private CreateExerciseDialog.Listener submitListener = null;

        public CreateExerciseDialog.Builder title(@NonNull String title) {
            this.title = title;
            return this;
        }

        public CreateExerciseDialog.Builder initialExerciseId(@NonNull String initialExerciseId) {
            this.initialExerciseId = initialExerciseId;
            return this;
        }

        public CreateExerciseDialog.Builder onSubmit(@Nullable CreateExerciseDialog.Listener listener) {
            this.submitListener = listener;
            return this;
        }

        public CreateExerciseDialog build() {
            CreateExerciseDialog fragment = new CreateExerciseDialog();

            Bundle args = new Bundle();
            args.putString(TITLE, title);
            args.putString(POSITIVE_BTN, "OK");
            args.putString(NEGATIVE_BTN, "Cancel");
            args.putString(INITIAL_EXERCISE_NAME, initialExerciseId);
            fragment.setArguments(args);

            fragment.listener = submitListener;
            return fragment;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Injector.getInjector(getContext()).inject(this);
        if (currentUserModule.getUser().getTotalExerciseCount() >= Variables.MAX_NUMBER_OF_EXERCISES) {
            creationDisabled = true;
        }

        Bundle args = requireArguments();
        String title = args.getString(TITLE, "Create New Exercise");
        String positiveBtn = args.getString(POSITIVE_BTN, "Create and Add");
        String negativeBtn = args.getString(NEGATIVE_BTN, "Cancel");
        String initialExerciseName = args.getString(INITIAL_EXERCISE_NAME, null);

        View popupView = getLayoutInflater().inflate(R.layout.popup_create_exercise, null);
        AlertDialog createExerciseDialog = new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setView(popupView)
                .setPositiveButton(positiveBtn, null)
                .setNegativeButton(negativeBtn, null)
                .create();

        EditText exerciseNameInput = popupView.findViewById(R.id.exercise_name_input);
        TextInputLayout exerciseNameLayout = popupView.findViewById(R.id.exercise_name_input_layout);
        TextView focusTV = popupView.findViewById(R.id.focus_tv);
        ProgressBar loadingBar = popupView.findViewById(R.id.loading_progress_bar);

        exerciseNameInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_EXERCISE_NAME)});
        exerciseNameInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(exerciseNameLayout));
        // nice little shortcut to not make the user type out a non-existent exercise they were looking for
        String defaultName = initialExerciseName != null ? initialExerciseName.trim() : "";
        exerciseNameInput.setText(defaultName);

        exerciseNameInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // hack as usual to get android to show keyboard when input is focused
                createExerciseDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        });
        if (creationDisabled) {
            exerciseNameLayout.setError("Max number of exercises reached (" + Variables.MAX_NUMBER_OF_EXERCISES + ")");
        }

        RecyclerView focusRecyclerView = popupView.findViewById(R.id.pick_focuses_recycler_view);

        List<String> focusList = new ArrayList<>(Variables.FOCUS_LIST);
        List<String> selectedFocuses = new ArrayList<>();
        FocusAdapter addFocusAdapter = new FocusAdapter(focusList, selectedFocuses, null);
        focusRecyclerView.setAdapter(addFocusAdapter);
        focusRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));


        createExerciseDialog.setOnShowListener(dialogInterface -> {
            if (defaultName.isEmpty()) {
                // if not pre-filling name, bring focus to name input to save user a click
                exerciseNameInput.requestFocus();
            }
        });
        createExerciseDialog.show();

        createExerciseDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (creationDisabled) {
                exerciseNameLayout.setError("Max number of exercises reached (" + Variables.MAX_NUMBER_OF_EXERCISES + ")");
                return;
            }

            String nameError;
            boolean focusError = false;

            List<String> exerciseNames = new ArrayList<>();
            for (OwnedExercise exercise : currentUserModule.getUser().getExercises()) {
                exerciseNames.add(exercise.getName());
            }
            nameError = ValidatorUtils.validNewExerciseName(exerciseNameInput.getText().toString().trim(), exerciseNames);
            exerciseNameLayout.setError(nameError);

            if (selectedFocuses.isEmpty()) {
                focusError = true;
                focusTV.startAnimation(AndroidUtils.shakeError(4));
                Toast.makeText(getContext(), "Must select at least one focus.", Toast.LENGTH_LONG).show();
            }

            if (nameError == null && !focusError && !creationDisabled) {
                String exerciseName = exerciseNameInput.getText().toString().trim();
                createExerciseDialog.setCancelable(false);
                loadingBar.setVisibility(View.VISIBLE);

                Executor executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    Result<OwnedExercise> result = selfManager.newExercise(
                            exerciseName, selectedFocuses, Variables.DEFAULT_WEIGHT, Variables.DEFAULT_SETS, Variables.DEFAULT_REPS, "", new ArrayList<>());
                    Handler handler = new Handler(getMainLooper());
                    handler.post(() -> {
                        loadingBar.setVisibility(View.GONE);
                        createExerciseDialog.setCancelable(true);
                        if (result.isSuccess()) {
                            OwnedExercise newExercise = result.getData();
                            listener.onSubmit(newExercise);
                            createExerciseDialog.dismiss();
                        } else {
                            AndroidUtils.showErrorDialog(result.getErrorMessage(), getContext());
                        }
                    });
                });
            }
        });
        return createExerciseDialog;
    }
}
