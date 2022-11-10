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
public class RoutineWeek implements Iterable<RoutineDay>, Model {
    public static final String DAYS = "days";

    private List<RoutineDay> days;

    RoutineWeek() {
        this.days = new ArrayList<>();
    }

    public static RoutineWeek EmptyWeek() {
        RoutineWeek week = new RoutineWeek();
        week.addDay(new RoutineDay());
        return week;
    }

    RoutineWeek(Map<String, Object> json) {
        this.days = new ArrayList<>();

        List<Object> jsonDays = (List<Object>) json.get(DAYS);
        for (Object day : jsonDays) {
            RoutineDay routineDay = new RoutineDay((Map<String, Object>) day);
            this.days.add(routineDay);
        }
    }

    @NonNull
    public RoutineWeek clone() {
        RoutineWeek retVal = new RoutineWeek();
        for (RoutineDay day : this) {
            RoutineDay dayToBeCloned = day.clone();
            retVal.addDay(dayToBeCloned);
        }
        return retVal;
    }

    public int getNumberOfDays() {
        return this.days.size();
    }

    RoutineDay getDay(int dayPosition) {
        return this.days.get(dayPosition);
    }

    void deleteDay(int dayPosition) {
        this.days.remove(dayPosition);
    }

    public void addDay(RoutineDay routineDay) {
        this.days.add(routineDay);
    }

    public void putDay(int dayPosition, RoutineDay routineDay) {
        this.days.set(dayPosition, routineDay);
    }

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> retVal = new HashMap<>();
        List<Object> jsonDays = new ArrayList<>();
        for (RoutineDay day : this) {
            jsonDays.add(day.asMap());
        }
        retVal.put(DAYS, jsonDays);

        return retVal;
    }

    @NonNull
    @Override
    public Iterator<RoutineDay> iterator() {
        return this.days.iterator();
    }
}
