package com.joshrap.liteweight.fragments;

import static android.content.Context.CLIPBOARD_SERVICE;
import static android.os.Looper.getMainLooper;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.res.Resources;
import android.graphics.Typeface;
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
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import com.google.android.material.textfield.TextInputLayout;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.activities.MainActivity;
import com.joshrap.liteweight.adapters.FocusAdapter;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;
import com.joshrap.liteweight.managers.UserManager;
import com.joshrap.liteweight.models.user.OwnedExercise;
import com.joshrap.liteweight.models.Result;
import com.joshrap.liteweight.models.user.User;
import com.joshrap.liteweight.providers.CurrentUserAndWorkoutProvider;
import com.joshrap.liteweight.utils.AndroidUtils;
import com.joshrap.liteweight.utils.ExerciseUtils;
import com.joshrap.liteweight.utils.ValidatorUtils;
import com.joshrap.liteweight.utils.WeightUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

public class NewExerciseFragment extends Fragment implements FragmentWithDialog {

    private User user;
    private TextInputLayout exerciseNameLayout, weightLayout, setsLayout, repsLayout, detailsLayout, urlLayout;
    private EditText exerciseNameInput, weightInput, setsInput, repsInput, detailsInput, urlInput;
    private boolean metricUnits;
    private ClipboardManager clipboard;
    private List<String> focusList, selectedFocuses;
    private int focusRotationAngle;
    private RelativeLayout focusRelativeLayout;
    private TextView focusCountTV;
    private AlertDialog alertDialog;
    private final MutableLiveData<String> focusTitle = new MutableLiveData<>();

    @Inject
    AlertDialog loadingDialog;
    @Inject
    UserManager userManager;
    @Inject
    CurrentUserAndWorkoutProvider currentUserAndWorkoutProvider;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        Injector.getInjector(getContext()).inject(this);
        ((MainActivity) getActivity()).toggleBackButton(true);
        ((MainActivity) getActivity()).updateToolbarTitle(Variables.NEW_EXERCISE_TITLE);

        clipboard = (ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE);

        user = currentUserAndWorkoutProvider.provideCurrentUser();
        metricUnits = user.getUserPreferences().isMetricUnits();
        focusList = new ArrayList<>(Variables.FOCUS_LIST);
        selectedFocuses = new ArrayList<>();

        return inflater.inflate(R.layout.fragment_new_exercise, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        focusCountTV = view.findViewById(R.id.focus_count_text_view);
        focusTitle.setValue(ExerciseUtils.getFocusTitle(selectedFocuses));
        focusTitle.observe(getViewLifecycleOwner(), this::setFocusTextView);

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
        exerciseNameInput.setText("");

        weightInput = view.findViewById(R.id.default_weight_input);
        weightInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_WEIGHT_DIGITS)});
        weightInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(weightLayout));
        weightInput.setText(Integer.toString(Variables.DEFAULT_WEIGHT));

        setsInput = view.findViewById(R.id.default_sets_input);
        setsInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_SETS_DIGITS)});
        setsInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(setsLayout));
        setsInput.setText(Integer.toString(Variables.DEFAULT_SETS));

        repsInput = view.findViewById(R.id.default_reps_input);
        repsInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_REPS_DIGITS)});
        repsInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(repsLayout));
        repsInput.setText(Integer.toString(Variables.DEFAULT_REPS));

        detailsInput = view.findViewById(R.id.default_details_input);
        detailsInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_DETAILS_LENGTH)});
        detailsInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(detailsLayout));
        detailsInput.setText("");

        urlInput = view.findViewById(R.id.url_input);
        urlInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_URL_LENGTH)});
        urlInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(urlLayout));
        urlInput.setText("");

        Button clipboardBtn = view.findViewById(R.id.copy_clipboard_btn);
        Button previewBtn = view.findViewById(R.id.preview_video_btn);
        previewBtn.setOnClickListener(v -> {
            alertDialog = new AlertDialog.Builder(getContext())
                    .setTitle("Launch Video")
                    .setMessage(R.string.launch_video_msg)
                    .setPositiveButton("Yes", (dialog, which) -> ExerciseUtils.launchVideo(urlInput.getText().toString().trim(), getContext()))
                    .setNegativeButton("No", null)
                    .create();
            alertDialog.show();
        });
        clipboardBtn.setOnClickListener(v -> {
            ((MainActivity) getActivity()).hideKeyboard();
            String url = urlInput.getText().toString().trim();
            clipboard.setPrimaryClip(new ClipData(ClipData.newPlainText("url", url)));
            Toast.makeText(getContext(), "Link copied to clipboard.", Toast.LENGTH_SHORT).show();
        });

        Button saveButton = view.findViewById(R.id.save_fab);
        saveButton.setOnClickListener(v -> {
            ((MainActivity) getActivity()).hideKeyboard();
            createExercise();
        });

        RecyclerView focusRecyclerView = view.findViewById(R.id.pick_focuses_recycler_view);
        FocusAdapter addFocusAdapter = new FocusAdapter(focusList, selectedFocuses, focusTitle);
        focusRecyclerView.setAdapter(addFocusAdapter);
        focusRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        ImageButton focusRowIcon = view.findViewById(R.id.focus_icon_btn);
        focusRelativeLayout = view.findViewById(R.id.focus_container);

        View.OnClickListener focusLayoutClicked = v -> {
            ((MainActivity) getActivity()).hideKeyboard();
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
    }

    @Override
    public void hideAllDialogs() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }

    private void setFocusTextView(String title) {
        int focusCount = 0;
        if (title != null) {
            focusCount = title.split(ExerciseUtils.FOCUS_DELIMITER).length;
        }
        Resources res = getResources();
        focusCountTV.setText(res.getString(R.string.focus_selected_count, focusCount));
        focusCountTV.setTypeface(focusCountTV.getTypeface(), Typeface.ITALIC);
    }

    private void createExercise() {
        String nameError, weightError, setsError, repsError, detailsError, urlError = null;
        boolean focusError = false;

        List<String> exerciseNames = new ArrayList<>();
        for (OwnedExercise exercise : user.getExercises()) {
            exerciseNames.add(exercise.getName());
        }
        nameError = ValidatorUtils.validNewExerciseName(exerciseNameInput.getText().toString().trim(), exerciseNames);
        exerciseNameLayout.setError(nameError);

        weightError = ValidatorUtils.validWeight(weightInput.getText().toString().trim());
        weightLayout.setError(weightError);

        setsError = ValidatorUtils.validSets(setsInput.getText().toString().trim());
        setsLayout.setError(setsError);

        repsError = ValidatorUtils.validReps(repsInput.getText().toString().trim());
        repsLayout.setError(repsError);

        detailsError = ValidatorUtils.validDetails(detailsInput.getText().toString().trim());
        detailsLayout.setError(detailsError);

        if (!urlInput.getText().toString().isEmpty()) {
            // only validate the url if user has inputted something
            urlError = ValidatorUtils.validUrl(urlInput.getText().toString().trim());
        }
        urlLayout.setError(urlError);

        if (selectedFocuses.isEmpty()) {
            focusError = true;
            focusRelativeLayout.startAnimation(AndroidUtils.shakeError(4));
            Toast.makeText(getContext(), "Must select at least one focus", Toast.LENGTH_LONG).show();
        }

        if (nameError == null && weightError == null && setsError == null &&
                repsError == null && detailsError == null && urlError == null && !focusError) {
            double weight = Double.parseDouble(weightInput.getText().toString().trim());
            if (metricUnits) {
                weight = WeightUtils.metricWeightToImperial(weight);
            }

            String exerciseName = exerciseNameInput.getText().toString().trim();
            int sets = Integer.parseInt(setsInput.getText().toString().trim());
            int reps = Integer.parseInt(repsInput.getText().toString().trim());
            String details = detailsInput.getText().toString().trim();
            String videoURL = urlInput.getText().toString().trim();

            // no errors, so go ahead and save
            AndroidUtils.showLoadingDialog(loadingDialog, "Creating exercise...");
            Executor executor = Executors.newSingleThreadExecutor();
            double finalWeight = weight; // java weirdness
            executor.execute(() -> {
                Result<OwnedExercise> result = this.userManager.newExercise(exerciseName, selectedFocuses, finalWeight, sets, reps, details, videoURL);
                Handler handler = new Handler(getMainLooper());
                handler.post(() -> {
                    loadingDialog.dismiss();
                    if (result.isSuccess()) {
                        ((MainActivity) getActivity()).finishFragment();
                    } else {
                        AndroidUtils.showErrorDialog(result.getErrorMessage(), getContext());
                    }
                });
            });
        }
    }
}
