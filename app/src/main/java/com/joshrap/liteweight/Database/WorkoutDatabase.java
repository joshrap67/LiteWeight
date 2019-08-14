package com.joshrap.liteweight.Database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.joshrap.liteweight.Database.Entities.*;
import com.joshrap.liteweight.Database.Daos.*;

@Database(entities = {WorkoutEntity.class, MetaEntity.class, ExerciseEntity.class}, version = 6, exportSchema = false)
public abstract class WorkoutDatabase extends RoomDatabase {
    private static WorkoutDatabase instance;

    public abstract WorkoutDao workoutDao();

    public abstract MetaDao logDao();

    public abstract ExerciseDao exerciseDao();

    public static synchronized WorkoutDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), WorkoutDatabase.class, "workout_db")
                    .fallbackToDestructiveMigration().build();
        }
        return instance;
    }
}
