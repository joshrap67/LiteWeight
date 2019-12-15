package com.joshrap.liteweight;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.joshrap.liteweight.Database.Entities.ExerciseEntity;

import java.util.ArrayList;
import java.util.List;

public class ExerciseAdapter extends ArrayAdapter<ExerciseEntity> {
    private Context mContext;
    private List<ExerciseEntity> exerciseList;

    public ExerciseAdapter(@NonNull Context context, ArrayList<ExerciseEntity> list) {
        super(context, 0, list);
        mContext = context;
        exerciseList = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_1, parent, false);
        }
        ExerciseEntity currentExercise = exerciseList.get(position);

        TextView release = listItem.findViewById(android.R.id.text1);
        release.setText(currentExercise.getExerciseName());

        return release;
    }
}
