package com.joshrap.liteweight.Database.Daos;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.joshrap.liteweight.Database.Entities.ExerciseEntity;

import java.util.List;

@Dao
public interface ExerciseDao {
    @Insert
    void insert(ExerciseEntity entity);
    @Update
    void update(ExerciseEntity entity);
    @Delete
    void delete(ExerciseEntity entity);
    @Query("DELETE FROM exercise_table")
    void deleteAllExercises();
    @Query("SELECT * FROM exercise_table")
    List<ExerciseEntity> getAllExercises();
}
