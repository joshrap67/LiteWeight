package com.joshrap.liteweight.database.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.joshrap.liteweight.database.entities.ExerciseEntity;

import java.util.List;

@Dao
public interface ExerciseDao {
    @Insert
    long insert(ExerciseEntity entity);

    @Update
    void update(ExerciseEntity entity);

    @Delete
    int delete(ExerciseEntity entity);

    @Query("DELETE FROM exercise_table")
    void deleteAllExercises();

    @Query("SELECT * FROM exercise_table")
    List<ExerciseEntity> getAllExercises();
}
