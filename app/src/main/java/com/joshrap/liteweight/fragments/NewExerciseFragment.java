package com.joshrap.liteweight.fragments;

import static android.os.Looper.getMainLooper;

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

import androidx.activity.OnBackPressedCallback;
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
import com.joshrap.liteweight.interfaces.LinkCallbacks;
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

import javax.inject.Inject;

public class NewExerciseFragment extends Fragment implements FragmentWithDialog {

    private TextInputLayout exerciseNameLayout, weightLayout, setsLayout, repsLayout, notesLayout;
    private EditText exerciseNameInput, weightInput, setsInput, repsInput, notesInput;
    private boolean metricUnits, exerciseCreated;
    private List<String> focusList, selectedFocuses;
    private List<Link> links;
    private int focusRotationAngle;
    private RelativeLayout focusRelativeLayout, linksLayout;
    private TextView focusCountTV;
    private AlertDialog alertDialog;
    private final MutableLiveData<String> focusTitle = new MutableLiveData<>();
    private final List<String> existingExerciseNames = new ArrayList<>();
    private SaveExerciseLinkAdapter linksAdapter;
    private OnBackPressedCallback backPressedCallback;

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
        ((MainActivity) activity).updateToolbarTitle(Variables.NEW_EXERCISE_TITLE);

        User user = currentUserModule.getUser();
        for (OwnedExercise exercise : user.getExercises()) {
            existingExerciseNames.add(exercise.getName());
        }
        metricUnits = user.getSettings().isMetricUnits();
        focusList = new ArrayList<>(Variables.FOCUS_LIST);
        selectedFocuses = new ArrayList<>();
        links = new ArrayList<>();

        return inflater.inflate(R.layout.fragment_new_exercise, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FragmentActivity activity = requireActivity();

        exerciseCreated = false;

        focusCountTV = view.findViewById(R.id.focus_count_text_view);
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
        exerciseNameInput.setText("");

        weightInput = view.findViewById(R.id.default_weight_input);
        weightInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_WEIGHT_DIGITS)});
        weightInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(weightLayout));
        weightInput.setText(String.format(Locale.getDefault(), Integer.toString(Variables.DEFAULT_WEIGHT)));

        setsInput = view.findViewById(R.id.default_sets_input);
        setsInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_SETS_DIGITS)});
        setsInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(setsLayout));
        setsInput.setText(String.format(Locale.getDefault(), Integer.toString(Variables.DEFAULT_SETS)));

        repsInput = view.findViewById(R.id.default_reps_input);
        repsInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_REPS_DIGITS)});
        repsInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(repsLayout));
        repsInput.setText(String.format(Locale.getDefault(), Integer.toString(Variables.DEFAULT_REPS)));

        notesInput = view.findViewById(R.id.notes_input);
        notesInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_NOTES_LENGTH)});
        notesInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(notesLayout));
        notesInput.setText("");


        Button saveButton = view.findViewById(R.id.save_btn);
        saveButton.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).hideKeyboard();
            createExercise();
        });

        RecyclerView focusRecyclerView = view.findViewById(R.id.pick_focuses_recycler_view);
        FocusAdapter addFocusAdapter = new FocusAdapter(focusList, selectedFocuses, focusTitle);
        focusRecyclerView.setAdapter(addFocusAdapter);
        focusRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        ImageButton focusRowIcon = view.findViewById(R.id.focus_icon_btn);
        focusRelativeLayout = view.findViewById(R.id.focus_container);

        View.OnClickListener focusLayoutClicked = v -> {
            ((MainActivity) requireActivity()).hideKeyboard();
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
        linksAdapter = new SaveExerciseLinkAdapter(links, new LinkCallbacks() {
            @Override
            public void onClear(Link link, int index) {
                links.remove(index);
                linksAdapter.notifyItemRemoved(index);
                linksAdapter.notifyItemRangeChanged(index, links.size());
            }

            @Override
            public void onClick(Link link, int index) {
                promptEditLink(link, index);
            }
        });

        linksRecyclerView.setAdapter(linksAdapter);
        linksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        Button addLinkBtn = view.findViewById(R.id.add_link_btn);
        addLinkBtn.setOnClickListener(x -> promptAddLink());

        backPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                boolean isModified = !exerciseNameInput.getText().toString().isEmpty()
                        || !weightInput.getText().toString().equals(Integer.toString(Variables.DEFAULT_WEIGHT))
                        || !setsInput.getText().toString().equals(Integer.toString(Variables.DEFAULT_SETS))
                        || !repsInput.getText().toString().equals(Integer.toString(Variables.DEFAULT_REPS))
                        || !notesInput.getText().toString().isEmpty()
                        || !selectedFocuses.isEmpty()
                        || !links.isEmpty();
                if (isModified && !exerciseCreated) {
                    hideAllDialogs(); // since user could spam back button and cause multiple ones to show
                    alertDialog = new AlertDialog.Builder(requireContext())
                            .setTitle("Unsaved Changes")
                            .setMessage(R.string.unsaved_changes_msg)
                            .setPositiveButton("Leave", (dialog, which) -> {
                                remove();
                                activity.getOnBackPressedDispatcher().onBackPressed();
                            })
                            .setNegativeButton("Stay", null)
                            .create();
                    alertDialog.show();
                } else {
                    remove();
                    activity.getOnBackPressedDispatcher().onBackPressed();
                }
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        if (backPressedCallback != null) {
            addBackPressedCallback();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        backPressedCallback.remove();
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

    private void addBackPressedCallback() {
        requireActivity().getOnBackPressedDispatcher().addCallback(backPressedCallback);
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
                    links.add(link);
                    linksAdapter.notifyItemInserted(links.size() - 1);
                    alertDialog.dismiss();
                }
            });
        });
        alertDialog.show();
    }

    private void promptEditLink(Link link, int index) {
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

                if (urlMsg == null && labelMsg == null) {
                    link.setUrl(newUrl);
                    link.setLabel(newLabel);
                    alertDialog.dismiss();
                    linksAdapter.notifyItemChanged(index);
                }
            });
        });
        alertDialog.show();
    }

    private void createExercise() {
        String nameError, weightError, setsError, repsError, notesError;
        boolean focusError = false;
        boolean urlError = false;

        nameError = ValidatorUtils.validNewExerciseName(exerciseNameInput.getText().toString().trim(), existingExerciseNames);
        exerciseNameLayout.setError(nameError);

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

        if (links.size() > Variables.MAX_LINKS) {
            urlError = true;
            linksLayout.startAnimation(AndroidUtils.shakeError(4));
            Toast.makeText(getContext(), String.format("Cannot have more than %s links.", Variables.MAX_LINKS), Toast.LENGTH_LONG).show();
        }

        if (nameError == null && weightError == null && setsError == null &&
                repsError == null && notesError == null && !urlError && !focusError) {
            double weight = Double.parseDouble(weightInput.getText().toString().trim());
            if (metricUnits) {
                weight = WeightUtils.metricWeightToImperial(weight);
            }

            String exerciseName = exerciseNameInput.getText().toString().trim();
            int sets = Integer.parseInt(setsInput.getText().toString().trim());
            int reps = Integer.parseInt(repsInput.getText().toString().trim());
            String notes = notesInput.getText().toString().trim();

            // no errors, so go ahead and save
            AndroidUtils.showLoadingDialog(loadingDialog, "Creating exercise...");
            Executor executor = Executors.newSingleThreadExecutor();
            double finalWeight = weight; // java weirdness
            executor.execute(() -> {
                Result<OwnedExercise> result = this.selfManager.newExercise(exerciseName, selectedFocuses, finalWeight, sets, reps, notes, links);
                Handler handler = new Handler(getMainLooper());
                handler.post(() -> {
                    loadingDialog.dismiss();
                    if (result.isSuccess()) {
                        exerciseCreated = true;
                        ((MainActivity) requireActivity()).finishFragment();
                    } else {
                        AndroidUtils.showErrorDialog(result.getErrorMessage(), getContext());
                    }
                });
            });
        }
    }
}
