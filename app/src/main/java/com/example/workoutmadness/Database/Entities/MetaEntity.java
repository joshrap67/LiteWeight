package com.example.workoutmadness.Database.Entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;


@Entity(tableName = "meta_table")
public class MetaEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String workoutName;
    private int currentDay;
    private int totalDays;
    private String dateLast;
    private String dateCreated;
    private int timesCompleted;
    private double percentageExercisesCompleted;
    private boolean currentWorkout;

    public MetaEntity(String workoutName, int currentDay, int totalDays, String dateLast, String dateCreated, int timesCompleted,
                      double percentageExercisesCompleted, boolean currentWorkout) {
        this.workoutName = workoutName;
        this.currentDay = currentDay;
        this.dateLast = dateLast;
        this.dateCreated = dateCreated;
        this.timesCompleted = timesCompleted;
        this.percentageExercisesCompleted = percentageExercisesCompleted;
        this.currentWorkout = currentWorkout;
        this.totalDays = totalDays;
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

    public boolean getCurrentWorkout() {
        return currentWorkout;
    }

    public int getCurrentDay() {
        return currentDay;
    }

    public void setCurrentDay(int currentDay) {
        this.currentDay = currentDay;
    }

    public int getTotalDays() {
        return totalDays;
    }

    @Override
    public String toString(){
        return "Id:"+getId()+" Workout: "+workoutName+" CurrentDay: "+currentDay+" TotalDays: "+totalDays+" DateLast: "+dateLast+
                " DateCreated: "+dateCreated+ " TimesCompleted: "+timesCompleted+ "Percentage "+
                percentageExercisesCompleted+" CurrentWorkout: "+currentWorkout;
    }

}
