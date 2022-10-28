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
public class SharedRoutine implements Model, Iterable<SharedWeek> {

    public static final String WEEKS = "weeks";

    private List<SharedWeek> weeks;

    public SharedRoutine() {
        this.weeks = new ArrayList<>();
    }

    public SharedRoutine(Map<String, Object> json) {
        if (json == null) {
            this.weeks = new ArrayList<>();
        } else {
            this.weeks = new ArrayList<>();
            List<Object> jsonWeeks = (List<Object>) json.get(WEEKS);
            for (Object jsonKey : jsonWeeks) {
                SharedWeek sharedWeek = new SharedWeek((Map<String, Object>) jsonKey);
                this.weeks.add(sharedWeek);
            }
        }
    }

    public List<SharedExercise> getExerciseListForDay(int week, int day) {
        return new ArrayList<>(this.getDay(week, day).getExercises());
    }

    public SharedWeek getWeek(int week) {
        return this.weeks.get(week);
    }

    public SharedDay getDay(int week, int day) {
        return this.weeks.get(week).getDay(day);
    }

    public int getNumberOfWeeks() {
        return this.weeks.size();
    }

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> retVal = new HashMap<>();
        List<Object> jsonWeeks = new ArrayList<>();
        for (SharedWeek week : this) {
            jsonWeeks.add(week.asMap());
        }
        retVal.put(WEEKS, jsonWeeks);
        return retVal;
    }

    @Override
    public Iterator<SharedWeek> iterator() {
        return this.weeks.iterator();
    }
}
