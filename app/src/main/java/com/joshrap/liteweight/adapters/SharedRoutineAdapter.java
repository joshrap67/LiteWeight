package com.joshrap.liteweight.adapters;

import android.animation.LayoutTransition;
import android.content.Context;
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
import com.joshrap.liteweight.helpers.WeightHelper;
import com.joshrap.liteweight.models.SentExercise;

import java.util.List;

public class SharedRoutineAdapter extends
        RecyclerView.Adapter<SharedRoutineAdapter.ViewHolder> {

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView exerciseName;
        Button weightButton;
        ImageButton doneButton;
        LinearLayout extraInfo;
        LinearLayout rootLayout;

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
            weightButton = itemView.findViewById(R.id.weight_btn);
            extraInfo = itemView.findViewById(R.id.extra_info_layout);
            doneButton = itemView.findViewById(R.id.save_button);

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

    private List<SentExercise> exercises;
    private boolean metricUnits;
    private RecyclerView recyclerView;
    private Context context;

    public SharedRoutineAdapter(List<SentExercise> routineExercises, boolean metricUnits, RecyclerView recyclerView,
            Context context) {
        this.exercises = routineExercises;
        this.metricUnits = metricUnits;
        this.recyclerView = recyclerView;
        this.context = context;
    }


    @Override
    public SharedRoutineAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View exerciseView = inflater.inflate(R.layout.row_exercise_read_only, parent, false);
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
    public void onBindViewHolder(SharedRoutineAdapter.ViewHolder holder, int position) {
        final SentExercise exercise = exercises.get(position);

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

        final String currentExercise = exercise.getExerciseName();
        final TextView exerciseName = holder.exerciseName;
        exerciseName.setText(currentExercise);

        final Button weightButton = holder.weightButton;
        final EditText weightInput = holder.weightInput;
        final EditText detailsInput = holder.detailsInput;
        final EditText repsInput = holder.repsInput;
        final EditText setsInput = holder.setsInput;

        final TextInputLayout weightInputLayout = holder.weightInputLayout;

        final LinearLayout extraInfo = holder.extraInfo;
        final ImageButton doneButton = holder.doneButton;

        weightInput.setEnabled(false);
        setsInput.setEnabled(false);
        repsInput.setEnabled(false);
        detailsInput.setEnabled(false);

        double weight = WeightHelper.getConvertedWeight(metricUnits, exercise.getWeight());
        String formattedWeight = WeightHelper.getFormattedWeightWithUnits(weight, metricUnits);
        weightButton.setText(formattedWeight);
        weightInputLayout.setHint("Weight (" + (metricUnits ? "kg)" : "lb)"));

        setsInput.setText(Integer.toString(exercise.getSets()));
        repsInput.setText(Integer.toString(exercise.getReps()));
        detailsInput.setText(exercise.getDetails());

        weightButton.setOnClickListener((v) -> {
            // show all the extra details for this exercise
            weightInput.setText(WeightHelper.getFormattedWeightForEditText(WeightHelper.getConvertedWeight(metricUnits, exercise.getWeight())));
            weightButton.setVisibility(View.INVISIBLE);
            extraInfo.setVisibility(View.VISIBLE);
            doneButton.setVisibility(View.VISIBLE);
        });
        doneButton.setOnClickListener(v -> {
            // hide the extra details
            weightButton.setVisibility(View.VISIBLE);
            extraInfo.setVisibility(View.GONE);
            doneButton.setVisibility(View.GONE);
            notifyDataSetChanged(); // avoids animation on closing the extra info
        });
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }
}