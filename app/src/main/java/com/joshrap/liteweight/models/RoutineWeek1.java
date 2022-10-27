package com.joshrap.liteweight.models;

import androidx.annotation.NonNull;

import com.joshrap.liteweight.interfaces.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class RoutineWeek1 implements Iterable<RoutineDay1>, Model {
    public static final String DAYS = "days";
    public static final String INDEX = "index";

    private List<RoutineDay1> days;
    private int index;

    RoutineWeek1() {
        this.days = new ArrayList<>();
    }

    public static RoutineWeek1 EmptyWeek() {
        RoutineWeek1 week = new RoutineWeek1();
        week.addDay(new RoutineDay1());
        return week;
    }

    RoutineWeek1(int index) {
        this.days = new ArrayList<>();
        this.index = index;
    }

    RoutineWeek1(List<Object> daysForWeek) {
        this.days = new ArrayList<>();
        for (Object day : daysForWeek) {
            RoutineDay1 routineDay = new RoutineDay1((List<Object>) day);
            this.days.add(routineDay);
        }
    }

    public RoutineWeek1 clone() {
        RoutineWeek1 retVal = new RoutineWeek1();
        for (RoutineDay1 day : this) {
            RoutineDay1 dayToBeCloned = day.clone();
            retVal.addDay(dayToBeCloned);
        }
        return retVal;
    }

    public int getNumberOfDays() {
        return this.days.size();
    }

    RoutineDay1 getDay(int dayPosition) {
        return this.days.get(dayPosition);
    }

    void deleteDay(int dayPosition) {
        this.days.removeIf(day -> (day.getIndex() == dayPosition));
        updateDayIndices();
    }

    public void addDay(RoutineDay1 routineDay) {
        this.days.add(routineDay);
    }

    public void putDay(int dayPosition, RoutineDay1 routineDay) {
        this.days.set(dayPosition, routineDay);
    }

    void updateDayIndices() {
        int i = 0;
        for (RoutineDay1 day : this) {
            day.setIndex(i);
            i++;
        }
    }

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> retVal = new HashMap<>();
        List<Object> jsonDays = new ArrayList<>();
        for (RoutineDay1 day : this) {
            jsonDays.add(day.asMap());
        }
        retVal.put(DAYS, jsonDays);
        retVal.put(INDEX, this.index);

        return retVal;
    }

    @NonNull
    @Override
    public Iterator<RoutineDay1> iterator() {
        return this.days.iterator();
    }
}
