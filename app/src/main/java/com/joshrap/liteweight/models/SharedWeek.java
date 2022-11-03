package com.joshrap.liteweight.models;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import com.joshrap.liteweight.interfaces.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
@SuppressLint("UseSparseArrays")
public class SharedWeek implements Iterable<SharedDay>, Model {

    public static final String DAYS = "days";

    private List<SharedDay> days;

    public SharedWeek() {
        this.days = new ArrayList<>();
    }

    public SharedWeek(Map<String, Object> jsonWeek) {
        this.days = new ArrayList<>();

        List<Object> jsonDays = (List<Object>) jsonWeek.get(DAYS);
        for (Object day : jsonDays) {
            SharedDay sharedDay = new SharedDay((Map<String, Object>) day);
            this.days.add(sharedDay);
        }
    }

    public int getNumberOfDays() {
        return this.days.size();
    }

    public SharedDay getDay(int day) {
        return this.days.get(day);
    }

    public void put(int dayIndex, SharedDay sharedDay) {
        this.days.set(dayIndex, sharedDay);
    }

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> retVal = new HashMap<>();
        List<Object> jsonDays = new ArrayList<>();
        for (SharedDay day : this) {
            jsonDays.add(day.asMap());
        }
        retVal.put(DAYS, jsonDays);

        return retVal;
    }

    @NonNull
    @Override
    public Iterator<SharedDay> iterator() {
        return this.days.iterator();
    }

}
