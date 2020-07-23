package com.joshrap.liteweight.database.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.joshrap.liteweight.database.entities.*;

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

    @Query("UPDATE meta_table SET workoutName=:newName WHERE workoutName=:oldName")
    void updateWorkoutName(String oldName, String newName);
}