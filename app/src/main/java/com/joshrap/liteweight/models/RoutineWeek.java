package com.joshrap.liteweight.models;

import android.annotation.SuppressLint;

import com.joshrap.liteweight.interfaces.Model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import lombok.Data;

@Data
@SuppressLint("UseSparseArrays")
public class RoutineWeek implements Iterable<Integer>, Model {

    private Map<Integer, RoutineDay> days;

    RoutineWeek() {
        this.days = new HashMap<>();
    }

    RoutineWeek(Map<String, Object> daysForWeek) {
        this.days = new HashMap<>();
        for (String day : daysForWeek.keySet()) {
            RoutineDay routineDay = new RoutineDay((Map<String, Object>) daysForWeek.get(day));
            this.days.put(Integer.parseInt(day), routineDay);
        }
    }

    public RoutineWeek clone() {
        RoutineWeek retVal = new RoutineWeek();
        for (Integer day : this.days.keySet()) {
            RoutineDay dayToBeCloned = this.days.get(day).clone();
            retVal.put(day, dayToBeCloned);
        }
        return retVal;
    }

    public int getNumberOfDays() {
        return this.days.size();
    }

    RoutineDay getDay(int day) {
        return this.days.get(day);
    }

    void deleteDay(int day) {
        this.days.remove(day);
        balanceMap();
    }

    public void put(int dayIndex, RoutineDay routineDay) {
        this.days.put(dayIndex, routineDay);
    }

    private void balanceMap() {
        int i = 0;
        Map<Integer, RoutineDay> temp = new HashMap<>();
        for (Integer day : this.days.keySet()) {
            temp.put(i, this.days.get(day));
            i++;
        }
        this.days = temp;
    }

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> retVal = new HashMap<>();
        for (Integer day : this) {
            retVal.put(day.toString(), this.getDay(day).asMap());
        }
        return retVal;
    }

    @Override
    public Iterator<Integer> iterator() {
        return this.days.keySet().iterator();
    }
}
