package com.joshrap.liteweight.models;

import com.joshrap.liteweight.interfaces.Model;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.ToString;

@ToString
public class RoutineDayMap implements Model {

    // map of all exercises in a given day. Maps sortValue to the given exercise

    public static final int alphabeticalSortAscending = 0;
    public static final int alphabeticalSortDescending = 1;
    public static final int weightSortAscending = 3;
    public static final int weightSortDescending = 4;
    public static final int customSort = 5;

    private Map<Integer, ExerciseRoutine> exerciseRoutineMap;

    RoutineDayMap(Map<String, Object> json) {
        this.exerciseRoutineMap = new HashMap<>();
        for (String sortVal : json.keySet()) {
            this.exerciseRoutineMap.put(Integer.valueOf(sortVal), new ExerciseRoutine((Map<String, Object>) json.get(sortVal)));
        }
    }

    RoutineDayMap() {
        this.exerciseRoutineMap = new HashMap<>();
    }

    Map<Integer, ExerciseRoutine> getExerciseRoutineMap() {
        return exerciseRoutineMap;
    }

    void insertNewExercise(ExerciseRoutine exerciseRoutine) {
        this.exerciseRoutineMap.put(this.exerciseRoutineMap.keySet().size(), exerciseRoutine);
    }

    void deleteExercise(String exerciseId) {
        int index = -1;
        for (Integer sortVal : this.exerciseRoutineMap.keySet()) {
            if (this.exerciseRoutineMap.get(sortVal).getExerciseId().equals(exerciseId)) {
                index = sortVal;
            }
        }
        this.exerciseRoutineMap.remove(index);
        balanceMap();
    }

    private void balanceMap() {
        int i = 0;
        Map<Integer, ExerciseRoutine> temp = new HashMap<>();
        for (Integer sortVal : this.exerciseRoutineMap.keySet()) {
            temp.put(i, this.exerciseRoutineMap.get(sortVal));
            i++;
        }
        this.exerciseRoutineMap = temp;
    }

    void sortDayMap(int sortVal, Map<String, String> idToName) {
        List<ExerciseRoutine> list = new LinkedList(exerciseRoutineMap.values());
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
        Map<Integer, ExerciseRoutine> temp = new HashMap<>();
        for (ExerciseRoutine exerciseRoutine : list) {
            temp.put(i, exerciseRoutine);
            i++;
        }
        this.exerciseRoutineMap = temp;
    }

    void swapExerciseOrder(int i, int j) {
        ExerciseRoutine fromExercise = this.exerciseRoutineMap.get(i);
        ExerciseRoutine toExercise = this.exerciseRoutineMap.get(j);
        this.exerciseRoutineMap.put(j, fromExercise);
        this.exerciseRoutineMap.put(i, toExercise);
        balanceMap();
    }

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> retVal = new HashMap<>();
        for (Integer sortVal : this.exerciseRoutineMap.keySet()) {
            retVal.put(sortVal.toString(), this.exerciseRoutineMap.get(sortVal).asMap());
        }
        return null;
    }

}
