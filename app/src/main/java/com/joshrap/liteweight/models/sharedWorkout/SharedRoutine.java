package com.joshrap.liteweight.models.sharedWorkout;

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
public class SharedRoutine implements Iterable<SharedWeek> {

    private List<SharedWeek> weeks = new ArrayList<>();

    public List<SharedExercise> getExerciseListForDay(int week, int day) {
        return new ArrayList<>(this.getDay(week, day).getExercises());
    }

    public SharedWeek getWeek(int week) {
        return this.weeks.get(week);
    }

    public SharedDay getDay(int week, int day) {
        return this.weeks.get(week).getDay(day);
    }

    public int getNumberOfWeeks() {
        return this.weeks.size();
    }

    @NonNull
    @Override
    public Iterator<SharedWeek> iterator() {
        return this.weeks.iterator();
    }
}
