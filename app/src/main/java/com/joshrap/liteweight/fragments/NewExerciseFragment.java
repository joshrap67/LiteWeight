package com.joshrap.liteweight.fragments;

import static android.content.Context.CLIPBOARD_SERVICE;
import static android.os.Looper.getMainLooper;

import android.app.ProgressDialog;
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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import com.google.android.material.textfield.TextInputLayout;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.activities.WorkoutActivity;
import com.joshrap.liteweight.adapters.FocusAdapter;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;
import com.joshrap.liteweight.models.OwnedExercise;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.User;
import com.joshrap.liteweight.models.UserWithWorkout;
import com.joshrap.liteweight.network.repos.UserRepository;
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
    private int rotationAngle;
    private RelativeLayout focusRelativeLayout;
    private TextView focusCountTV;
    private final MutableLiveData<String> focusTitle = new MutableLiveData<>();

    @Inject
    ProgressDialog loadingDialog;
    @Inject
    UserRepository userRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        Injector.getInjector(getContext()).inject(this);
        ((WorkoutActivity) getActivity()).toggleBackButton(true);
        ((WorkoutActivity) getActivity()).updateToolbarTitle(Variables.NEW_EXERCISE_TITLE);

        clipboard = (ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE);

        UserWithWorkout userWithWorkout = ((WorkoutActivity) getActivity()).getUserWithWorkout();
        user = userWithWorkout.getUser();
        metricUnits = user.getUserPreferences().isMetricUnits();
        focusList = Variables.FOCUS_LIST;
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
        weightInput.setText("0");

        setsInput = view.findViewById(R.id.default_sets_input);
        setsInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_SETS_DIGITS)});
        setsInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(setsLayout));
        setsInput.setText("3");

        repsInput = view.findViewById(R.id.default_reps_input);
        repsInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_REPS_DIGITS)});
        repsInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(repsLayout));
        repsInput.setText("15");

        detailsInput = view.findViewById(R.id.default_details_input);
        detailsInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_DETAILS_LENGTH)});
        detailsInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(detailsLayout));
        detailsInput.setText("");

        urlInput = view.findViewById(R.id.url_input);
        urlInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_URL_LENGTH)});
        urlInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(urlLayout));
        urlInput.setText("");

        Button clipboardBtn = view.findViewById(R.id.clipboard_btn);
        Button previewBtn = view.findViewById(R.id.preview_btn);
        previewBtn.setOnClickListener(v -> ExerciseUtils.launchVideo(urlInput.getText().toString().trim(), getContext()));
        clipboardBtn.setOnClickListener(v -> {
            clipboard.setPrimaryClip(new ClipData(ClipData.newPlainText("url", urlInput.getText().toString().trim())));
            Toast.makeText(getContext(), "Link copied to clipboard.", Toast.LENGTH_SHORT).show();
        });

        clipboardBtn.setVisibility(View.GONE);
        previewBtn.setVisibility(View.GONE);

        urlInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (v.hasFocus()) {
                clipboardBtn.setVisibility(View.GONE);
                previewBtn.setVisibility(View.GONE);
            } else {
                if (urlInput.getText().length() > 0) {
                    clipboardBtn.setVisibility(View.VISIBLE);
                    previewBtn.setVisibility(View.VISIBLE);
                }
            }
        });

        Button saveButton = view.findViewById(R.id.save_btn);
        saveButton.setOnClickListener(v -> createExercise());


        RecyclerView focusRecyclerView = view.findViewById(R.id.pick_focuses_recycler_view);
        FocusAdapter addFocusAdapter = new FocusAdapter(focusList, selectedFocuses, focusTitle);
        focusRecyclerView.setAdapter(addFocusAdapter);
        focusRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        ImageButton focusRowIcon = view.findViewById(R.id.focus_image_btn);
        focusRelativeLayout = view.findViewById(R.id.focus_container);

        View.OnClickListener focusLayoutClicked = v -> {
            boolean visible = focusRecyclerView.getVisibility() == View.VISIBLE;
            focusRecyclerView.setVisibility(visible ? View.GONE : View.VISIBLE);
            rotationAngle = rotationAngle == 0 ? 180 : 0;
            focusRowIcon.animate().rotation(rotationAngle).setDuration(400).start();
            if (visible) {
                // provide smooth animation when closing
                TransitionManager.beginDelayedTransition((ViewGroup) view.getParent(), new AutoTransition());
            }
        };
        focusRelativeLayout.setOnClickListener(focusLayoutClicked);
        focusRowIcon.setOnClickListener(focusLayoutClicked);
    }

    @Override
    public void onPause() {
        hideAllDialogs();
        super.onPause();
    }

    @Override
    public void hideAllDialogs() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
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
        for (String exerciseId : user.getOwnedExercises().keySet()) {
            exerciseNames.add(user.getOwnedExercises().get(exerciseId).getExerciseName());
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
                ResultStatus<OwnedExercise> resultStatus = this.userRepository.newExercise(exerciseName, selectedFocuses, finalWeight, sets, reps, details, videoURL);
                Handler handler = new Handler(getMainLooper());
                handler.post(() -> {
                    loadingDialog.dismiss();
                    if (resultStatus.isSuccess()) {
                        OwnedExercise newExercise = resultStatus.getData();
                        user.getOwnedExercises().put(newExercise.getExerciseId(), newExercise);
                        ((WorkoutActivity) getActivity()).finishFragment();
                    } else {
                        AndroidUtils.showErrorDialog("Create Exercise Error", resultStatus.getErrorMessage(), getContext());
                    }
                });
            });
        }
    }
}
