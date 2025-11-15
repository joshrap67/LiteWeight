package com.joshrap.liteweight.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import com.google.android.material.textfield.TextInputLayout;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.models.receivedWorkout.ReceivedExercise;
import com.joshrap.liteweight.utils.WeightUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReceivedRoutineAdapter extends RecyclerView.Adapter<ReceivedRoutineAdapter.ViewHolder> {
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final CheckBox exerciseName; // checkbox just to make layout consistent
        final Button expandButton;
        final RelativeLayout extraInfoContainer;
        final RelativeLayout rootLayout;

        final EditText instructionsInput;
        final EditText weightInput;
        final EditText setsInput;
        final EditText repsInput;

        final TextInputLayout weightInputLayout;

        ViewHolder(View itemView) {
            super(itemView);

            rootLayout = itemView.findViewById(R.id.root_layout);

            exerciseName = itemView.findViewById(R.id.exercise_checkbox);
            expandButton = itemView.findViewById(R.id.expand_btn);
            extraInfoContainer = itemView.findViewById(R.id.extra_info_container);

            weightInput = itemView.findViewById(R.id.weight_input);
            instructionsInput = itemView.findViewById(R.id.instructions_input);
            setsInput = itemView.findViewById(R.id.sets_input);
            repsInput = itemView.findViewById(R.id.reps_input);

            weightInputLayout = itemView.findViewById(R.id.weight_input_layout);
        }
    }

    private final List<ReceivedExercise> receivedExercises;
    private final boolean metricUnits;
    private final Map<ReceivedExercise, Boolean> expandedExercises;

    public ReceivedRoutineAdapter(List<ReceivedExercise> receivedExercises, boolean metricUnits) {
        this.receivedExercises = receivedExercises;
        this.metricUnits = metricUnits;
        this.expandedExercises = new HashMap<>();
    }

    @NonNull
    @Override
    public ReceivedRoutineAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View exerciseView = inflater.inflate(R.layout.row_exercise_read_only, parent, false);
        return new ViewHolder(exerciseView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, List<Object> payloads) {
        // this overload is needed since if you rebind with the intention to only collapse, the layout is overridden causing weird animation bugs
        if (!payloads.isEmpty()) {
            final ReceivedExercise exercise = receivedExercises.get(position);
            boolean isExpanded = Boolean.TRUE.equals(expandedExercises.get(exercise));

            if (isExpanded) {
                setExpandedViews(holder, exercise);
            } else {
                setCollapsedViews(holder, exercise);
            }
        } else {
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    @Override
    public void onBindViewHolder(ReceivedRoutineAdapter.ViewHolder holder, int position) {
        final ReceivedExercise exercise = receivedExercises.get(position);

        final String currentExercise = exercise.getExerciseName();
        final TextView exerciseName = holder.exerciseName;
        exerciseName.setText(currentExercise);

        final Button expandButton = holder.expandButton;
        final EditText weightInput = holder.weightInput;
        final EditText instructionsInput = holder.instructionsInput;
        final EditText repsInput = holder.repsInput;
        final EditText setsInput = holder.setsInput;

        weightInput.setEnabled(false);
        setsInput.setEnabled(false);
        repsInput.setEnabled(false);
        instructionsInput.setEnabled(false);

        if (Boolean.TRUE.equals(expandedExercises.get(exercise))) {
            setExpandedViews(holder, exercise);
        } else {
            setCollapsedViews(holder, exercise);
        }

        expandButton.setOnClickListener((v) -> {
            ReceivedExercise receivedExercise = receivedExercises.get(holder.getAdapterPosition());
            boolean newExpandedVal = !Boolean.TRUE.equals(expandedExercises.get(receivedExercise));
            expandedExercises.put(receivedExercise, newExpandedVal);

            if (newExpandedVal) {
                // wait for recycler view to stop animating before changing the visibility (only for expand since otherwise wierd flicker is shown)
                AutoTransition autoTransition = new AutoTransition();
                autoTransition.setDuration(100);
                TransitionManager.beginDelayedTransition(holder.rootLayout, autoTransition);
            }

            notifyItemChanged(holder.getAdapterPosition(), true);
        });
    }

    private void setInputs(ReceivedRoutineAdapter.ViewHolder holder, ReceivedExercise exercise) {
        double weight = WeightUtils.getConvertedWeight(metricUnits, exercise.getWeight());
        holder.weightInputLayout.setHint("Weight (" + (metricUnits ? "kg)" : "lb)"));

        holder.weightInput.setText(WeightUtils.getFormattedWeightForEditText(weight));
        holder.setsInput.setText(String.format(Locale.getDefault(), Integer.toString(exercise.getSets())));
        holder.repsInput.setText(String.format(Locale.getDefault(), Integer.toString(exercise.getReps())));
        holder.instructionsInput.setText(exercise.getInstructions());
    }

    private void setExpandedViews(ReceivedRoutineAdapter.ViewHolder holder, ReceivedExercise exercise) {
        holder.extraInfoContainer.setVisibility(View.VISIBLE);
        holder.expandButton.setText(R.string.done_all_caps);
        holder.expandButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.up_arrow_small, 0);
        setInputs(holder, exercise);
    }

    private void setCollapsedViews(ReceivedRoutineAdapter.ViewHolder holder, ReceivedExercise exercise) {
        holder.extraInfoContainer.setVisibility(View.GONE);

        double weight = WeightUtils.getConvertedWeight(metricUnits, exercise.getWeight());
        String formattedWeight = WeightUtils.getFormattedWeightWithUnits(weight, metricUnits);
        holder.expandButton.setText(formattedWeight);
        holder.expandButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.down_arrow_small, 0);
    }

    @Override
    public int getItemCount() {
        return receivedExercises.size();
    }
}