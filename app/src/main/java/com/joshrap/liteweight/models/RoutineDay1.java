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
public class RoutineDay1 implements Iterable<RoutineExercise1>, Model {
    public static final int alphabeticalSortAscending = 0;
    public static final int alphabeticalSortDescending = 1;
    public static final int weightSortAscending = 3;
    public static final int weightSortDescending = 4;
    public static final int customSort = 5;

    public static final String EXERCISES = "exercises";
    public static final String INDEX = "index";

    private List<RoutineExercise1> exercises;
    private int index;
    // todo an optional label?

    RoutineDay1() {
        this.exercises = new ArrayList<>();
    }

    RoutineDay1(int index) {
        this.index = index;
        this.exercises = new ArrayList<>();
    }

    RoutineDay1(List<Object> exercisesForDay) {
        this.exercises = new ArrayList<>();
        for (Object exercise : exercisesForDay) {
            RoutineExercise1 routineExercise = new RoutineExercise1((Map<String, Object>) exercise);
            this.exercises.add(routineExercise);
        }
    }

    public RoutineDay1 clone() {
        RoutineDay1 retVal = new RoutineDay1(this.index);
        for (RoutineExercise1 routineExercise : this.exercises) {
            RoutineExercise1 specificExerciseCloned = new RoutineExercise1(routineExercise);
            retVal.getExercises().add(specificExerciseCloned);
        }
        return retVal;
    }

    void insertNewExercise(RoutineExercise1 routineExercise) {
        this.exercises.add(routineExercise);
    }

    public int getNumberOfExercises() {
        return this.exercises.size();
    }

    RoutineExercise1 getExercise(int sortVal) {
        return this.exercises.get(sortVal);
    }

    void sortDay(int sortVal, Map<String, String> idToName) {
        if (sortVal == alphabeticalSortAscending) {
            this.exercises.sort(Comparator.comparing(o -> idToName.get(o.getExerciseId())));
        } else if (sortVal == alphabeticalSortDescending) {
            this.exercises.sort((o1, o2) -> idToName.get(o2.getExerciseId()).compareTo(idToName.get(o1.getExerciseId())));
        } else if (sortVal == weightSortAscending) {
            this.exercises.sort(Comparator.comparingDouble(RoutineExercise1::getWeight));
        } else if (sortVal == weightSortDescending) {
            this.exercises.sort((o1, o2) -> Double.compare(o2.getWeight(), o1.getWeight()));
        }
    }

    void swapExerciseOrder(int i, int j) {
        Collections.swap(this.exercises, i, j);
        updateExercisesSortValues();
    }

    boolean deleteExercise(String exerciseId) {
        boolean deleted = this.exercises.removeIf(x -> (x.getExerciseId().equals(exerciseId)));
        if (deleted) {
            updateExercisesSortValues();
        }

        return deleted;
    }

    private void updateExercisesSortValues() {
        int i = 0;
        for (RoutineExercise1 exercise : this) {
            exercise.setIndex(i);
            i++;
        }
    }

    @NonNull
    @Override
    public Iterator<RoutineExercise1> iterator() {
        return this.exercises.iterator();
    }

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> retVal = new HashMap<>();
        List<Object> jsonExercises = new ArrayList<>();
        for (RoutineExercise1 exercise : this) {
            jsonExercises.add(exercise.asMap());
        }
        retVal.put(EXERCISES, jsonExercises);
        retVal.put(INDEX, this.index);
        return retVal;
    }
}
