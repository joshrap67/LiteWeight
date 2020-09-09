package com.joshrap.liteweight.adapters;

import android.content.Context;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.helpers.ExerciseHelper;
import com.joshrap.liteweight.helpers.WeightHelper;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.models.ExerciseRoutine;
import com.joshrap.liteweight.models.ExerciseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RoutineAdapter extends
        RecyclerView.Adapter<RoutineAdapter.ViewHolder> {

    class ViewHolder extends RecyclerView.ViewHolder {

        CheckBox exerciseCheckbox;
        Button weightButton;
        ImageButton saveButton;
        ImageButton cancelButton;
        ImageButton videoButton;
        LinearLayout extraInfo;

        EditText detailsInput;
        EditText weightInput;
        EditText setsInput;
        EditText repsInput;

        TextInputLayout weightInputLayout;
        TextInputLayout setsInputLayout;
        TextInputLayout repsInputLayout;
        TextInputLayout detailsInputLayout;

        ViewHolder(View itemView) {
            super(itemView);

            exerciseCheckbox = itemView.findViewById(R.id.exercise_name);
            weightButton = itemView.findViewById(R.id.weight_btn);
            extraInfo = itemView.findViewById(R.id.extra_info_layout);
            saveButton = itemView.findViewById(R.id.save_button);
            videoButton = itemView.findViewById(R.id.launch_video);
            cancelButton = itemView.findViewById(R.id.cancel_button);

            weightInput = itemView.findViewById(R.id.weight_input);
            detailsInput = itemView.findViewById(R.id.details_input);
            setsInput = itemView.findViewById(R.id.sets_input);
            repsInput = itemView.findViewById(R.id.reps_input);

            weightInputLayout = itemView.findViewById(R.id.weight_input_layout);
            setsInputLayout = itemView.findViewById(R.id.sets_input_layout);
            repsInputLayout = itemView.findViewById(R.id.reps_input_layout);
            detailsInputLayout = itemView.findViewById(R.id.details_input_layout);
        }
    }

    private List<ExerciseRoutine> exercises;
    private Map<String, ExerciseUser> exerciseUserMap;
    private Context context;
    private boolean metricUnits;
    private boolean videosEnabled;
    private List<String> extrasShownMap;

    // Pass in the contact array into the constructor
    public RoutineAdapter(List<ExerciseRoutine> exerciseRoutines, Map<String, ExerciseUser> exerciseIdToName, Context context, boolean metricUnits, boolean videosEnabled) {
        this.exercises = exerciseRoutines;
        this.exerciseUserMap = exerciseIdToName;
        this.context = context;
        this.metricUnits = metricUnits;
        this.videosEnabled = videosEnabled;
        this.extrasShownMap = new ArrayList<>();
    }


    @Override
    public RoutineAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View exerciseView = inflater.inflate(R.layout.row_exercise_active_workout, parent, false);

        // Return a new holder instance
        return new ViewHolder(exerciseView);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(RoutineAdapter.ViewHolder holder, int position) {
        // Get the data model based on position
        ExerciseRoutine exercise = exercises.get(position);

        final String currentExercise = this.exerciseUserMap.get(exercise.getExerciseId()).getExerciseName();
        final CheckBox exerciseCheckbox = holder.exerciseCheckbox;
        exerciseCheckbox.setText(currentExercise);
        exerciseCheckbox.setChecked(exercise.isCompleted());
        exerciseCheckbox.setOnClickListener(v -> {
            if (exerciseCheckbox.isChecked()) {
                exercise.setCompleted(true);
            } else {
                exercise.setCompleted(false);
            }
        });

        final Button weightButton = holder.weightButton;
        final EditText weightInput = holder.weightInput;
        final EditText detailsInput = holder.detailsInput;
        final EditText repsInput = holder.repsInput;
        final EditText setsInput = holder.setsInput;

        final TextInputLayout detailsInputLayout = holder.detailsInputLayout;
        final TextInputLayout setsInputLayout = holder.setsInputLayout;
        final TextInputLayout repsInputLayout = holder.repsInputLayout;
        final TextInputLayout weightInputLayout = holder.weightInputLayout;

        final LinearLayout extraInfo = holder.extraInfo;
        final ImageButton saveButton = holder.saveButton;
        final ImageButton cancelButton = holder.cancelButton;
        final ImageButton videoButton = holder.videoButton;
        videoButton.setVisibility((videosEnabled) ? View.VISIBLE : View.GONE);
        if (extrasShownMap.contains(exercise.getExerciseId())) {
            // since closing another row builds entire list again, need to make sure button stays hidden if supposed to be
            videoButton.setVisibility(View.GONE);
        }

        weightInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_WEIGHT_DIGITS)});
        setsInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_SETS_DIGITS)});
        repsInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_REPS_DIGITS)});
        detailsInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_DETAILS_LENGTH)});

        double weight = WeightHelper.getConvertedWeight(metricUnits, exercise.getWeight());
        String formattedWeight = WeightHelper.getFormattedWeightWithUnits(weight, metricUnits);
        weightButton.setText(formattedWeight);
        weightInputLayout.setHint("Weight (" + (metricUnits ? "kg)" : "lb)"));

        setsInput.setText(Integer.toString(exercise.getSets()));
        repsInput.setText(Integer.toString(exercise.getReps()));
        detailsInput.setText(exercise.getDetails());

        weightButton.setOnClickListener((v) -> {
            // show all the extra details for this exercise
            extrasShownMap.add(exercise.getExerciseId());
            weightInput.setText(WeightHelper.getFormattedWeight(metricUnits, exercise.getWeight()));
            weightButton.setVisibility(View.INVISIBLE);
            videoButton.setVisibility(View.GONE);
            extraInfo.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.VISIBLE);
        });

        View.OnClickListener collapseExtraInfo = v -> {
            // hide the extra details
            extrasShownMap.remove(exercise.getExerciseId());
            weightButton.setVisibility(View.VISIBLE);
            extraInfo.setVisibility(View.GONE);
            saveButton.setVisibility(View.GONE);
            cancelButton.setVisibility(View.GONE);
            videoButton.setVisibility((videosEnabled) ? View.VISIBLE : View.GONE);
            // remove any errors
            weightInputLayout.setError(null);
            setsInputLayout.setError(null);
            repsInput.setError(null);
            detailsInput.setError(null);
            // ensure the text in each field is the same
            setsInput.setText(Integer.toString(exercise.getSets()));
            repsInput.setText(Integer.toString(exercise.getReps()));
            detailsInput.setText(exercise.getDetails());
            weightInput.setText(WeightHelper.getFormattedWeight(metricUnits, exercise.getWeight()));

            notifyDataSetChanged(); // avoids animation on closing the extra info
        };
        saveButton.setOnClickListener(view -> {
            // first check if input on all fields is valid
            boolean validInput = inputValid(weightInput, detailsInput, setsInput, repsInput,
                    weightInputLayout, detailsInputLayout, setsInputLayout, repsInputLayout);

            if (validInput) {
                double newWeight = Double.parseDouble(weightInput.getText().toString());
                if (metricUnits) {
                    // convert if in metric
                    newWeight = WeightHelper.metricWeightToImperial(newWeight);
                }

                exercise.setWeight(newWeight);
                weightButton.setText(WeightHelper.getFormattedWeightWithUnits(newWeight, metricUnits));

                extrasShownMap.remove(exercise.getExerciseId());
                exercise.setDetails(detailsInput.getText().toString().trim());
                exercise.setReps(Integer.valueOf(repsInput.getText().toString().trim()));
                exercise.setSets(Integer.valueOf(setsInput.getText().toString().trim()));
                // remove any errors
                weightInputLayout.setError(null);
                setsInputLayout.setError(null);
                repsInputLayout.setError(null);
                detailsInput.setError(null);
                // hide extra layout
                weightButton.setVisibility(View.VISIBLE);
                extraInfo.setVisibility(View.GONE);
                saveButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
                notifyDataSetChanged(); // avoids animation on closing the extra info

            }
        });
        cancelButton.setOnClickListener(collapseExtraInfo);
        videoButton.setOnClickListener(v ->
                ExerciseHelper.launchVideo(this.exerciseUserMap.get(exercise.getExerciseId()).getVideoUrl(), context));
    }

    private boolean inputValid(EditText weightInput, EditText detailsInput,
                               EditText setsInput, EditText repsInput, TextInputLayout weightLayout,
                               TextInputLayout detailsLayout, TextInputLayout setsLayout, TextInputLayout repsLayout) {
        boolean valid = true;
        if (weightInput.getText().toString().trim().isEmpty()) {
            valid = false;
            weightLayout.setError("Weight cannot be empty.");
        } else if (weightInput.getText().toString().trim().length() > Variables.MAX_WEIGHT_DIGITS) {
            weightLayout.setError("Weight is too large.");
            valid = false;
        }

        if (setsInput.getText().toString().trim().isEmpty() ||
                setsInput.getText().toString().length() > Variables.MAX_SETS_DIGITS) {
            setsLayout.setError("Invalid");
            valid = false;
        }

        if (repsInput.getText().toString().trim().isEmpty() ||
                repsInput.getText().toString().length() > Variables.MAX_REPS_DIGITS) {
            repsLayout.setError("Invalid");
            valid = false;
        }

        if (detailsInput.getText().toString().length() > Variables.MAX_DETAILS_LENGTH) {
            detailsLayout.setError("Too many characters.");
            valid = false;
        }

        return valid;
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }
}