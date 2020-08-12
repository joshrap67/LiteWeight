package com.joshrap.liteweight.models;

import com.joshrap.liteweight.interfaces.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class Routine implements Model {

    Map<Integer, Map<Integer, RoutineDayMap>> routine;

    Routine(Map<String, Object> json) {
        if (json == null) {
            this.routine = null;
        } else {
            this.routine = new HashMap<>();
            for (String week : json.keySet()) {
                Map<String, Object> days = (Map<String, Object>) json.get(week);
                Map<Integer, RoutineDayMap> specificDay = new HashMap<>();
                for (String day : days.keySet()) {

                    RoutineDayMap dayExerciseMap = new RoutineDayMap(
                            (Map<String, Object>) ((Map<String, Object>) json
                                    .get(week)).get(day));

                    specificDay.put(Integer.parseInt(day), dayExerciseMap);
                }
                this.routine.put(Integer.parseInt(week), specificDay);
            }
        }
    }

    public Routine() {
        this.routine = new HashMap<>();
    }

    public Map<Integer, RoutineDayMap> getWeek(int week) {
        return this.getRoutine().get(week);
    }

    public RoutineDayMap getDay(int week, int day) {
        return this.getRoutine().get(week).get(day);
    }

    public void appendNewDay(int week, int day) {
        RoutineDayMap dayExerciseMap = new RoutineDayMap();
        if (this.getRoutine().get(week) == null) {
            this.getRoutine().put(week, new HashMap<>());
        }
        this.routine.get(week).put(day, dayExerciseMap);
    }

    public void insertExercise(int week, int day, ExerciseRoutine exerciseRoutine) {
        this.routine.get(week).get(day).insertNewExercise(exerciseRoutine);
    }

    public void removeExercise(int week, int day, String exerciseId) {
        this.routine.get(week).get(day).deleteExercise(exerciseId);
    }

    public void deleteWeek(int week) {
        this.routine.remove(week);
        int i = 0;
        Map<Integer, Map<Integer, RoutineDayMap>> temp = new HashMap<>();
        for (Integer weekIndex : this.routine.keySet()) {
            temp.put(i, this.routine.get(weekIndex));
            i++;
        }
        this.routine = temp;
    }

    public void deleteDay(int week, int day) {
        this.routine.get(week).remove(day);
        int i = 0;
        Map<Integer, RoutineDayMap> temp = new HashMap<>();
        for (Integer dayIndex : this.routine.get(week).keySet()) {
            temp.put(i, this.routine.get(week).get(dayIndex));
            i++;
        }
        this.routine.put(week, temp);
    }

    public List<ExerciseRoutine> getExerciseListForDay(int week, int day) {
        // TODO sanity sort based on index
        List<ExerciseRoutine> list = new ArrayList<>();
        for (Integer sortVal : this.routine.get(week).get(day).getExerciseRoutineMap().keySet()) {
            list.add(this.routine.get(week).get(day).getExerciseRoutineMap().get(sortVal));
        }
        return list;
    }

    public void sortDay(int week, int day, int sortVal, Map<String, String> idToName) {
        this.routine.get(week).get(day).sortDayMap(sortVal, idToName);
    }

    public void swapExerciseOrder(int week, int day, int fromPosition, int toPosition) {
        this.routine.get(week).get(day).swapExerciseOrder(fromPosition, toPosition);
    }

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> retVal = new HashMap<>();
        if (this.routine != null) {
            for (Integer week : this.routine.keySet()) {
                Map<String, Object> specificDay = new HashMap<>();
                for (Integer day : this.routine.get(week).keySet()) {
                    Map<String, Object> exercisesForDay = new HashMap<>();
                    for (Integer sortVal : this.routine.get(week).get(day).getExerciseRoutineMap()
                            .keySet()) {
                        exercisesForDay.put(sortVal.toString(),
                                this.routine.get(week).get(day).getExerciseRoutineMap().get(sortVal)
                                        .asMap());
                    }
                    specificDay.put(day.toString(), exercisesForDay);
                }
                retVal.put(week.toString(), specificDay);
            }
        }
        return retVal;
    }
}
