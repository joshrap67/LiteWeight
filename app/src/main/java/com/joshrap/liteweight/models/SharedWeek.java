package com.joshrap.liteweight.models;

import android.annotation.SuppressLint;

import com.joshrap.liteweight.interfaces.Model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import lombok.Data;

@Data
@SuppressLint("UseSparseArrays")
public class SharedWeek implements Iterable<Integer>, Model {

    private Map<Integer, SharedDay> days;

    public SharedWeek() {
        this.days = new HashMap<>();
    }

    public SharedWeek(Map<String, Object> daysForWeek) {
        this.days = new HashMap<>();
        for (String day : daysForWeek.keySet()) {
            SharedDay sharedDay = new SharedDay((Map<String, Object>) daysForWeek.get(day));
            this.days.put(Integer.parseInt(day), sharedDay);
        }
    }

    public int getNumberOfDays() {
        return this.days.size();
    }

    public SharedDay getDay(int day) {
        return this.days.get(day);
    }

    public void put(int dayIndex, SharedDay sharedDay) {
        this.days.put(dayIndex, sharedDay);
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
