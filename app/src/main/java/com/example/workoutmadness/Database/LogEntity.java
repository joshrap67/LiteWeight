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
    private boolean current;

    public LogEntity(String workoutName, int currentDay, String dateLast, String dateCreated, int timesCompleted,
                     double percentageExercisesCompleted, boolean current) {
        this.workoutName = workoutName;
        this.currentDay = currentDay;
        this.dateLast = dateLast;
        this.dateCreated = dateCreated;
        this.timesCompleted = timesCompleted;
        this.percentageExercisesCompleted = percentageExercisesCompleted;
        this.current = current;
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

    public boolean getCurrent() {
        return current;
    }

    public int getCurrentDay() {
        return currentDay;
    }

    @Override
    public String toString(){
        return "Workout: "+workoutName+" CurrentDay: "+currentDay+"DateLast: "+dateLast+" DateCreated: "+dateCreated+
                " TimesCompleted: "+timesCompleted+ "Percentage "+percentageExercisesCompleted+" Current: "+current;
    }

}
