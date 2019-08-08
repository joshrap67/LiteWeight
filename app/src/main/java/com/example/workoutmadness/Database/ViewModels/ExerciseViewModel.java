package com.example.workoutmadness.Database.ViewModels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import com.example.workoutmadness.Database.Entities.*;
import com.example.workoutmadness.Database.Repositories.*;

import java.util.ArrayList;
import java.util.List;

public class ExerciseViewModel extends AndroidViewModel {
    private WorkoutRepository repository;
    private List<ExerciseEntity> allExercises;
    private String currentWorkout;

    public ExerciseViewModel(@NonNull Application application) {
        super(application);
        repository = new WorkoutRepository(application);
    }

    public void insert(ExerciseEntity entity) {
        repository.insertExerciseEntity(entity);
    }

    public void update(ExerciseEntity entity) {
        repository.updateExerciseEntity(entity);
    }

    public void delete(ExerciseEntity entity) {
        repository.deleteExerciseEntity(entity);
    }

    public void deleteAllExercises() {
        repository.deleteAllExerciseEntities();
    }

    public ArrayList<ExerciseEntity> getAllExercises(){
        return new ArrayList<>(repository.getAllExercises());
    }
}
