package com.joshrap.liteweight.models;

import android.annotation.SuppressLint;

import com.joshrap.liteweight.interfaces.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lombok.Data;

@Data
@SuppressLint("UseSparseArrays")
public class SharedRoutine implements Model, Iterable<Integer> {

    private Map<Integer, SharedWeek> weeks;

    public SharedRoutine() {
        this.weeks = new HashMap<>();
    }

    public SharedRoutine(Map<String, Object> json) {
        if (json == null) {
            this.weeks = null;
        } else {
            this.weeks = new HashMap<>();
            for (String week : json.keySet()) {
                SharedWeek sharedWeek = new SharedWeek((Map<String, Object>) json.get(week));
                this.weeks.put(Integer.parseInt(week), sharedWeek);
            }
        }
    }

    public List<SharedExercise> getExerciseListForDay(int week, int day) {
        List<SharedExercise> exerciseList = new ArrayList<>();
        TreeMap<Integer, SharedExercise> sorted = new TreeMap<>(this.getWeek(week).getDay(day).getExercises());
        for (Integer sortVal : sorted.keySet()) {
            exerciseList.add(this.getDay(week, day).getExercise(sortVal));
        }
        return exerciseList;
    }

    public SharedWeek getWeek(int week) {
        return this.weeks.get(week);
    }

    public SharedDay getDay(int week, int day) {
        return this.weeks.get(week).getDay(day);
    }

    void putWeek(int weekIndex, SharedWeek week) {
        this.weeks.put(weekIndex, week);
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
