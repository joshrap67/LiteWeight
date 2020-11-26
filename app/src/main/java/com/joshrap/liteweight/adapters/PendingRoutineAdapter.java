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

import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.utils.WeightUtils;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.models.RoutineExercise;
import com.joshrap.liteweight.models.Routine;

import java.util.List;
import java.util.Map;

public class PendingRoutineAdapter extends
        RecyclerView.Adapter<PendingRoutineAdapter.ViewHolder> {

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
        LinearLayout rootLayout;

        ViewHolder(View itemView) {
            super(itemView);
            rootLayout = itemView.findViewById(R.id.root_layout);

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

    private List<RoutineExercise> exercises;
    private Map<String, String> exerciseIdToName;
    private Routine pendingRoutine;
    private int currentWeek;
    private int currentDay;
    private int mode;
    private RecyclerView recyclerView;
    private Context context;
    private boolean metricUnits;

    public PendingRoutineAdapter(List<RoutineExercise> routineExercises, Map<String,
            String> exerciseIdToName, Routine routine, int currentWeek, int currentDay, boolean metricUnits, int mode,
                                 RecyclerView recyclerView, Context context) {
        this.exercises = routineExercises;
        this.exerciseIdToName = exerciseIdToName;
        this.pendingRoutine = routine;
        this.currentWeek = currentWeek;
        this.currentDay = currentDay;
        this.metricUnits = metricUnits;
        this.mode = mode;
        this.recyclerView = recyclerView;
        this.context = context;
    }

    @Override
    public PendingRoutineAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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
    public void onBindViewHolder(PendingRoutineAdapter.ViewHolder holder, int position) {
        final RoutineExercise exercise = exercises.get(position);

        LinearLayout rootLayout = holder.rootLayout;
        LayoutTransition layoutTransition = rootLayout.getLayoutTransition();
        layoutTransition.addTransitionListener(new LayoutTransition.TransitionListener() {
            @Override
            public void endTransition(LayoutTransition transition, ViewGroup container, View view, int transitionType) {
                RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(context) {
                    @Override
                    protected int getVerticalSnapPreference() {
                        return LinearSmoothScroller.SNAP_TO_START;
                    }
                };

                if (transitionType == LayoutTransition.APPEARING &&
                        holder.itemView.getY() > recyclerView.getHeight() * .60) {
                    // start to scroll down if the view being expanded is a certain amount of distance from the top of the recycler view
                    smoothScroller.setTargetPosition(holder.getLayoutPosition());
                    recyclerView.getLayoutManager().startSmoothScroll(smoothScroller);
                    // if i don't have this notify then it sometimes has an empty space above the container... i hate android
                    notifyDataSetChanged();
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
        deleteButton.setVisibility((mode == Variables.DELETE_MODE) ? View.VISIBLE : View.GONE);

        LinearLayout extraInfo = holder.extraInfo;
        ImageButton saveButton = holder.saveButton;
        ImageButton cancelButton = holder.cancelButton;

        weightInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_WEIGHT_DIGITS)});
        setsInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_SETS_DIGITS)});
        repsInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_REPS_DIGITS)});
        detailsInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_DETAILS_LENGTH)});

        double weight = WeightUtils.getConvertedWeight(metricUnits, exercise.getWeight());
        String formattedWeight = WeightUtils.getFormattedWeightWithUnits(weight, metricUnits);
        weightButton.setText(formattedWeight);
        weightInputLayout.setHint("Weight (" + (metricUnits ? "kg)" : "lb)"));

        setsInput.setText(Integer.toString(exercise.getSets()));
        repsInput.setText(Integer.toString(exercise.getReps()));
        detailsInput.setText(exercise.getDetails());

        weightButton.setOnClickListener((v) -> {
            // show all the extra details for this exercise
            deleteButton.setVisibility(View.GONE);
            weightInput.setText(WeightUtils.getFormattedWeightForEditText(WeightUtils.getConvertedWeight(metricUnits, exercise.getWeight())));
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
            weightInput.setText(WeightUtils.getFormattedWeightForEditText(WeightUtils.getConvertedWeight(metricUnits, exercise.getWeight())));

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
                    newWeight = WeightUtils.metricWeightToImperial(newWeight);
                }

                exercise.setWeight(newWeight);
                weightButton.setText(WeightUtils.getFormattedWeightWithUnits(newWeight, metricUnits));

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

    @Override
    public int getItemCount() {
        return exercises.size();
    }
}