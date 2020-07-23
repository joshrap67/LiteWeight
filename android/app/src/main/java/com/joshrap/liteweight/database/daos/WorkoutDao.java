package com.joshrap.liteweight.database.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.joshrap.liteweight.database.entities.*;

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

    @Query("DELETE FROM workout_table WHERE exercise=:exerciseName AND workout=:workoutName AND day=:day")
    void deleteSpecificExerciseFromWorkout(String workoutName, String exerciseName, int day);

    @Query("DELETE FROM workout_table WHERE exercise=:exerciseName")
    void deleteExerciseFromWorkouts(String exerciseName);

    @Query("SELECT * FROM workout_table")
    LiveData<List<WorkoutEntity>> getAllWorkouts();

    @Query("SELECT * FROM workout_table WHERE workout=:workoutName")
    List<WorkoutEntity> getExercises(String workoutName);

    @Query("UPDATE workout_table SET exercise=:newName WHERE exercise=:oldName")
    void updateExerciseName(String oldName, String newName);

    @Query("UPDATE workout_table SET workout=:newName WHERE workout=:oldName")
    void updateWorkoutName(String oldName, String newName);
}
