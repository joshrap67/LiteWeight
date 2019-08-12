package com.example.workoutmadness.Database.ViewModels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.workoutmadness.Database.Entities.*;
import com.example.workoutmadness.Database.Repositories.*;

import java.util.ArrayList;
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

    public void deleteAllWorkouts() {
        repository.deleteAllWorkouts();
    }

    public void deleteEntireWorkout(String workoutName){
        repository.deleteEntireWorkout(workoutName);
    }

    public void updateExerciseName(String oldName, String newName){
        repository.updateExerciseName(oldName,newName);
    }

    public LiveData<List<WorkoutEntity>> getAllWorkouts() {
        return allWorkouts;
    }

    public ArrayList<WorkoutEntity> getExercises(String workout){
        ArrayList<WorkoutEntity> retList = new ArrayList<>();
        retList.addAll(repository.getExercises(workout));
        return retList;
    }
}
