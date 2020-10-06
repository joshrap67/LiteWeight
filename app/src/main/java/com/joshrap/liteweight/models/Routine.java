package com.joshrap.liteweight.models;

import android.annotation.SuppressLint;

import com.joshrap.liteweight.interfaces.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
@SuppressLint("UseSparseArrays")
public class Routine implements Model, Iterable<Integer> {

    private Map<Integer, RoutineWeek> weeks;

    public Routine() {
        this.weeks = new HashMap<>();
    }

    Routine(Routine toBeCloned) {
        // copy constructor
        this.weeks = new HashMap<>();
        for (int week = 0; week < toBeCloned.getNumberOfWeeks(); week++) {
            RoutineWeek routineWeek = new RoutineWeek();
            for (int day = 0; day < toBeCloned.getWeek(week).getNumberOfDays(); day++) {
                routineWeek.put(day, toBeCloned.getDay(week, day).clone());
            }
            this.weeks.put(week, routineWeek);
        }
    }

    Routine(Map<String, Object> json) {
        if (json == null) {
            this.weeks = null;
        } else {
            this.weeks = new HashMap<>();
            for (String week : json.keySet()) {
                RoutineWeek routineWeek = new RoutineWeek((Map<String, Object>) json.get(week));
                this.weeks.put(Integer.parseInt(week), routineWeek);
            }
        }
    }

    public List<RoutineExercise> getExerciseListForDay(int week, int day) {
        List<RoutineExercise> exerciseList = new ArrayList<>();
        // TODO sanity sort based on index?
        for (Integer sortVal : this.getWeek(week).getDay(day)) {
            exerciseList.add(this.getDay(week, day).getExercise(sortVal));
        }
        return exerciseList;
    }

    public RoutineWeek getWeek(int week) {
        return this.weeks.get(week);
    }

    public RoutineDay getDay(int week, int day) {
        return this.weeks.get(week).getDay(day);
    }

    public void appendNewDay(int week, int day) {
        RoutineDay routineDay = new RoutineDay();
        if (this.getWeeks().get(week) == null) {
            // week with this index doesn't exist yet, so create it before appending the day
            this.putWeek(week, new RoutineWeek());
        }
        this.getWeek(week).put(day, routineDay);
    }

    public void putWeek(int weekIndex, RoutineWeek week) {
        this.weeks.put(weekIndex, week);
    }

    public void putDay(int weekIndex, int dayIndex, RoutineDay day) {
        this.getWeek(weekIndex).put(dayIndex, day);
    }

    public boolean removeExercise(int week, int day, String exerciseId) {
        return this.getDay(week, day).deleteExercise(exerciseId);
    }

    public void sortDay(int week, int day, int sortVal, Map<String, String> idToName) {
        this.getDay(week, day).sortDay(sortVal, idToName);
    }

    public int getNumberOfWeeks() {
        return this.weeks.size();
    }

    public int getTotalNumberOfDays() {
        int days = 0;
        for (Integer week : this.weeks.keySet()) {
            days += this.weeks.get(week).getNumberOfDays();
        }
        return days;
    }

    public void insertExercise(int week, int day, final RoutineExercise routineExercise) {
        this.getDay(week, day).insertNewExercise(routineExercise);
    }

    public void deleteWeek(int week) {
        this.weeks.remove(week);
        int i = 0;
        Map<Integer, RoutineWeek> temp = new HashMap<>();
        for (Integer weekIndex : this.weeks.keySet()) {
            temp.put(i, this.weeks.get(weekIndex));
            i++;
        }
        this.weeks = temp;
    }

    public void deleteDay(int week, int day) {
        this.getWeek(week).deleteDay(day);
    }

    public void swapExerciseOrder(int week, int day, int fromPosition, int toPosition) {
        this.getDay(week, day).swapExerciseOrder(fromPosition, toPosition);
    }

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> retVal = new HashMap<>();
        for (Integer week : this.weeks.keySet()) {
            retVal.put(week.toString(), this.getWeek(week).asMap());
        }

        return retVal;
    }

    @Override
    public Iterator<Integer> iterator() {
        return this.weeks.keySet().iterator();
    }
}
