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
public class ReceivedWeek implements Iterable<ReceivedDay> {

    private List<ReceivedDay> days = new ArrayList<>();

    public int getNumberOfDays() {
        return this.days.size();
    }

    public ReceivedDay getDay(int day) {
        return this.days.get(day);
    }

    @NonNull
    @Override
    public Iterator<ReceivedDay> iterator() {
        return this.days.iterator();
    }

}
