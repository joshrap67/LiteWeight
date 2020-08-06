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

    public Routine(Map<String, Object> json) {
        if (json == null) {
            this.routine = null;
        } else {
            this.routine = new HashMap<>();
            for (String week : json.keySet()) {
                Map<String, Object> days = (Map<String, Object>) json.get(week);
                for (String day : days.keySet()) {
                    Map<String, Object> exerciseList = (Map<String, Object>) ((Map<String, Object>) json.get(week)).get(day);
                    Map<Integer, RoutineDayMap> specificDay = new HashMap<>();
                    for (String sortValue : exerciseList.keySet()) {
                        RoutineDayMap dayExerciseMap = new RoutineDayMap((Map<String, Object>) days.get(sortValue));
                        specificDay.put(Integer.parseInt(day), dayExerciseMap);
                    }
                    // TODO sort here?
                    this.routine.put(Integer.parseInt(week), specificDay);
                }
            }
        }
    }

    public Routine() {
        this.routine = new HashMap<>();
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
        List<ExerciseRoutine> list = new ArrayList<>();
        for (Integer sortVal : this.routine.get(week).get(day).getExerciseRoutineMap().keySet()) {
            list.add(this.routine.get(week).get(day).getExerciseRoutineMap().get(sortVal));
        }
        return list;
    }

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> retVal = new HashMap<>();
        if (this.routine != null) {
            for (Integer week : this.routine.keySet()) {
                for (Integer day : this.routine.get(week).keySet()) {
                    for (Integer sortVal : this.routine.get(week).get(day).getExerciseRoutineMap().keySet()) {

                    }
                    Map<String, Object> specificDay = new HashMap<>();
//                    specificDay.put(day.toString(), this.routine.get(week).get(day).asMap());
                    retVal.put(week.toString(), specificDay);
                }
            }
        }
        return retVal;
    }
}
