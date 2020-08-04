package com.joshrap.liteweight.models;

import com.joshrap.liteweight.interfaces.Model;

import java.util.HashMap;
import java.util.Map;

import lombok.ToString;

@ToString
public class RoutineDayMap implements Model {

    // map of all exercises in a given day. Maps sortValue to the given exercise

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

    // TODO sort method

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> retVal = new HashMap<>();
        for (Integer sortVal : this.exerciseRoutineMap.keySet()) {
            retVal.put(sortVal.toString(), this.exerciseRoutineMap.get(sortVal).asMap());
        }
        return null;
    }

}
