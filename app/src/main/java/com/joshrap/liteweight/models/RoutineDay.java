package com.joshrap.liteweight.models;

import android.annotation.SuppressLint;

import com.joshrap.liteweight.interfaces.Model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
@SuppressLint("UseSparseArrays")
public class RoutineDay implements Iterable<Integer>, Model {
    public static final int alphabeticalSortAscending = 0;
    public static final int alphabeticalSortDescending = 1;
    public static final int weightSortAscending = 3;
    public static final int weightSortDescending = 4;
    public static final int customSort = 5;

    private Map<Integer, RoutineExercise> exercises;

    RoutineDay() {
        this.exercises = new HashMap<>();
    }

    RoutineDay(Map<String, Object> exercisesForDay) {
        this.exercises = new HashMap<>();
        for (String sortVal : exercisesForDay.keySet()) {
            RoutineExercise routineExercise = new RoutineExercise((Map<String, Object>) exercisesForDay.get(sortVal));
            this.exercises.put(Integer.parseInt(sortVal), routineExercise);
        }
    }

    public RoutineDay clone() {
        RoutineDay retVal = new RoutineDay();
        for (Integer sortVal : this.exercises.keySet()) {
            RoutineExercise specificExerciseCloned = new RoutineExercise(this.exercises.get(sortVal));
            retVal.getExercises().put(sortVal, specificExerciseCloned);
        }
        return retVal;
    }

    void insertNewExercise(RoutineExercise routineExercise) {
        this.exercises.put(this.exercises.keySet().size(), routineExercise);
    }

    public int getNumberOfExercises() {
        return this.exercises.size();
    }

    RoutineExercise getExercise(int sortVal) {
        return this.exercises.get(sortVal);
    }

    void sortDay(int sortVal, Map<String, String> idToName) {
        List<RoutineExercise> list = new LinkedList(exercises.values());
        if (sortVal == alphabeticalSortAscending) {
            Collections.sort(list, (o1, o2) -> idToName.get(o1.getExerciseId()).compareTo(idToName.get(o2.getExerciseId())));
        } else if (sortVal == alphabeticalSortDescending) {
            Collections.sort(list, (o1, o2) -> idToName.get(o2.getExerciseId()).compareTo(idToName.get(o1.getExerciseId())));
        } else if (sortVal == weightSortAscending) {
            Collections.sort(list, (o1, o2) -> Double.compare(o1.getWeight(), o2.getWeight()));
        } else if (sortVal == weightSortDescending) {
            Collections.sort(list, (o1, o2) -> Double.compare(o2.getWeight(), o1.getWeight()));
        }

        int i = 0;
        Map<Integer, RoutineExercise> temp = new HashMap<>();
        for (RoutineExercise routineExercise : list) {
            temp.put(i, routineExercise);
            i++;
        }
        this.exercises = temp;
    }

    void swapExerciseOrder(int i, int j) {
        RoutineExercise fromExercise = this.exercises.get(i);
        RoutineExercise toExercise = this.exercises.get(j);
        this.exercises.put(j, fromExercise);
        this.exercises.put(i, toExercise);
        balanceMap();
    }

    boolean deleteExercise(String exerciseId) {
        boolean deleted = false;

        int index = -1;
        for (Integer sortVal : this.exercises.keySet()) {
            if (this.exercises.get(sortVal).getExerciseId().equals(exerciseId)) {
                index = sortVal;
            }
        }
        if (index != -1) {
            this.exercises.remove(index);
            balanceMap();
            deleted = true;
        }
        return deleted;
    }

    private void balanceMap() {
        int i = 0;
        Map<Integer, RoutineExercise> temp = new HashMap<>();
        for (Integer sortVal : this.exercises.keySet()) {
            temp.put(i, this.exercises.get(sortVal));
            i++;
        }
        this.exercises = temp;
    }

    @Override
    public Iterator<Integer> iterator() {
        return this.exercises.keySet().iterator();
    }

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> retVal = new HashMap<>();
        for (Integer sortVal : this) {
            retVal.put(sortVal.toString(), this.getExercise(sortVal).asMap());
        }
        return retVal;
    }
}
