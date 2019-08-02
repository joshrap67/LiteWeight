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
public interface LogDao {
    @Insert
    void insert(LogEntity entity);
    @Update
    void update(LogEntity entity);
    @Delete
    void delete(LogEntity entity);
    @Query("DELETE FROM log_table")
    void deleteAllLogs();
    @Query("SELECT * FROM log_table")
    LiveData<List<LogEntity>> getAllLogs();
    @Query("SELECT workoutName FROM log_table WHERE current='true'")
    String getCurrentWorkout();
}