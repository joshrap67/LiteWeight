package com.joshrap.liteweight.models;

import android.annotation.SuppressLint;

import com.joshrap.liteweight.interfaces.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
@SuppressLint("UseSparseArrays")
public class SharedDay implements Iterable<SharedExercise>, Model {

    public static final String EXERCISES = "exercises";

    private List<SharedExercise> exercises;
    private Integer index;

    public SharedDay() {
        this.exercises = new ArrayList<>();
    }

    public SharedDay(Map<String, Object> json) {
        this.exercises = new ArrayList<>();

        List<Object> exercisesJson = (List<Object>) json.get(EXERCISES);
        for (Object exerciseJson : exercisesJson) {
            SharedExercise routineExercise = new SharedExercise((Map<String, Object>) exerciseJson);
            this.exercises.add(routineExercise);
        }
    }

    public void put(int sortVal, SharedExercise sharedExercise) {
        this.exercises.set(sortVal, sharedExercise);
    }

    @Override
    public Iterator<SharedExercise> iterator() {
        return this.exercises.iterator();
    }

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> retVal = new HashMap<>();
        List<Object> jsonExercises = new ArrayList<>();
        for (SharedExercise exercise : this) {
            jsonExercises.add(exercise.asMap());
        }
        retVal.put(EXERCISES, jsonExercises);
        return retVal;
    }
}
