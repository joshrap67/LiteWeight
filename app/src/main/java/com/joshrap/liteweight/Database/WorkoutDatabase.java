package com.joshrap.liteweight.Database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;

import com.joshrap.liteweight.Database.Entities.*;
import com.joshrap.liteweight.Database.Daos.*;
import com.joshrap.liteweight.Globals.Variables;

@Database(entities = {WorkoutEntity.class, MetaEntity.class, ExerciseEntity.class}, version = 1, exportSchema = false)
public abstract class WorkoutDatabase extends RoomDatabase {
    private static WorkoutDatabase instance;

    public abstract WorkoutDao workoutDao();

    public abstract MetaDao logDao();

    public abstract ExerciseDao exerciseDao();

    public static synchronized WorkoutDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    WorkoutDatabase.class, Variables.DATABASE_NAME)
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration().build();
        }
        return instance;
    }

    // region
    // migrations - whenever the version of the database changes you must add one
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // example of how to do this if necessary
//            database.execSQL("ALTER TABLE workout_table "
//                    + " ADD COLUMN reps INT NOT NULL DEFAULT(0)");
        }
    };
    //endregion
}
