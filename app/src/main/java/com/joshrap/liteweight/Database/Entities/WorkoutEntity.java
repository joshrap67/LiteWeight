package com.joshrap.liteweight.Database.Entities;

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
        this.workout = workout;
        this.day = day;
        this.status = status;
    }


    public int getId() {
        return id;
    }

    public String getWorkout() {
        return workout;
    }

    public boolean getStatus() {
        return status;
    }

    public String getExercise() {
        return exercise;
    }

    public int getDay() {
        return day;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public void setId(int id) {
        this.id = id;
    }


    @Override
    public String toString() {
        return "Id: " + this.getId() + " Workout: " + this.workout + " Exercise: " + this.exercise +
                " Day: " + this.day + " Status: " + this.status;
    }
}
