package com.joshrap.liteweight.adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.joshrap.liteweight.models.OwnedExercise;

import java.util.List;

public class ExerciseAdapter extends ArrayAdapter<OwnedExercise> {
    private final Context context;
    private final List<OwnedExercise> exerciseList;

    public ExerciseAdapter(@NonNull Context context, List<OwnedExercise> list) {
        super(context, 0, list);
        this.context = context;
        this.exerciseList = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
        }
        OwnedExercise currentExercise = exerciseList.get(position);

        TextView exerciseNameTV = listItem.findViewById(android.R.id.text1);
        exerciseNameTV.setText(currentExercise.getExerciseName());

        return exerciseNameTV;
    }
}
