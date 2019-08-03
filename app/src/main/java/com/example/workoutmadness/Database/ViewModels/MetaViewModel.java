package com.example.workoutmadness.Database.ViewModels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.example.workoutmadness.Database.Entities.*;
import com.example.workoutmadness.Database.Repositories.*;

import java.util.List;

public class MetaViewModel extends AndroidViewModel {
    private WorkoutRepository repository;
    private LiveData<List<MetaEntity>> allMetadata;
    private String currentWorkout;

    public MetaViewModel(@NonNull Application application) {
        super(application);
        repository = new WorkoutRepository(application);
        allMetadata = repository.getAllMetadata();

    }

    public void insert(MetaEntity entity) {
        repository.insertMetaEntity(entity);
    }

    public void update(MetaEntity entity) {
        repository.updateMetaEntity(entity);
    }

    public void delete(MetaEntity entity) {
        repository.deleteMetaEntity(entity);
    }

    public void deletaAllMeta() {
        repository.deleteAllMetadata();
    }

    public LiveData<List<MetaEntity>> getAllMetadata() {
        return allMetadata;
    }

    public void getCurrentWorkoutMeta(){
        repository.getCurrentWorkoutMeta();
    }

    public MetaEntity getCurrentWorkoutMetaResult(){
        return repository.getCurrentWorkoutMetaResult();
    }
}
