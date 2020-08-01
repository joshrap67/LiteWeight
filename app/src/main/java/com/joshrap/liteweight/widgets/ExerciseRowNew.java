package com.joshrap.liteweight.widgets;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;

import com.joshrap.liteweight.database.entities.ExerciseEntity;
import com.joshrap.liteweight.database.entities.WorkoutEntity;
import com.joshrap.liteweight.database.viewModels.ExerciseViewModel;
import com.joshrap.liteweight.database.viewModels.WorkoutViewModel;
import com.joshrap.liteweight.helpers.ExerciseHelper;
import com.joshrap.liteweight.helpers.WeightHelper;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.imports.Variables;

public class ExerciseRowNew implements Comparable<ExerciseRowNew> {
    private Context context;
    private Activity activity;
    private String name, formattedWeight;
    private boolean status, videos, ignoreWeight, metricUnits;
    private double weight;
    private WorkoutViewModel workoutModel;
    private ExerciseViewModel exerciseModel;
    private WorkoutEntity workoutEntity;
    private ExerciseEntity exerciseEntity;

    public ExerciseRowNew(final WorkoutEntity workoutEntity, ExerciseEntity exerciseEntity, Context context, Activity activity,
                       boolean videos, boolean metricUnits, WorkoutViewModel workoutViewModel,
                       ExerciseViewModel exerciseViewModel) {
        /*
            Constructor utilized by CurrentWorkoutFragment
         */
        this.workoutEntity = workoutEntity;
        this.exerciseEntity = exerciseEntity;
        this.context = context;
        this.activity = activity;
        this.videos = videos;
        this.metricUnits = metricUnits;
        this.workoutModel = workoutViewModel;
        this.exerciseModel = exerciseViewModel;
        this.status = workoutEntity.getStatus();
        this.name = workoutEntity.getExercise();
    }

    public void setStatus(boolean aStatus) {
        /*
            Sets the status of the exercise as either being complete or incomplete.
         */
        status = aStatus;
        workoutEntity.setStatus(aStatus);
    }

    public WorkoutEntity getWorkoutEntity() {
        return this.workoutEntity;
    }

    public String getName() {
        return this.name;
    }

    public View getDisplayedRow() {
            /*
                Takes all of the information from the instance variables of this exercise and puts it into a row to be displayed
                by the main table in the CurrentWorkout fragment.
             */
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View row = inflater.inflate(R.layout.row_exercise_new, null);
        final CheckBox exerciseNameCheckbox = row.findViewById(R.id.exercise_name);
        final EditText weightView = row.findViewById(R.id.weight_view);
        // setup checkbox
        exerciseNameCheckbox.setText(name);
        if (status) {
            exerciseNameCheckbox.setChecked(true);
        }
        exerciseNameCheckbox.setOnClickListener(v -> {
            if (status) {
                workoutEntity.setStatus(false);
                workoutModel.update(workoutEntity);
                status = false;
            } else {
                workoutEntity.setStatus(true);
                workoutModel.update(workoutEntity);
                status = true;
            }
        });
        // set up weight button
        if (metricUnits) {
            // value in DB is always in murican units
            weight = exerciseEntity.getCurrentWeight() * Variables.KG;
        } else {
            weight = exerciseEntity.getCurrentWeight();
        }
        formattedWeight = WeightHelper.getFormattedWeight(weight);
        if (weight >= 0) {
            weightView.setText(formattedWeight + (metricUnits ? " kg" : " lb"));
        } else {
            weightView.setText("N/A");
        }
        weightView.setOnClickListener(v -> {
            // show a popup for editing the weight of the exercise
            showEditWeightPopup(weightView);
        });
        // setup video button
        if (videos) {
            ImageButton videoButton = row.findViewById(R.id.launch_video);
            videoButton.setOnClickListener(v -> ExerciseHelper.launchVideo(exerciseEntity, context, activity));
        } else {
            ImageView videoButton = row.findViewById(R.id.launch_video);
            videoButton.setVisibility(View.GONE);
        }
        return row;
    }

    private void showEditWeightPopup(final EditText weightView) {
        /*
            If user clicks on editText for weight, show a popup for editing the weight.
         */
        View popupView = activity.getLayoutInflater().inflate(R.layout.popup_edit_weight, null);
        final EditText weightInput = popupView.findViewById(R.id.name_input);
        weightInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_WEIGHT_DIGITS)});
        weightInput.setHint(formattedWeight + (metricUnits ? " kg" : " lb"));
        final Switch ignoreWeightSwitch = popupView.findViewById(R.id.ignore_weight_switch);
        if (weight < 0) {
            ignoreWeightSwitch.setChecked(true);
            ignoreWeight = true;
            weightInput.setHint("N/A");
            weightInput.setEnabled(false);
        } else {
            ignoreWeight = false;
            ignoreWeightSwitch.setChecked(false);
            weightInput.setEnabled(true);
        }
        ignoreWeightSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ignoreWeight = isChecked;
            if (ignoreWeight) {
                weightInput.setHint("N/A");
                weightInput.setEnabled(false);
            } else {
                weightInput.setEnabled(true);
                if (exerciseEntity.getCurrentWeight() == Variables.IGNORE_WEIGHT_VALUE) {
                    weightInput.setHint(String.format("Enter weight (%s)", (metricUnits ? " kg" : " lb"))); // to get rid of sentinel value from Database
                } else {
                    weightInput.setHint(formattedWeight + (metricUnits ? " kg" : " lb"));
                }
            }
        });
        // now that the widgets are all initialized from the view, create the dialog and insert the view into it
        final AlertDialog alertDialog = new AlertDialog.Builder(context, R.style.AlertDialogTheme)
                .setView(popupView)
                .setTitle(name)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();
        alertDialog.setOnShowListener(dialogInterface -> {
            Button saveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveButton.setOnClickListener(view -> {
                if (ignoreWeight) {
                    weight = Variables.IGNORE_WEIGHT_VALUE;
                    exerciseEntity.setCurrentWeight(weight);
                    weightView.setText("N/A");
                    exerciseModel.update(exerciseEntity);
                    alertDialog.dismiss();
                } else if (!weightInput.getText().toString().equals("")) {
                    weight = Double.parseDouble(weightInput.getText().toString());
                    formattedWeight = WeightHelper.getFormattedWeight(weight);
                    weightView.setText(formattedWeight + (metricUnits ? " kg" : " lb"));
                    if (metricUnits) {
                        // convert if in metric
                        weight /= Variables.KG;
                    }
                    if (weight > exerciseEntity.getMaxWeight()) {
                        exerciseEntity.setMaxWeight(weight);
                    } else if (weight < exerciseEntity.getMinWeight() || exerciseEntity.getMinWeight() == 0) {
                        exerciseEntity.setMinWeight(weight);
                    }
                    exerciseEntity.setCurrentWeight(weight);
                    exerciseModel.update(exerciseEntity);
                    alertDialog.dismiss();
                } else {
                    weightInput.setError("Enter a valid weight!");
                }
            });
        });
        alertDialog.show();
    }

    public boolean getStatus() {
        return this.status;
    }

    @Override
    public int compareTo(ExerciseRowNew o) {
        // used for sorting purposes
        return this.name.toLowerCase().compareTo(o.name.toLowerCase());
    }
}
