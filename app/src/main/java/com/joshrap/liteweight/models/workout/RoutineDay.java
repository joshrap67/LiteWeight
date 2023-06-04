package com.joshrap.liteweight.models.workout;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoutineDay implements Iterable<RoutineExercise> {
    public static final int alphabeticalSortAscending = 0;
    public static final int alphabeticalSortDescending = 1;
    public static final int weightSortAscending = 3;
    public static final int weightSortDescending = 4;
    public static final int customSort = 5;

    private List<RoutineExercise> exercises = new ArrayList<>();
    private String tag;

    @NonNull
    public RoutineDay clone() {
        RoutineDay retVal = new RoutineDay();
        retVal.setTag(this.tag);
        for (RoutineExercise routineExercise : this.exercises) {
            RoutineExercise specificExerciseCloned = new RoutineExercise(routineExercise);
            retVal.getExercises().add(specificExerciseCloned);
        }
        return retVal;
    }

    void insertNewExercise(RoutineExercise routineExercise) {
        this.exercises.add(routineExercise);
    }

    public int totalNumberOfExercises() {
        return this.exercises.size();
    }

    void sortDay(int sortMode, Map<String, String> idToName) {
        if (sortMode == alphabeticalSortAscending) {
            this.exercises.sort(Comparator.comparing(o -> idToName.get(o.getExerciseId())));
        } else if (sortMode == alphabeticalSortDescending) {
            this.exercises.sort((o1, o2) -> idToName.get(o2.getExerciseId()).compareTo(idToName.get(o1.getExerciseId())));
        } else if (sortMode == weightSortAscending) {
            this.exercises.sort(Comparator.comparingDouble(RoutineExercise::getWeight));
        } else if (sortMode == weightSortDescending) {
            this.exercises.sort((o1, o2) -> Double.compare(o2.getWeight(), o1.getWeight()));
        }
    }

    void swapExerciseOrder(int i, int j) {
        Collections.swap(this.exercises, i, j);
    }

    public void deleteExercise(String exerciseId) {
        this.exercises.removeIf(x -> (x.getExerciseId().equals(exerciseId)));
    }

    @NonNull
    @Override
    public Iterator<RoutineExercise> iterator() {
        return this.exercises.iterator();
    }
}
