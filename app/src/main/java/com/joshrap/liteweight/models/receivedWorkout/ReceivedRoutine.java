package com.joshrap.liteweight.models.receivedWorkout;

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
public class ReceivedRoutine implements Iterable<ReceivedWeek> {

    private List<ReceivedWeek> weeks = new ArrayList<>();

    public List<ReceivedExercise> getExerciseListForDay(int week, int day) {
        return new ArrayList<>(this.getDay(week, day).getExercises());
    }

    public ReceivedWeek getWeek(int week) {
        return this.weeks.get(week);
    }

    public ReceivedDay getDay(int week, int day) {
        return this.weeks.get(week).getDay(day);
    }

    public int getNumberOfWeeks() {
        return this.weeks.size();
    }

    @NonNull
    @Override
    public Iterator<ReceivedWeek> iterator() {
        return this.weeks.iterator();
    }
}
