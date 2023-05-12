package com.joshrap.liteweight.models.workout;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoutineWeek implements Iterable<RoutineDay> {

    private List<RoutineDay> days = new ArrayList<>();

    public static RoutineWeek EmptyWeek() {
        RoutineWeek week = new RoutineWeek();
        week.addDay(new RoutineDay());
        return week;
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

    public void removeDay(RoutineDay routineDay) {
        int index = -1;
        for (int i = 0; i < this.days.size(); i++) {
            RoutineDay day = this.days.get(i);
            if (day == routineDay) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            this.days.remove(index);
        }
    }

    @NonNull
    @Override
    public Iterator<RoutineDay> iterator() {
        return this.days.iterator();
    }
}
