package com.joshrap.liteweight.adapters;

import android.app.Activity;
import android.content.Context;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import com.google.android.material.textfield.TextInputLayout;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.activities.MainActivity;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.models.workout.Routine;
import com.joshrap.liteweight.models.workout.RoutineExercise;
import com.joshrap.liteweight.utils.AndroidUtils;
import com.joshrap.liteweight.utils.WeightUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lombok.Data;

public class RoutineDayAdapter extends RecyclerView.Adapter<RoutineDayAdapter.ViewHolder> {

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView exerciseTV;
        final Button expandButton;
        final ImageButton deleteButton;
        final RelativeLayout extraInfoContainer;
        final RelativeLayout rootLayout;

        final EditText detailsInput;
        final EditText weightInput;
        final EditText setsInput;
        final EditText repsInput;

        final TextInputLayout weightInputLayout;
        final TextInputLayout setsInputLayout;
        final TextInputLayout repsInputLayout;
        final TextInputLayout detailsInputLayout;

        ViewHolder(View itemView) {
            super(itemView);

            rootLayout = itemView.findViewById(R.id.root_layout);

            deleteButton = itemView.findViewById(R.id.delete_exercise_icon_btn);
            exerciseTV = itemView.findViewById(R.id.exercise_name_tv);
            expandButton = itemView.findViewById(R.id.expand_btn);
            extraInfoContainer = itemView.findViewById(R.id.extra_info_container);

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
    public final List<RoutineRowModel> routineRowModels;
    private final Activity activity;
    private final boolean metricUnits;

    public RoutineDayAdapter(Map<String, String> exerciseIdToName, Map<String, Double> exerciseIdToCurrentMaxWeight,
                             Routine routine, int currentWeek, int currentDay, boolean metricUnits, Activity activity) {
        this.exerciseIdToName = exerciseIdToName;
        this.exerciseIdToCurrentMaxWeight = exerciseIdToCurrentMaxWeight;
        this.pendingRoutine = routine;
        this.currentWeek = currentWeek;
        this.currentDay = currentDay;
        this.metricUnits = metricUnits;
        this.activity = activity;

        List<RoutineRowModel> routineRowModels = new ArrayList<>();
        for (RoutineExercise exercise : routine.exerciseListForDay(currentWeek, currentDay)) {
            RoutineRowModel exerciseRowModel = new RoutineRowModel(exercise, false);
            routineRowModels.add(exerciseRowModel);
        }
        this.routineRowModels = routineRowModels;
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
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, List<Object> payloads) {
        // this overload is needed since if you rebind with the intention to only collapse, the layout is overridden causing weird animation bugs
        if (!payloads.isEmpty()) {
            final RoutineRowModel routineRowModel = routineRowModels.get(position);
            final RoutineExercise exercise = routineRowModel.routineExercise;
            boolean isExpanded = routineRowModel.isExpanded;

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
    public void onBindViewHolder(RoutineDayAdapter.ViewHolder holder, int position) {
        final RoutineRowModel rowModel = routineRowModels.get(position);
        final RoutineExercise exercise = rowModel.routineExercise;
        boolean isExpanded = rowModel.isExpanded;

        final String currentExercise = this.exerciseIdToName.get(exercise.getExerciseId());
        TextView exerciseTV = holder.exerciseTV;
        exerciseTV.setText(currentExercise);

        Button expandButton = holder.expandButton;
        EditText weightInput = holder.weightInput;
        EditText detailsInput = holder.detailsInput;
        EditText repsInput = holder.repsInput;
        EditText setsInput = holder.setsInput;

        TextInputLayout detailsInputLayout = holder.detailsInputLayout;
        TextInputLayout setsInputLayout = holder.setsInputLayout;
        TextInputLayout repsInputLayout = holder.repsInputLayout;
        TextInputLayout weightInputLayout = holder.weightInputLayout;

        ImageButton deleteButton = holder.deleteButton;

        weightInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_WEIGHT_DIGITS)});
        setsInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_SETS_DIGITS)});
        repsInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_REPS_DIGITS)});
        detailsInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_DETAILS_LENGTH)});

        weightInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(weightInputLayout));
        setsInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(setsInputLayout));
        repsInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(repsInputLayout));
        detailsInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(detailsInputLayout));

        if (isExpanded) {
            setExpandedViews(holder, exercise);
        } else {
            setCollapsedViews(holder, exercise);
        }

        deleteButton.setOnClickListener(v -> {
            ((MainActivity) activity).hideKeyboard();
            pendingRoutine.removeExercise(currentWeek, currentDay, exercise.getExerciseId());
            routineRowModels.remove(rowModel);
            notifyItemRemoved(holder.getAdapterPosition());
            notifyItemRangeChanged(holder.getAdapterPosition(), getItemCount(), true); // payload avoids flicker for items below removed one
        });

        expandButton.setOnClickListener((v) -> {
            ((MainActivity) activity).hideKeyboard();

            if (rowModel.isExpanded) {
                boolean validInput = inputValid(weightInput, detailsInput, setsInput, repsInput,
                        weightInputLayout, detailsInputLayout, setsInputLayout, repsInputLayout);

                if (validInput) {
                    double newWeight = Double.parseDouble(weightInput.getText().toString());
                    if (metricUnits) {
                        // convert back to imperial if in metric since weight is stored in imperial on backend
                        newWeight = WeightUtils.metricWeightToImperial(newWeight);
                    }

                    exercise.setWeight(newWeight);
                    exercise.setDetails(detailsInput.getText().toString().trim());
                    exercise.setReps(Integer.parseInt(repsInput.getText().toString().trim()));
                    exercise.setSets(Integer.parseInt(setsInput.getText().toString().trim()));
                    if (exerciseIdToCurrentMaxWeight.containsKey(exercise.getExerciseId()) && exerciseIdToCurrentMaxWeight.get(exercise.getExerciseId()) < newWeight) {
                        // shortcut used for first workout being created - prevents user from constantly having to change from 0lb
                        exerciseIdToCurrentMaxWeight.put(exercise.getExerciseId(), newWeight);
                    }

                    rowModel.isExpanded = false;

                    notifyItemChanged(holder.getAdapterPosition(), true);
                    ((MainActivity) activity).hideKeyboard();
                }

            } else {
                // show all the extra details for this exercise so the user can edit/read them
                rowModel.isExpanded = true;

                // wait for recycler view to stop animating before changing the visibility
                AutoTransition autoTransition = new AutoTransition();
                autoTransition.setDuration(100);
                TransitionManager.beginDelayedTransition(holder.rootLayout, autoTransition);

                notifyItemChanged(holder.getAdapterPosition(), true);
            }
        });
    }

    private void setExpandedViews(RoutineDayAdapter.ViewHolder holder, RoutineExercise exercise) {
        holder.extraInfoContainer.setVisibility(View.VISIBLE);
        holder.expandButton.setText(R.string.done_all_caps);
        holder.expandButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.up_arrow_small, 0);

        setInputs(holder, exercise);
    }

    private void setCollapsedViews(RoutineDayAdapter.ViewHolder holder, RoutineExercise exercise) {
        holder.extraInfoContainer.setVisibility(View.GONE);

        double weight = WeightUtils.getConvertedWeight(metricUnits, exercise.getWeight());
        String formattedWeight = WeightUtils.getFormattedWeightWithUnits(weight, metricUnits);
        holder.expandButton.setText(formattedWeight);
        holder.expandButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.down_arrow_small, 0);
    }

    private void setInputs(ViewHolder holder, RoutineExercise exercise) {
        double weight = WeightUtils.getConvertedWeight(metricUnits, exercise.getWeight());
        holder.weightInputLayout.setHint("Weight (" + (metricUnits ? "kg)" : "lb)"));

        holder.weightInput.setText(WeightUtils.getFormattedWeightForEditText(weight));
        holder.setsInput.setText(String.format(Locale.getDefault(), Integer.toString(exercise.getSets())));
        holder.repsInput.setText(String.format(Locale.getDefault(), Integer.toString(exercise.getReps())));
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
        return routineRowModels.size();
    }

    // separate class that wraps the routine exercise and holds data about the state of the row in the recycler view
    @Data
    public static class RoutineRowModel {
        private final RoutineExercise routineExercise;
        private boolean isExpanded;

        public RoutineRowModel(RoutineExercise routineExercise, boolean isExpanded) {
            this.routineExercise = routineExercise;
            this.isExpanded = isExpanded;
        }
    }
}