package com.joshrap.liteweight.database.viewModels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import com.joshrap.liteweight.database.entities.*;
import com.joshrap.liteweight.database.repositories.*;

import java.util.ArrayList;

public class ExerciseViewModel extends AndroidViewModel {
    private WorkoutRepository repository;

    public ExerciseViewModel(@NonNull Application application) {
        super(application);
        repository = new WorkoutRepository(application);
    }

    public long insert(ExerciseEntity entity) {
        return repository.insertExerciseEntity(entity);
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

    public ArrayList<ExerciseEntity> getAllExercises() {
        return new ArrayList<>(repository.getAllExercises());
    }
}
