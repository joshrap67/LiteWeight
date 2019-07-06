package com.example.workoutmadness;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

public class MyWorkoutFragment extends Fragment {
    private View view;
    private ArrayList<String> workouts = new ArrayList<>();
    private TextView selectedWorkout, statisticsTV;
    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_my_workouts,container,false);
        ((MainActivity)getActivity()).updateToolbarTitle("My Workouts");
        listView= view.findViewById(R.id.workout_list);
        selectedWorkout=view.findViewById(R.id.selected_workout_text_view);
        statisticsTV=view.findViewById(R.id.stat_text_view);
        populateListView();
        return view;
    }

    public void populateListView(){
        //todo when workout is selected it moves to the top of the list.
        String msg = "Times Completed: 420\n" +
                "Average Percentage of Exercises Completed: 69%\n" +
                "Most Frequent Exercise: Yeeting\n" +
                "Least Frequent Exercise: Dabbing";
        statisticsTV.setText(msg);
        workouts.add("Abc 1");
        workouts.add("Ce3 2");

        arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, workouts);
        listView.setAdapter(arrayAdapter);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        Collections.sort(workouts);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String oldText = selectedWorkout.getText().toString();
                String newText = workouts.get(position);
                workouts.add(oldText);
                Collections.sort(workouts);
                selectedWorkout.setText(newText);
                workouts.remove(newText);
                arrayAdapter.notifyDataSetChanged();

            }
        });
    }

    public void updateWorkoutList(){

    }

}
