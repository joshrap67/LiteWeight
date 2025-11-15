package com.joshrap.liteweight.adapters;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
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
import com.joshrap.liteweight.models.user.OwnedExercise;
import com.joshrap.liteweight.models.workout.Routine;
import com.joshrap.liteweight.models.workout.RoutineExercise;
import com.joshrap.liteweight.utils.AndroidUtils;
import com.joshrap.liteweight.utils.WeightUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RoutineDayAdapter extends RecyclerView.Adapter<RoutineDayAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView exerciseTV;
        final Button expandButton;
        final ImageButton deleteButton;
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

            deleteButton = itemView.findViewById(R.id.delete_exercise_icon_btn);
            exerciseTV = itemView.findViewById(R.id.exercise_name_tv);
            expandButton = itemView.findViewById(R.id.expand_btn);
            extraInfoContainer = itemView.findViewById(R.id.extra_info_container);

            weightInput = itemView.findViewById(R.id.weight_input);
            instructionsInput = itemView.findViewById(R.id.instructions_input);
            setsInput = itemView.findViewById(R.id.sets_input);
            repsInput = itemView.findViewById(R.id.reps_input);

            weightInputLayout = itemView.findViewById(R.id.weight_input_layout);
        }
    }

    private final Map<String, String> exerciseIdToName;
    private final Map<String, Double> exerciseIdToCurrentMaxWeight;
    private final Routine pendingRoutine;
    private final int currentWeek;
    private final int currentDay;
    private final List<RoutineExercise> exercises;
    private final Activity activity;
    private final boolean metricUnits;
    private final Map<String, Boolean> expandedExercises;


    public RoutineDayAdapter(Map<String, String> exerciseIdToName, Map<String, Double> exerciseIdToCurrentMaxWeight,
                             Routine routine, int currentWeek, int currentDay, boolean metricUnits, Activity activity) {
        this.exerciseIdToName = exerciseIdToName;
        this.exerciseIdToCurrentMaxWeight = exerciseIdToCurrentMaxWeight;
        this.pendingRoutine = routine;
        this.currentWeek = currentWeek;
        this.currentDay = currentDay;
        this.metricUnits = metricUnits;
        this.activity = activity;
        this.expandedExercises = new HashMap<>();
        this.exercises = routine.exerciseListForDay(currentWeek, currentDay);
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
            final RoutineExercise exercise = exercises.get(position);
            boolean isExpanded = Boolean.TRUE.equals(expandedExercises.get(exercise.getExerciseId()));

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
        final RoutineExercise exercise = exercises.get(position);

        final String currentExercise = this.exerciseIdToName.get(exercise.getExerciseId());
        TextView exerciseTV = holder.exerciseTV;
        exerciseTV.setText(currentExercise);

        Button expandButton = holder.expandButton;
        EditText weightInput = holder.weightInput;
        EditText instructionsInput = holder.instructionsInput;
        EditText repsInput = holder.repsInput;
        EditText setsInput = holder.setsInput;

        ImageButton deleteButton = holder.deleteButton;

        weightInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_WEIGHT_DIGITS)});
        setsInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_SETS_DIGITS)});
        repsInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_REPS_DIGITS)});
        instructionsInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_INSTRUCTIONS_LENGTH)});

        AndroidUtils.setSetsTextWatcher(setsInput, exercise);
        AndroidUtils.setRepsTextWatcher(repsInput, exercise);
        AndroidUtils.setInstructionsTextWatcher(instructionsInput, exercise);

        if (weightInput.getTag() instanceof TextWatcher) {
            // clear any existing watcher
            weightInput.removeTextChangedListener((TextWatcher) weightInput.getTag());
        }

        TextWatcher weightWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String weight = weightInput.getText().toString().trim();
                if (!weight.isEmpty() && weight.length() <= Variables.MAX_WEIGHT_DIGITS) {
                    double newWeight = Double.parseDouble(weight);
                    if (metricUnits) {
                        // convert back to imperial if in metric since weight is stored in imperial on backend
                        newWeight = WeightUtils.metricWeightToImperial(newWeight);
                    }

                    // breaking pattern of using shared util method due to this shortcut
                    if (exerciseIdToCurrentMaxWeight.containsKey(exercise.getExerciseId()) && exerciseIdToCurrentMaxWeight.get(exercise.getExerciseId()) < newWeight) {
                        // shortcut used for first workout being created - prevents user from constantly having to change from 0lb
                        exerciseIdToCurrentMaxWeight.put(exercise.getExerciseId(), newWeight);
                    }

                    exercise.setWeight(newWeight);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        };
        weightInput.addTextChangedListener(weightWatcher);
        weightInput.setTag(weightWatcher);

        if (Boolean.TRUE.equals(expandedExercises.get(exercise.getExerciseId()))) {
            setExpandedViews(holder, exercise);
        } else {
            setCollapsedViews(holder, exercise);
        }

        deleteButton.setOnClickListener(v -> {
            RoutineExercise _exercise = getExercise(holder.getAdapterPosition());
            ((MainActivity) activity).hideKeyboard();
            pendingRoutine.removeExercise(currentWeek, currentDay, _exercise.getExerciseId());
            exercises.remove(_exercise);
            notifyItemRemoved(holder.getAdapterPosition());
            notifyItemRangeChanged(holder.getAdapterPosition(), getItemCount(), true); // payload avoids flicker for items below removed one
        });

        expandButton.setOnClickListener((v) -> {
            RoutineExercise _exercise = getExercise(holder.getAdapterPosition());
            ((MainActivity) activity).hideKeyboard();

            if (Boolean.TRUE.equals(expandedExercises.get(_exercise.getExerciseId()))) {
                expandedExercises.put(_exercise.getExerciseId(), false);

                notifyItemChanged(holder.getAdapterPosition(), true);
                ((MainActivity) activity).hideKeyboard();

            } else {
                // show all the extra details for this exercise so the user can edit/read them
                expandedExercises.put(_exercise.getExerciseId(), true);

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
        holder.instructionsInput.setText(exercise.getInstructions());
    }

    private RoutineExercise getExercise(int position) {
        return exercises.get(position);
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    public void addExercise(RoutineExercise exercise) {
        this.exercises.add(exercise);
    }

    public void removeExercise(OwnedExercise exercise) {
        this.exercises.removeIf(x -> x.getExerciseId().equals(exercise.getId()));
    }
}