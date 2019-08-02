package com.example.workoutmadness.Database;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.List;

public class LogViewModel extends AndroidViewModel {
    private WorkoutRepository repository;
    private LiveData<List<LogEntity>> allLogs;
    private MutableLiveData<String> currentWorkout = new MutableLiveData<>();

    public LogViewModel(@NonNull Application application) {
        super(application);
        repository = new WorkoutRepository(application);
        allLogs = repository.getAllLogs();

    }

    public void insert(LogEntity entity) {
        repository.insertLogEntity(entity);
    }

    public void update(LogEntity entity) {
        repository.updateLogEntity(entity);
    }

    public void delete(LogEntity entity) {
        repository.deleteLogEntity(entity);
    }

    public void deleteAllLogs() {
        repository.deleteAllLogs();
    }

    public LiveData<List<LogEntity>> getAllLogs() {
        return allLogs;
    }

    public LiveData<String> getCurrentWorkout(){
        repository.getCurrentWorkout();
        return currentWorkout;
    }
}
