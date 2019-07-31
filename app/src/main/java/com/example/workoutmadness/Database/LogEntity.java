package com.example.workoutmadness.Database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "log_table")
public class LogEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String workoutName;
    private int currentDay;
    private String dateLast;
    private String dateCreated;
    private int timesCompleted;
    private double percentageExercisesCompleted;

    public LogEntity(String workout, int day, String dateLast, String dateCreated, int timesCompleted,
                     double percentageExercisesCompleted) {
        this.workoutName =workout;
        this.currentDay = day;
        this.dateLast=dateLast;
        this.dateCreated=dateCreated;
        this.timesCompleted=timesCompleted;
        this.percentageExercisesCompleted=percentageExercisesCompleted;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getWorkoutName() {
        return workoutName;
    }

    public String getDateLast() {
        return dateLast;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public int getTimesCompleted() {
        return timesCompleted;
    }

    public double getPercentageExercisesCompleted() {
        return percentageExercisesCompleted;
    }

    public int getCurrentDay() {
        return currentDay;
    }

}
