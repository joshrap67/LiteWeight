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
public class SharedDay implements Iterable<SharedExercise> {

    private List<SharedExercise> exercises = new ArrayList<>();
    private String tag;

    @NonNull
    @Override
    public Iterator<SharedExercise> iterator() {
        return this.exercises.iterator();
    }
}
