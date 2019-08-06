package com.example.workoutmadness.Database.Daos;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import com.example.workoutmadness.Database.Entities.*;

import java.util.List;

@Dao
public interface MetaDao {
    @Insert
    void insert(MetaEntity entity);
    @Update
    void update(MetaEntity entity);
    @Delete
    void delete(MetaEntity entity);
    @Query("DELETE FROM meta_table")
    void deleteAllMetadata();
    @Query("SELECT * FROM meta_table")
    List<MetaEntity> getAllMetadata();
    @Query("SELECT * FROM meta_table WHERE currentWorkout=1")
    MetaEntity getCurrentWorkoutMeta();
}