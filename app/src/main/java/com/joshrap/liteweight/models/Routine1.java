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
public class Routine1 implements Model, Iterable<RoutineWeek1> {
    public static final String WEEKS = "weeks";

    private List<RoutineWeek1> weeks;

    public Routine1() {
        this.weeks = new ArrayList<>();
    }

    public static Routine1 pendingRoutine() {
        Routine1 routine = new Routine1();
        routine.addWeek(RoutineWeek1.EmptyWeek());
        return routine;
    }

    Routine1(Routine1 toBeCloned) {
        // copy constructor
        this.weeks = new ArrayList<>();
        for (RoutineWeek1 week : toBeCloned) {
            RoutineWeek1 routineWeek = new RoutineWeek1();
            for (RoutineDay1 day : week) {
                routineWeek.addDay(day.clone());
            }
            this.weeks.add(routineWeek);
        }
    }

    Routine1(List<Object> json) {
        if (json == null) {
            this.weeks = null;
        } else {
            this.weeks = new ArrayList<>();
            for (Object week : json) {
                RoutineWeek1 routineWeek = new RoutineWeek1((List<Object>) week);
                this.weeks.add(routineWeek);
            }
        }
    }

    public static boolean routinesDifferent(Routine1 routine1, Routine1 routine2) {
        // assume both routines are sorted

        // todo unit test
        if (routine1.getTotalNumberOfDays() != routine2.getTotalNumberOfDays()) {
            // one routine has more total days than the other, so not equal
            return true;
        }

        if (routine1.getNumberOfWeeks() != routine2.getNumberOfWeeks()) {
            // one routine has more or less weeks than the other, so not equal
            return true;
        }

        for (RoutineWeek1 week : routine1) {
            int weekPosition = week.getIndex(); // todo rename weekPosition or routineOrder
            RoutineWeek1 otherWeek = routine2.getWeek(weekPosition);
            if (week.getNumberOfDays() != otherWeek.getNumberOfDays()) {
                // one routine has more or less days in a week than the other, so not equal
                return true;
            }

            for (RoutineDay1 day : week) {
                int dayPosition = day.getIndex();
                RoutineDay1 otherDay = otherWeek.getDay(dayPosition);

                List<RoutineExercise1> exercises1 = day.getExercises();
                List<RoutineExercise1> exercises2 = otherDay.getExercises();
                if (exercises1.size() != exercises2.size()) {
                    // one routine has more or less exercises in a day than the other, so the two routines are different
                    return true;
                }
                for (int i = 0; i < exercises1.size(); i++) {
                    if (RoutineExercise1.exercisesDifferent(exercises1.get(i), exercises2.get(i))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    public RoutineWeek1 getWeek(int weekPosition) {
        return this.weeks.get(weekPosition);
    }

    public void addWeek(RoutineWeek1 week) {
        week.setIndex(this.weeks.size());
        this.weeks.add(week);
    }

    public void addEmptyWeek() {
        RoutineWeek1 week = RoutineWeek1.EmptyWeek();
        week.setIndex(this.weeks.size());
        this.weeks.add(week);
    }

    public void putWeek(int index, RoutineWeek1 week) {
        this.weeks.set(index, week);
        updateWeeksSortValues();
    }

    public void deleteWeek(int weekPosition) {
        this.weeks.removeIf(week -> (week.getIndex() == weekPosition));
        this.updateWeeksSortValues();
    }

    public void updateWeeksSortValues() {
        int i = 0;
        for (RoutineWeek1 week1 : this) {
            week1.setIndex(i);
            i++;
        }
    }


    public RoutineDay1 getDay(int weekPosition, int dayPosition) {
        return this.weeks.get(weekPosition).getDay(dayPosition);
    }

    public void appendNewEmptyDay(int week) {
        int weekSize = this.weeks.get(week).getNumberOfDays();
        RoutineDay1 routineDay = new RoutineDay1(weekSize); // todo grr
        if (this.getWeeks().get(week) == null) {
            // week with this index doesn't exist yet, so create it before appending the day
            this.addWeek(new RoutineWeek1());
        }
        this.getWeek(week).addDay(routineDay);
    }

    public void appendDay(int week, RoutineDay1 day) {
        int weekSize = this.weeks.get(week).getNumberOfDays();
        day.setIndex(weekSize);
        if (this.getWeeks().get(week) == null) {
            // week with this index doesn't exist yet, so create it before appending the day
            this.addWeek(new RoutineWeek1());
        }
        this.getWeek(week).addDay(day);
    }

    public void putDay(int weekIndex, int dayIndex, RoutineDay1 day) {
        this.getWeek(weekIndex).putDay(dayIndex, day);
        this.getWeek(weekIndex).updateDayIndices();
    }

    public void deleteDay(int weekPosition, int dayPosition) {
        this.getWeek(weekPosition).deleteDay(dayPosition);
    }


    public void sortDay(int week, int day, int sortVal, Map<String, String> idToName) {
        this.getDay(week, day).sortDay(sortVal, idToName);
    }


    public List<RoutineExercise1> getExerciseListForDay(int weekPosition, int dayPosition) {
        return new ArrayList<>(this.weeks.get(weekPosition).getDay(dayPosition).getExercises());
    }

    public void addExercise(int week, int day, final RoutineExercise1 routineExercise) {
        this.getDay(week, day).insertNewExercise(routineExercise);
    }

    public void removeExercise(int week, int day, String exerciseId) {
        this.getDay(week, day).deleteExercise(exerciseId);
    }

    public void swapExerciseOrder(int week, int day, int fromPosition, int toPosition) {
        this.getDay(week, day).swapExerciseOrder(fromPosition, toPosition);
    }


    public int getNumberOfWeeks() {
        return this.weeks.size();
    }

    public boolean isEmpty() {
        return this.getNumberOfWeeks() == 0;
    }

    public int getTotalNumberOfDays() {
        int days = 0;
        for (RoutineWeek1 week : this) {
            days += week.getNumberOfDays();
        }
        return days;
    }

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> retVal = new HashMap<>();
        List<Object> jsonWeeks = new ArrayList<>();
        for (RoutineWeek1 week : this) {
            jsonWeeks.add(week.asMap());
        }
        retVal.put(WEEKS, jsonWeeks);
        return retVal;
    }

    @NonNull
    @Override
    public Iterator<RoutineWeek1> iterator() {
        return this.weeks.iterator();
    }
}
