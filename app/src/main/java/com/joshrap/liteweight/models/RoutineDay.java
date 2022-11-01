package com.joshrap.liteweight.models;

import androidx.annotation.NonNull;

import com.joshrap.liteweight.interfaces.Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class RoutineDay implements Iterable<RoutineExercise>, Model {
    public static final int alphabeticalSortAscending = 0;
    public static final int alphabeticalSortDescending = 1;
    public static final int weightSortAscending = 3;
    public static final int weightSortDescending = 4;
    public static final int customSort = 5;

    public static final String EXERCISES = "exercises";
    public static final String TAG = "tag";

    private List<RoutineExercise> exercises;
    private String tag;

    RoutineDay() {
        this.exercises = new ArrayList<>();
    }

    RoutineDay(Map<String, Object> json) {
        this.exercises = new ArrayList<>();
        this.tag = (String) json.get(TAG);

        List<Object> jsonExercises = (List<Object>) json.get(EXERCISES);
        for (Object exercise : jsonExercises) {
            RoutineExercise routineExercise = new RoutineExercise((Map<String, Object>) exercise);
            this.exercises.add(routineExercise);
        }
    }

    public RoutineDay clone() {
        RoutineDay retVal = new RoutineDay();
        retVal.setTag(this.tag);
        for (RoutineExercise routineExercise : this.exercises) {
            RoutineExercise specificExerciseCloned = new RoutineExercise(routineExercise);
            retVal.getExercises().add(specificExerciseCloned);
        }
        return retVal;
    }

    void insertNewExercise(RoutineExercise routineExercise) {
        this.exercises.add(routineExercise);
    }

    public int getNumberOfExercises() {
        return this.exercises.size();
    }

    void sortDay(int sortMode, Map<String, String> idToName) {
        if (sortMode == alphabeticalSortAscending) {
            this.exercises.sort(Comparator.comparing(o -> idToName.get(o.getExerciseId())));
        } else if (sortMode == alphabeticalSortDescending) {
            this.exercises.sort((o1, o2) -> idToName.get(o2.getExerciseId()).compareTo(idToName.get(o1.getExerciseId())));
        } else if (sortMode == weightSortAscending) {
            this.exercises.sort(Comparator.comparingDouble(RoutineExercise::getWeight));
        } else if (sortMode == weightSortDescending) {
            this.exercises.sort((o1, o2) -> Double.compare(o2.getWeight(), o1.getWeight()));
        }
    }

    void swapExerciseOrder(int i, int j) {
        Collections.swap(this.exercises, i, j);
    }

    public boolean deleteExercise(String exerciseId) {
        return this.exercises.removeIf(x -> (x.getExerciseId().equals(exerciseId)));
    }

    @NonNull
    @Override
    public Iterator<RoutineExercise> iterator() {
        return this.exercises.iterator();
    }

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> retVal = new HashMap<>();
        List<Object> jsonExercises = new ArrayList<>();
        for (RoutineExercise exercise : this) {
            jsonExercises.add(exercise.asMap());
        }
        retVal.put(EXERCISES, jsonExercises);
        retVal.put(TAG, this.tag);
        return retVal;
    }
}
