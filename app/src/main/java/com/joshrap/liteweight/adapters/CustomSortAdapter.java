package com.joshrap.liteweight.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.joshrap.liteweight.R;
import com.joshrap.liteweight.helpers.WeightHelper;
import com.joshrap.liteweight.models.RoutineExercise;

import java.util.List;
import java.util.Map;

public class CustomSortAdapter extends
        RecyclerView.Adapter<CustomSortAdapter.ViewHolder> {
    class ViewHolder extends RecyclerView.ViewHolder {
        TextView exerciseTV;
        Button weightButton;

        ViewHolder(View itemView) {
            super(itemView);
            exerciseTV = itemView.findViewById(R.id.exercise_name);
            weightButton = itemView.findViewById(R.id.weight_btn);
        }
    }

    private List<RoutineExercise> exercises;
    private Map<String, String> exerciseIdToName;
    private boolean metricUnits;

    public CustomSortAdapter(List<RoutineExercise> routineExercises, Map<String,
            String> exerciseIdToName, boolean metricUnits) {
        this.exercises = routineExercises;
        this.exerciseIdToName = exerciseIdToName;
        this.metricUnits = metricUnits;
    }


    @Override
    public CustomSortAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View exerciseView = inflater.inflate(R.layout.row_exercise_sorting, parent, false);
        return new ViewHolder(exerciseView);
    }

    @Override
    public void onBindViewHolder(CustomSortAdapter.ViewHolder holder, int position) {
        RoutineExercise exercise = exercises.get(position);

        final String currentExercise = this.exerciseIdToName.get(exercise.getExerciseId());
        final TextView exerciseTV = holder.exerciseTV;
        exerciseTV.setText(currentExercise);

        final Button weightButton = holder.weightButton;
        double weight = WeightHelper.getConvertedWeight(metricUnits, exercise.getWeight());
        String formattedWeight = WeightHelper.getFormattedWeightWithUnits(weight, metricUnits);
        weightButton.setText(formattedWeight);
        weightButton.setEnabled(false);
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }
}