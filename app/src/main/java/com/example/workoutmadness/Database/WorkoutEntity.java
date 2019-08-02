package com.example.workoutmadness.Database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "workout_table")
public class WorkoutEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String exercise;
    private String workout;
    private int day;
    private boolean status;

    public WorkoutEntity(String exercise, String workout, int day, boolean status) {
        this.exercise = exercise;
        this.workout=workout;
        this.day = day;
        this.status=status;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getWorkout() {
        return workout;
    }

    public boolean isStatus() {
        return status;
    }

    public String getExercise() {
        return exercise;
    }

    public int getDay() {
        return day;
    }

    @Override
    public String toString(){
        /*
            Great for debugging
         */
        return "Workout: "+this.workout+" Exercise: "+this.exercise+" Day: "+this.day+" Status: "+this.status;
    }
}
