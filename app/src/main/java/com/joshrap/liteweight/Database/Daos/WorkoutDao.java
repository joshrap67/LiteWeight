package com.joshrap.liteweight.Database.Daos;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import com.joshrap.liteweight.Database.Entities.*;

import java.util.List;

@Dao
public interface WorkoutDao {
    @Insert
    void insert(WorkoutEntity entity);
    @Update
    void update(WorkoutEntity entity);
    @Delete
    void delete(WorkoutEntity entity);
    @Query("DELETE FROM workout_table WHERE workout=:name")
    void deleteEntireWorkout(String name);
    @Query("DELETE FROM workout_table")
    void deleteAllWorkouts();
    @Query("SELECT * FROM workout_table")
    LiveData<List<WorkoutEntity>> getAllWorkouts();
    @Query("SELECT * FROM workout_table WHERE workout=:workoutName")
    List<WorkoutEntity> getExercises(String workoutName);
    @Query("UPDATE workout_table SET exercise=:newName WHERE exercise=:oldName")
    void updateExerciseName(String oldName, String newName);
}
