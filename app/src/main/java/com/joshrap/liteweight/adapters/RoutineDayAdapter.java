package com.joshrap.liteweight.adapters;

import android.animation.LayoutTransition;
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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.models.Routine;
import com.joshrap.liteweight.models.RoutineExercise;
import com.joshrap.liteweight.utils.WeightUtils;

import java.util.List;
import java.util.Map;

public class RoutineDayAdapter extends RecyclerView.Adapter<RoutineDayAdapter.ViewHolder> {

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView exerciseTV;
        Button weightButton;
        ImageButton deleteButton;
        ImageButton saveButton;
        LinearLayout extraInfo;

        EditText detailsInput;
        EditText weightInput;
        EditText setsInput;
        EditText repsInput;

        TextInputLayout weightInputLayout;
        TextInputLayout setsInputLayout;
        TextInputLayout repsInputLayout;
        TextInputLayout detailsInputLayout;
        LinearLayout rootLayout;

        ViewHolder(View itemView) {
            super(itemView);
            rootLayout = itemView.findViewById(R.id.root_layout);

            deleteButton = itemView.findViewById(R.id.delete_exercise);
            exerciseTV = itemView.findViewById(R.id.exercise_name);
            weightButton = itemView.findViewById(R.id.weight_btn);
            extraInfo = itemView.findViewById(R.id.extra_info_layout);
            saveButton = itemView.findViewById(R.id.save_button);

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

    private final Map<String, String> exerciseIdToName;
    private final Map<String, Double> exerciseIdToCurrentMaxWeight;
    private final Routine pendingRoutine;
    private final int currentWeek;
    private final int currentDay;
    private final RecyclerView recyclerView;
    private final boolean metricUnits;

    public RoutineDayAdapter(Map<String, String> exerciseIdToName, Map<String, Double> exerciseIdToCurrentMaxWeight,
                             Routine routine, int currentWeek, int currentDay, boolean metricUnits,
                             RecyclerView recyclerView) {
        this.exerciseIdToName = exerciseIdToName;
        this.exerciseIdToCurrentMaxWeight = exerciseIdToCurrentMaxWeight;
        this.pendingRoutine = routine;
        this.currentWeek = currentWeek;
        this.currentDay = currentDay;
        this.metricUnits = metricUnits;
        this.recyclerView = recyclerView;
    }

    @NonNull
    @Override
    public RoutineDayAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View exerciseView = inflater.inflate(R.layout.row_exercise_pending, parent, false);
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

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, List<Object> payloads) {
        if (!payloads.isEmpty()) {
            // this exercise has been updated, clear errors, set values, and animate back to single row
            final RoutineExercise exercise = Exercises().get(position);

            // remove any errors
            holder.weightInputLayout.setError(null);
            holder.setsInputLayout.setError(null);
            holder.repsInputLayout.setError(null);
            holder.detailsInput.setError(null);
            // hide extra layout
            holder.weightButton.setVisibility(View.VISIBLE);
            holder.extraInfo.setVisibility(View.GONE);
            holder.saveButton.setVisibility(View.GONE);
            holder.exerciseTV.setVisibility(View.VISIBLE);

            setInputs(holder, exercise);
        } else {
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    private List<RoutineExercise> Exercises() {
        return pendingRoutine.getExerciseListForDay(currentWeek, currentDay);
    }

    @Override
    public void onBindViewHolder(RoutineDayAdapter.ViewHolder holder, int position) {
        final RoutineExercise exercise = Exercises().get(position);

        LinearLayout rootLayout = holder.rootLayout;
        LayoutTransition layoutTransition = rootLayout.getLayoutTransition();
        layoutTransition.addTransitionListener(new LayoutTransition.TransitionListener() {
            @Override
            public void endTransition(LayoutTransition transition, ViewGroup container, View view, int transitionType) {
                RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(view.getContext()) {
                    @Override
                    protected int getVerticalSnapPreference() {
                        return LinearSmoothScroller.SNAP_TO_START;
                    }
                };

                if (transitionType == LayoutTransition.CHANGE_APPEARING &&
                        holder.itemView.getY() > recyclerView.getHeight() * .60) {
                    // start to scroll down if the view being expanded is a certain amount of distance from the top of the recycler view
                    smoothScroller.setTargetPosition(holder.getLayoutPosition());
                    recyclerView.getLayoutManager().startSmoothScroll(smoothScroller);
                }
            }

            @Override
            public void startTransition(LayoutTransition transition, ViewGroup container, View view, int transitionType) {
            }
        });

        final String currentExercise = this.exerciseIdToName.get(exercise.getExerciseId());
        TextView exerciseTV = holder.exerciseTV;
        exerciseTV.setText(currentExercise);

        Button weightButton = holder.weightButton;
        EditText weightInput = holder.weightInput;
        EditText detailsInput = holder.detailsInput;
        EditText repsInput = holder.repsInput;
        EditText setsInput = holder.setsInput;

        TextInputLayout detailsInputLayout = holder.detailsInputLayout;
        TextInputLayout setsInputLayout = holder.setsInputLayout;
        TextInputLayout repsInputLayout = holder.repsInputLayout;
        TextInputLayout weightInputLayout = holder.weightInputLayout;

        ImageButton deleteButton = holder.deleteButton;
        LinearLayout extraInfo = holder.extraInfo;
        ImageButton saveButton = holder.saveButton;

        weightInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_WEIGHT_DIGITS)});
        setsInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_SETS_DIGITS)});
        repsInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_REPS_DIGITS)});
        detailsInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_DETAILS_LENGTH)});

        setInputs(holder, exercise);

        weightButton.setOnClickListener((v) -> {
            // show all the extra details for this exercise
            weightButton.setVisibility(View.INVISIBLE);
            extraInfo.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.VISIBLE);
        });


        deleteButton.setOnClickListener(v -> {
            pendingRoutine.removeExercise(currentWeek, currentDay, exercise.getExerciseId());
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, getItemCount());
        });

        saveButton.setOnClickListener(view -> {
            // first check if input on all fields is valid
            boolean validInput = inputValid(weightInput, detailsInput, setsInput, repsInput,
                    weightInputLayout, detailsInputLayout, setsInputLayout, repsInputLayout);

            if (validInput) {
                double newWeight = Double.parseDouble(weightInput.getText().toString());
                if (metricUnits) {
                    // convert if in metric
                    newWeight = WeightUtils.metricWeightToImperial(newWeight);
                }

                exercise.setWeight(newWeight);
                exercise.setDetails(detailsInput.getText().toString().trim());
                exercise.setReps(Integer.valueOf(repsInput.getText().toString().trim()));
                exercise.setSets(Integer.valueOf(setsInput.getText().toString().trim()));
                if (exerciseIdToCurrentMaxWeight.containsKey(exercise.getExerciseId()) && exerciseIdToCurrentMaxWeight.get(exercise.getExerciseId()) < newWeight) {
                    // shortcut used for first workout being created - prevents user from constantly having to change from 0lb
                    exerciseIdToCurrentMaxWeight.put(exercise.getExerciseId(), newWeight);
                }

                notifyItemChanged(position, true);
            }
        });
    }

    private void setInputs(ViewHolder holder, RoutineExercise exercise) {
        double weight = WeightUtils.getConvertedWeight(metricUnits, exercise.getWeight());
        String formattedWeight = WeightUtils.getFormattedWeightWithUnits(weight, metricUnits);
        holder.weightButton.setText(formattedWeight);
        holder.weightInputLayout.setHint("Weight (" + (metricUnits ? "kg)" : "lb)"));

        holder.weightInput.setText(WeightUtils.getFormattedWeightForEditText(weight));
        holder.setsInput.setText(Integer.toString(exercise.getSets()));
        holder.repsInput.setText(Integer.toString(exercise.getReps()));
        holder.detailsInput.setText(exercise.getDetails());
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
        return Exercises().size();
    }
}