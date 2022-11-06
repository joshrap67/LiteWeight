package com.joshrap.liteweight.adapters;

import android.animation.LayoutTransition;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.activities.WorkoutActivity;
import com.joshrap.liteweight.fragments.CurrentWorkoutFragment;
import com.joshrap.liteweight.models.RoutineExercise;
import com.joshrap.liteweight.utils.WeightUtils;
import com.joshrap.liteweight.models.SharedExercise;

import java.util.List;

public class SharedRoutineAdapter extends RecyclerView.Adapter<SharedRoutineAdapter.ViewHolder> {
    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox exerciseName; // checkbox just to make layout easier
        Button expandButton;
        RelativeLayout extraInfo;
        RelativeLayout rootLayout;

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
            rootLayout = itemView.findViewById(R.id.root_layout);

            exerciseName = itemView.findViewById(R.id.exercise_name);
            expandButton = itemView.findViewById(R.id.expand_btn);
            extraInfo = itemView.findViewById(R.id.extra_info_layout);

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

    private final List<SharedRoutineRowModel> sharedRoutineRowModels;
    private final boolean metricUnits;
    private final RecyclerView recyclerView;
    private final Context context;

    public SharedRoutineAdapter(List<SharedRoutineRowModel> sharedRoutineRowModels, boolean metricUnits, RecyclerView recyclerView,
                                Context context) {
        this.sharedRoutineRowModels = sharedRoutineRowModels;
        this.metricUnits = metricUnits;
        this.recyclerView = recyclerView;
        this.context = context;
    }


    @NonNull
    @Override
    public SharedRoutineAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View exerciseView = inflater.inflate(R.layout.row_exercise_read_only, parent, false);
        return new ViewHolder(exerciseView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, List<Object> payloads) {
        // this overload is needed since if you rebind with the intention to only collapse, the linear layout is overridden causing weird animation bugs
        if (!payloads.isEmpty()) {
            final SharedRoutineRowModel routineRowModel = sharedRoutineRowModels.get(position);
            final SharedExercise exercise = routineRowModel.sharedExercise;
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
    public void onBindViewHolder(SharedRoutineAdapter.ViewHolder holder, int position) {
        final SharedRoutineRowModel rowModel = sharedRoutineRowModels.get(position);
        final SharedExercise exercise = rowModel.sharedExercise;
        boolean isExpanded = rowModel.isExpanded;

        RelativeLayout rootLayout = holder.rootLayout;
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

        final String currentExercise = exercise.getExerciseName();
        final TextView exerciseName = holder.exerciseName;
        exerciseName.setText(currentExercise);

        final Button expandButton = holder.expandButton;
        final EditText weightInput = holder.weightInput;
        final EditText detailsInput = holder.detailsInput;
        final EditText repsInput = holder.repsInput;
        final EditText setsInput = holder.setsInput;

        weightInput.setEnabled(false);
        setsInput.setEnabled(false);
        repsInput.setEnabled(false);
        detailsInput.setEnabled(false);

        if (isExpanded) {
            setExpandedViews(holder, exercise);
        } else {
            setCollapsedViews(holder, exercise);
        }

        expandButton.setOnClickListener((v) -> {
            rowModel.isExpanded = !rowModel.isExpanded;
            notifyItemChanged(position, true);
            setExpandedViews(holder, exercise);
        });
    }

    private void setInputs(SharedRoutineAdapter.ViewHolder holder, SharedExercise exercise) {
        double weight = WeightUtils.getConvertedWeight(metricUnits, exercise.getWeight());
        holder.weightInputLayout.setHint("Weight (" + (metricUnits ? "kg)" : "lb)"));

        holder.weightInput.setText(WeightUtils.getFormattedWeightForEditText(weight));
        holder.setsInput.setText(Integer.toString(exercise.getSets()));
        holder.repsInput.setText(Integer.toString(exercise.getReps()));
        holder.detailsInput.setText(exercise.getDetails());

    }

    private void setExpandedViews(SharedRoutineAdapter.ViewHolder holder, SharedExercise exercise) {
        holder.extraInfo.setVisibility(View.VISIBLE);
        holder.expandButton.setText(R.string.done_all_caps);
        holder.expandButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.small_up_arrow, 0);
        setInputs(holder, exercise);
    }

    private void setCollapsedViews(SharedRoutineAdapter.ViewHolder holder, SharedExercise exercise) {
        holder.weightInputLayout.setError(null);
        holder.setsInputLayout.setError(null);
        holder.repsInputLayout.setError(null);
        holder.detailsInput.setError(null);

        // hide the extra layout
        holder.extraInfo.setVisibility(View.GONE);

        double weight = WeightUtils.getConvertedWeight(metricUnits, exercise.getWeight());
        String formattedWeight = WeightUtils.getFormattedWeightWithUnits(weight, metricUnits);
        holder.expandButton.setText(formattedWeight);
        holder.expandButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.small_down_arrow, 0);
    }

    @Override
    public int getItemCount() {
        return sharedRoutineRowModels.size();
    }

    // separate class that wraps the shared exercise and holds data about the state of the row in the recycler view
    public static class SharedRoutineRowModel {
        private final SharedExercise sharedExercise;
        private boolean isExpanded;

        public SharedRoutineRowModel(SharedExercise sharedExercise, boolean isExpanded) {
            this.sharedExercise = sharedExercise;
            this.isExpanded = isExpanded;
        }
    }
}