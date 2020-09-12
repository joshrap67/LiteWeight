package com.joshrap.liteweight.adapters;

import android.content.Context;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.helpers.WeightHelper;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.models.ExerciseRoutine;
import com.joshrap.liteweight.models.Routine;

import java.util.List;
import java.util.Map;

public class PendingRoutineAdapter extends
        RecyclerView.Adapter<PendingRoutineAdapter.ViewHolder> {

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    class ViewHolder extends RecyclerView.ViewHolder {
        TextView exerciseTV;
        Button weightButton;
        ImageButton deleteButton;
        ImageButton saveButton;
        ImageButton cancelButton;
        LinearLayout extraInfo;

        EditText detailsInput;
        EditText weightInput;
        EditText setsInput;
        EditText repsInput;

        TextInputLayout weightInputLayout;
        TextInputLayout setsInputLayout;
        TextInputLayout repsInputLayout;
        TextInputLayout detailsInputLayout;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            deleteButton = itemView.findViewById(R.id.delete_exercise);
            exerciseTV = itemView.findViewById(R.id.exercise_name);
            weightButton = itemView.findViewById(R.id.weight_btn);
            extraInfo = itemView.findViewById(R.id.extra_info_layout);
            saveButton = itemView.findViewById(R.id.save_button);
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
    private Map<String, String> exerciseIdToName;
    private Routine pendingRoutine;
    private int currentWeek;
    private int currentDay;
    private int mode;
    private boolean metricUnits;

    // Pass in the contact array into the constructor
    public PendingRoutineAdapter(List<ExerciseRoutine> exerciseRoutines, Map<String,
            String> exerciseIdToName, Routine routine, int currentWeek, int currentDay, boolean metricUnits, int mode) {
        this.exercises = exerciseRoutines;
        this.exerciseIdToName = exerciseIdToName;
        this.pendingRoutine = routine;
        this.currentWeek = currentWeek;
        this.currentDay = currentDay;
        this.metricUnits = metricUnits;
        this.mode = mode;
    }


    @Override
    public PendingRoutineAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View exerciseView = inflater.inflate(R.layout.row_exercise_pending, parent, false);

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
    public void onBindViewHolder(PendingRoutineAdapter.ViewHolder holder, int position) {
        // Get the data model based on position
        ExerciseRoutine exercise = exercises.get(position);

        final String currentExercise = this.exerciseIdToName.get(exercise.getExerciseId());
        final TextView exerciseTV = holder.exerciseTV;
        exerciseTV.setText(currentExercise);

        final Button weightButton = holder.weightButton;
        final EditText weightInput = holder.weightInput;
        final EditText detailsInput = holder.detailsInput;
        final EditText repsInput = holder.repsInput;
        final EditText setsInput = holder.setsInput;

        final TextInputLayout detailsInputLayout = holder.detailsInputLayout;
        final TextInputLayout setsInputLayout = holder.setsInputLayout;
        final TextInputLayout repsInputLayout = holder.repsInputLayout;
        final TextInputLayout weightInputLayout = holder.weightInputLayout;

        final ImageButton deleteButton = holder.deleteButton;
        deleteButton.setVisibility((mode == Variables.DELETE_MODE) ? View.VISIBLE : View.GONE);

        final LinearLayout extraInfo = holder.extraInfo;
        final ImageButton saveButton = holder.saveButton;
        final ImageButton cancelButton = holder.cancelButton;

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
            deleteButton.setVisibility(View.GONE);
            weightInput.setText(WeightHelper.getFormattedWeightForInput(WeightHelper.getConvertedWeight(metricUnits, exercise.getWeight())));
            weightButton.setVisibility(View.INVISIBLE);
            extraInfo.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.VISIBLE);
        });

        View.OnClickListener collapseExtraInfo = v -> {
            // hide the extra details
            weightButton.setVisibility(View.VISIBLE);
            extraInfo.setVisibility(View.GONE);
            saveButton.setVisibility(View.GONE);
            cancelButton.setVisibility(View.GONE);
            deleteButton.setVisibility((mode == Variables.DELETE_MODE) ? View.VISIBLE : View.GONE);
            // remove any errors
            weightInputLayout.setError(null);
            setsInputLayout.setError(null);
            repsInput.setError(null);
            detailsInput.setError(null);
            // ensure the text in each field is the same
            setsInput.setText(Integer.toString(exercise.getSets()));
            repsInput.setText(Integer.toString(exercise.getReps()));
            detailsInput.setText(exercise.getDetails());
            weightInput.setText(WeightHelper.getFormattedWeightForInput(WeightHelper.getConvertedWeight(metricUnits, exercise.getWeight())));

            notifyDataSetChanged(); // avoids animation on closing the extra info
        };
        deleteButton.setOnClickListener(v -> {
            pendingRoutine.removeExercise(currentWeek, currentDay, exercise.getExerciseId());
            exercises.remove(exercise);
            notifyDataSetChanged();
        });
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
                deleteButton.setVisibility((mode == Variables.DELETE_MODE) ? View.VISIBLE : View.GONE);
                exerciseTV.setVisibility(View.VISIBLE);
                notifyDataSetChanged(); // avoids animation on closing the extra info

            }
        });
        cancelButton.setOnClickListener(collapseExtraInfo);
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

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return exercises.size();
    }
}