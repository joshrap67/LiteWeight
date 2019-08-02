package com.example.workoutmadness.Database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {WorkoutEntity.class, LogEntity.class},version = 2,exportSchema = false)
public abstract class WorkoutDatabase extends RoomDatabase {
    private static WorkoutDatabase instance;

    public abstract WorkoutDao workoutDao();
    public abstract LogDao logDao();

    public static synchronized WorkoutDatabase getInstance(Context context){
        if(instance==null){
            instance = Room.databaseBuilder(context.getApplicationContext(),WorkoutDatabase.class,"workout_db")
                    .fallbackToDestructiveMigration().build();
        }
        return instance;
    }
}
