package com.joshrap.liteweight.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.models.ExerciseRoutine;

import java.util.List;

public class RoutineAdapter extends
        RecyclerView.Adapter<RoutineAdapter.ViewHolder> {

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    class ViewHolder extends RecyclerView.ViewHolder {

        CheckBox exerciseTV;
        Button weightButton;
        EditText weightInput;
        TextInputLayout weightLayout;
        LinearLayout extraInfo;
        ImageButton videoButton;
        ImageButton saveButton;
        ImageButton cancelButton;
        EditText notesInput;
        Switch ignoreWeightSwitch;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            exerciseTV = itemView.findViewById(R.id.exercise_name);
            weightButton = itemView.findViewById(R.id.weight_btn);
            weightInput = itemView.findViewById(R.id.weight_view);
            weightLayout = itemView.findViewById(R.id.weight_input_layout);
            extraInfo = itemView.findViewById(R.id.extra_info_layout);
            videoButton = itemView.findViewById(R.id.launch_video);
            saveButton = itemView.findViewById(R.id.save_button);
            cancelButton = itemView.findViewById(R.id.cancel_button);
            notesInput = itemView.findViewById(R.id.notes_input);
            ignoreWeightSwitch = itemView.findViewById(R.id.ignore_weight_switch);
        }
    }

    private List<ExerciseRoutine> exercises;

    // Pass in the contact array into the constructor
    public RoutineAdapter(List<ExerciseRoutine> contacts) {
        this.exercises = contacts;
    }


    @Override
    public RoutineAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View exerciseView = inflater.inflate(R.layout.row_exercise_new, parent, false);

        // Return a new holder instance
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
    public void onBindViewHolder(RoutineAdapter.ViewHolder holder, int position) {
        // Get the data model based on position
        ExerciseRoutine exercise = exercises.get(position);

        final String currentExercise = exercise.getExerciseName();
        final CheckBox exerciseTV = holder.exerciseTV;
        exerciseTV.setText(currentExercise);

        final Button weightButton = holder.weightButton;
        final EditText weightInput = holder.weightInput;
        final TextInputLayout weightLayout = holder.weightLayout;
        final LinearLayout extraInfo = holder.extraInfo;
        final ImageButton videoButton = holder.videoButton;
        final ImageButton saveButton = holder.saveButton;
        final ImageButton cancelButton = holder.cancelButton;
        final EditText notesInput = holder.notesInput;
        final Switch ignoreWeightSwitch = holder.ignoreWeightSwitch;

        // TODO make sure to hidekeyboard when close buttons are pressed

        weightButton.setOnClickListener((v) -> {
            exercise.setExtraShown(true);

            weightLayout.setVisibility(View.VISIBLE);
            weightLayout.setHint(currentExercise);
            weightButton.setVisibility(View.GONE);
            ignoreWeightSwitch.setVisibility(View.VISIBLE);
            extraInfo.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.VISIBLE);
            videoButton.setVisibility(View.GONE);
            exerciseTV.setVisibility(View.GONE);
        });

        View.OnClickListener collapseExtraInfo = v -> {
            exercise.setExtraShown(false);
            weightLayout.setVisibility(View.GONE);
            weightButton.setVisibility(View.VISIBLE);
            extraInfo.setVisibility(View.GONE);
            saveButton.setVisibility(View.GONE);
            cancelButton.setVisibility(View.GONE);
            ignoreWeightSwitch.setVisibility(View.GONE);
            videoButton.setVisibility(View.VISIBLE);
            exerciseTV.setVisibility(View.VISIBLE);
            notifyDataSetChanged(); // avoids animation on closing the extra info
        };
        saveButton.setOnClickListener(collapseExtraInfo);
        cancelButton.setOnClickListener(collapseExtraInfo);
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return exercises.size();
    }
}