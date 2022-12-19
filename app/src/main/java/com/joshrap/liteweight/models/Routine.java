package com.joshrap.liteweight.models;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import com.joshrap.liteweight.interfaces.Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
@SuppressLint("UseSparseArrays")
public class Routine implements Model, Iterable<RoutineWeek> {
    public static final String WEEKS = "weeks";

    private List<RoutineWeek> weeks;

    public Routine() {
        this.weeks = new ArrayList<>();
    }

    public static Routine emptyRoutine() {
        Routine routine = new Routine();
        routine.addWeek(RoutineWeek.EmptyWeek());
        return routine;
    }

    Routine(Routine toBeCloned) {
        // copy constructor
        this.weeks = new ArrayList<>();
        for (RoutineWeek week : toBeCloned) {
            RoutineWeek routineWeek = new RoutineWeek();
            for (RoutineDay day : week) {
                routineWeek.addDay(day.clone());
            }
            this.weeks.add(routineWeek);
        }
    }

    Routine(Map<String, Object> json) {
        if (json == null) {
            this.weeks = new ArrayList<>();
        } else {
            this.weeks = new ArrayList<>();
            List<Object> jsonWeeks = (List<Object>) json.get(WEEKS);
            for (Object week : jsonWeeks) {
                RoutineWeek routineWeek = new RoutineWeek((Map<String, Object>) week);
                this.weeks.add(routineWeek);
            }
        }
    }

    // assumes both routines are sorted
    public static boolean routinesDifferent(Routine routine1, Routine routine2) {
        if (routine1.getTotalNumberOfDays() != routine2.getTotalNumberOfDays()) {
            // one routine has more total days than the other, so not equal
            return true;
        }

        if (routine1.getNumberOfWeeks() != routine2.getNumberOfWeeks()) {
            // one routine has more or less weeks than the other, so not equal
            return true;
        }

        for (RoutineWeek week : routine1) {
            int weekPosition = routine1.getWeeks().indexOf(week);
            RoutineWeek otherWeek = routine2.getWeek(weekPosition);
            if (week.getNumberOfDays() != otherWeek.getNumberOfDays()) {
                // one routine has more or less days in a week than the other, so not equal
                return true;
            }

            for (RoutineDay day : week) {
                int dayPosition = week.getDays().indexOf(day);
                RoutineDay otherDay = otherWeek.getDay(dayPosition);

                List<RoutineExercise> exercises1 = day.getExercises();
                List<RoutineExercise> exercises2 = otherDay.getExercises();
                if (exercises1.size() != exercises2.size()) {
                    // one routine has more or less exercises in a day than the other, so the two routines are different
                    return true;
                }
                for (int i = 0; i < exercises1.size(); i++) {
                    if (RoutineExercise.exercisesDifferent(exercises1.get(i), exercises2.get(i))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    public RoutineWeek getWeek(int weekPosition) {
        return this.weeks.get(weekPosition);
    }

    public void addWeek(RoutineWeek week) {
        this.weeks.add(week);
    }

    public void addEmptyWeek() {
        this.weeks.add(RoutineWeek.EmptyWeek());
    }

    public void putWeek(int index, RoutineWeek week) {
        this.weeks.set(index, week);
    }

    public void deleteWeek(int weekIndex) {
        this.weeks.remove(weekIndex);
    }

    public RoutineDay getDay(int weekIndex, int dayIndex) {
        return this.weeks.get(weekIndex).getDay(dayIndex);
    }

    public void appendEmptyDay(int weekIndex) {
        RoutineDay routineDay = new RoutineDay();
        if (this.getWeeks().get(weekIndex) == null) {
            // week with this index doesn't exist yet, so create it before appending the day
            this.addWeek(new RoutineWeek());
        }
        this.getWeek(weekIndex).addDay(routineDay);
    }

    public void appendDay(int weekIndex, RoutineDay day) {
        if (this.getWeeks().get(weekIndex) == null) {
            // week with this index doesn't exist yet, so create it before appending the day
            this.addWeek(new RoutineWeek());
        }
        this.getWeek(weekIndex).addDay(day);
    }

    public void putDay(int weekIndex, int dayIndex, RoutineDay day) {
        this.getWeek(weekIndex).putDay(dayIndex, day);
    }

    public void deleteDay(int weekIndex, int dayIndex) {
        this.getWeek(weekIndex).deleteDay(dayIndex);
    }


    public void sortDay(int weekIndex, int dayIndex, int sortMode, Map<String, String> idToName) {
        this.getDay(weekIndex, dayIndex).sortDay(sortMode, idToName);
    }


    public List<RoutineExercise> getExerciseListForDay(int weekPosition, int dayPosition) {
        return new ArrayList<>(this.weeks.get(weekPosition).getDay(dayPosition).getExercises());
    }

    public void addExercise(int week, int day, final RoutineExercise routineExercise) {
        this.getDay(week, day).insertNewExercise(routineExercise);
    }

    public void removeExercise(int week, int day, String exerciseId) {
        this.getDay(week, day).deleteExercise(exerciseId);
    }

    public void deleteExerciseFromRoutine(final String exerciseId) {
        for (RoutineWeek week : this) {
            for (RoutineDay day : week) {
                day.deleteExercise(exerciseId);
            }
        }
    }

    public void swapExerciseOrder(int week, int day, int fromPosition, int toPosition) {
        this.getDay(week, day).swapExerciseOrder(fromPosition, toPosition);
    }

    public void swapDaysOrder(int week, int fromPosition, int toPosition) {
        Collections.swap(this.getWeek(week).getDays(), fromPosition, toPosition);
    }

    public void swapWeeksOrder(int fromPosition, int toPosition) {
        Collections.swap(this.getWeeks(), fromPosition, toPosition);
    }

    public int getWeekIndexOfDay(RoutineDay day) {
        int weekPosition = -1;
        for (int weekIndex = 0; weekIndex < this.getNumberOfWeeks(); weekIndex++) {
            RoutineWeek week = this.getWeek(weekIndex);
            boolean found = false;
            for (RoutineDay day1 : week) {
                if (day1 == day) {
                    found = true;
                    break;
                }
            }
            if (found) {
                weekPosition = weekIndex;
                break;
            }
        }
        return weekPosition;
    }

    public int getNumberOfWeeks() {
        return this.weeks.size();
    }

    public int getTotalNumberOfDays() {
        int days = 0;
        for (RoutineWeek week : this) {
            days += week.getNumberOfDays();
        }
        return days;
    }

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> retVal = new HashMap<>();
        List<Object> jsonWeeks = new ArrayList<>();
        for (RoutineWeek week : this) {
            jsonWeeks.add(week.asMap());
        }
        retVal.put(WEEKS, jsonWeeks);
        return retVal;
    }

    @NonNull
    @Override
    public Iterator<RoutineWeek> iterator() {
        return this.weeks.iterator();
    }
}
