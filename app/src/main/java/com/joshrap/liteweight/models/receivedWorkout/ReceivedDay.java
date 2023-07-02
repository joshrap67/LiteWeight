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
public class ReceivedDay implements Iterable<ReceivedExercise> {

    private List<ReceivedExercise> exercises = new ArrayList<>();
    private String tag;

    @NonNull
    @Override
    public Iterator<ReceivedExercise> iterator() {
        return this.exercises.iterator();
    }
}
