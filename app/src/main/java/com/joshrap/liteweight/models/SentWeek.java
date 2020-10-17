package com.joshrap.liteweight.models;

import android.annotation.SuppressLint;

import com.joshrap.liteweight.interfaces.Model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import lombok.Data;

@Data
@SuppressLint("UseSparseArrays")
public class SentWeek implements Iterable<Integer>, Model {

    private Map<Integer, SentDay> days;

    public SentWeek() {
        this.days = new HashMap<>();
    }

    public SentWeek(Map<String, Object> daysForWeek) {
        this.days = new HashMap<>();
        for (String day : daysForWeek.keySet()) {
            SentDay sentDay = new SentDay((Map<String, Object>) daysForWeek.get(day));
            this.days.put(Integer.parseInt(day), sentDay);
        }
    }

    public int getNumberOfDays() {
        return this.days.size();
    }

    public SentDay getDay(int day) {
        return this.days.get(day);
    }

    public void put(int dayIndex, SentDay sentDay) {
        this.days.put(dayIndex, sentDay);
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
