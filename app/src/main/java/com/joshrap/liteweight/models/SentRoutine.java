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
class SentRoutine implements Model, Iterable<Integer> {

    private Map<Integer, SentWeek> weeks;

    SentRoutine() {
        this.weeks = new HashMap<>();
    }

    SentRoutine(Map<String, Object> json) {
        if (json == null) {
            this.weeks = null;
        } else {
            this.weeks = new HashMap<>();
            for (String week : json.keySet()) {
                SentWeek sentWeek = new SentWeek((Map<String, Object>) json.get(week));
                this.weeks.put(Integer.parseInt(week), sentWeek);
            }
        }
    }

    public List<SentExercise> getExerciseListForDay(int week, int day) {
        List<SentExercise> exerciseList = new ArrayList<>();
        for (Integer sortVal : this.getWeek(week).getDay(day)) {
            exerciseList.add(this.getDay(week, day).getExercise(sortVal));
        }
        return exerciseList;
    }

    SentWeek getWeek(int week) {
        return this.weeks.get(week);
    }

    SentDay getDay(int week, int day) {
        return this.weeks.get(week).getDay(day);
    }

    void putWeek(int weekIndex, SentWeek week) {
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
