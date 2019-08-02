package com.example.workoutmadness.Database;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;

public class WorkoutViewModel extends AndroidViewModel {
    private WorkoutRepository repository;
    private LiveData<List<WorkoutEntity>> allWorkouts;

    public WorkoutViewModel(@NonNull Application application) {
        super(application);
        repository = new WorkoutRepository(application);
        allWorkouts = repository.getAllWorkouts();
    }

    public void insert(WorkoutEntity entity) {
        repository.insertWorkoutEntity(entity);
    }

    public void update(WorkoutEntity entity) {
        repository.updateWorkoutEntity(entity);
    }

    public void delete(WorkoutEntity entity) {
        repository.deleteWorkoutEntity(entity);
    }

    public void deleteAllNotes() {
        repository.deleteAllWorkouts();
    }

    public LiveData<List<WorkoutEntity>> getAllWorkouts() {
        return allWorkouts;
    }
}
