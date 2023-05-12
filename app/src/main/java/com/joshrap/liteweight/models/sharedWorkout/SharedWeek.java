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
public class SharedWeek implements Iterable<SharedDay> {

    private List<SharedDay> days = new ArrayList<>();

    public int getNumberOfDays() {
        return this.days.size();
    }

    public SharedDay getDay(int day) {
        return this.days.get(day);
    }

    @NonNull
    @Override
    public Iterator<SharedDay> iterator() {
        return this.days.iterator();
    }

}
