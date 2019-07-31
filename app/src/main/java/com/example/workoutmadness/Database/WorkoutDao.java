package com.example.workoutmadness.Database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import static android.icu.text.MessagePattern.ArgType.SELECT;

@Dao
public interface WorkoutDao {
    @Insert
    void insert(WorkoutEntity entity);
    @Update
    void update(WorkoutEntity entity);
    @Delete
    void delete(WorkoutEntity entity);
    @Query("DELETE FROM workout_table")
    void deleteAllWorkouts();
    @Query("SELECT * FROM workout_table")
    LiveData<List<WorkoutEntity>> getAllWorkouts();
    // todo add query by day and workout name
    // todo add query by unique workout name
}
