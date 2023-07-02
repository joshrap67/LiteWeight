package com.joshrap.liteweight.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.joshrap.liteweight.R;
import com.joshrap.liteweight.interfaces.DraggableViewHolder;
import com.joshrap.liteweight.models.workout.RoutineExercise;
import com.joshrap.liteweight.utils.WeightUtils;

import java.util.List;
import java.util.Map;

public class CustomSortAdapter extends RecyclerView.Adapter<CustomSortAdapter.ViewHolder> {
    static class ViewHolder extends RecyclerView.ViewHolder implements DraggableViewHolder {
        final TextView exerciseTV;
        final Button weightButton;
        final RelativeLayout rootLayout;

        ViewHolder(View itemView) {
            super(itemView);
            exerciseTV = itemView.findViewById(R.id.exercise_name_tv);
            weightButton = itemView.findViewById(R.id.weight_btn);
            rootLayout = itemView.findViewById(R.id.root_layout);
        }

        @Override
        public void onItemSelected() {
            rootLayout.setBackgroundResource(R.drawable.exercise_row_sorting_selected_background);
        }

        @Override
        public void onItemCleared() {
            rootLayout.setBackgroundResource(R.drawable.exercise_row_sorting_background);
        }
    }

    private final List<RoutineExercise> exercises;
    private final Map<String, String> exerciseIdToName;
    private final boolean metricUnits;

    public CustomSortAdapter(List<RoutineExercise> routineExercises, Map<String, String> exerciseIdToName, boolean metricUnits) {
        this.exercises = routineExercises;
        this.exerciseIdToName = exerciseIdToName;
        this.metricUnits = metricUnits;
    }


    @NonNull
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
        double weight = WeightUtils.getConvertedWeight(metricUnits, exercise.getWeight());
        String formattedWeight = WeightUtils.getFormattedWeightWithUnits(weight, metricUnits);
        weightButton.setText(formattedWeight);
        weightButton.setEnabled(false);
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }
}