package com.joshrap.liteweight.adapters;

import android.content.Context;
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
import com.joshrap.liteweight.models.SentExercise;

import java.util.List;

public class SentRoutineAdapter extends
        RecyclerView.Adapter<SentRoutineAdapter.ViewHolder> {

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView exerciseName;
        Button weightButton;
        ImageButton doneButton;
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

    // Pass in the contact array into the constructor
    public SentRoutineAdapter(List<SentExercise> routineExercises, boolean metricUnits) {
        this.exercises = routineExercises;
        this.metricUnits = metricUnits;
    }


    @Override
    public SentRoutineAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(SentRoutineAdapter.ViewHolder holder, int position) {
        final SentExercise exercise = exercises.get(position);

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
            weightInput.setText(WeightHelper.getFormattedWeightForInput(WeightHelper.getConvertedWeight(metricUnits, exercise.getWeight())));
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