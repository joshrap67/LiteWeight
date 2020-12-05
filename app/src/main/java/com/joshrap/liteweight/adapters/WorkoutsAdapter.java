package com.joshrap.liteweight.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.joshrap.liteweight.models.WorkoutMeta;

import java.util.List;

public class WorkoutsAdapter extends ArrayAdapter<WorkoutMeta> {
    private Context context;
    private List<WorkoutMeta> workoutList;

    public WorkoutsAdapter(@NonNull Context context, List<WorkoutMeta> list) {
        super(context, 0, list);
        this.context = context;
        this.workoutList = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_activated_1, parent, false);
        }
        WorkoutMeta currentExercise = workoutList.get(position);

        TextView workoutNameTV = listItem.findViewById(android.R.id.text1);
        workoutNameTV.setText(currentExercise.getWorkoutName());
        workoutNameTV.setTextSize(17);

        return workoutNameTV;
    }
}
