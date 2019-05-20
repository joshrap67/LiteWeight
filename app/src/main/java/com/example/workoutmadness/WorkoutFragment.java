package com.example.workoutmadness;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class WorkoutFragment extends Fragment {
    private View view;
    private TextView dayTV;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_workout,container,false);
        populateWorkouts();
        return view;
    }

    public void populateWorkouts(){
        TableLayout ll = (TableLayout) view.findViewById(R.id.main_table);
        getCurrentDay();
        for (int i = 0; i <15; i++) {
            TableRow row = new TableRow(getActivity());
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(lp);
            CheckBox exercise = new CheckBox(getActivity());
            exercise.setText("Barbell Curls");
            row.addView(exercise);
            Button videoButton = new Button(getActivity());
            videoButton.setText("Video");
            row.addView(videoButton);
            ll.addView(row,i);
        }
    }

    public void getCurrentDay(){
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new InputStreamReader(getContext().getAssets().open("currentDay.txt")));
            String day = reader.readLine();
            dayTV = view.findViewById(R.id.dayTextView);
            dayTV.setText(day);
        }
        catch (Exception e){
            Log.d("ERROR","Error when trying to read day file!");
        }

    }
}
