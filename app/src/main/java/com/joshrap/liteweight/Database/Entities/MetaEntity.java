package com.joshrap.liteweight.Database.Entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.joshrap.liteweight.Variables;

import java.text.DateFormat;
import java.text.SimpleDateFormat;


@Entity(tableName = "meta_table")
public class MetaEntity implements Comparable<MetaEntity> {
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
    private String mostFrequentFocus;

    public MetaEntity(String workoutName, int currentDay, int totalDays, String dateLast, String dateCreated, int timesCompleted,
                      double percentageExercisesCompleted, boolean currentWorkout, String mostFrequentFocus) {
        this.workoutName = workoutName;
        this.currentDay = currentDay;
        this.dateLast = dateLast;
        this.dateCreated = dateCreated;
        this.timesCompleted = timesCompleted;
        this.percentageExercisesCompleted = percentageExercisesCompleted;
        this.currentWorkout = currentWorkout;
        this.totalDays = totalDays;
        this.mostFrequentFocus = mostFrequentFocus;
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

    public String getMostFrequentFocus() {
        return mostFrequentFocus;
    }

    public double getPercentageExercisesCompleted() {
        return percentageExercisesCompleted;
    }

    public boolean getCurrentWorkout() {
        return currentWorkout;
    }

    public void setCurrentWorkout(boolean status){
        this.currentWorkout=status;
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

    public void setMostFrequentFocus(String mostFrequentFocus) {
        this.mostFrequentFocus = mostFrequentFocus;
    }

    public void setDateLast(String dateLast) {
        this.dateLast = dateLast;
    }

    public void setTimesCompleted(int timesCompleted) {
        this.timesCompleted = timesCompleted;
    }

    @Override
    public String toString(){
        return "Id:"+getId()+" Workout: "+workoutName+" CurrentDay: "+currentDay+" TotalDays: "+totalDays+" DateLast: "+dateLast+
                " DateCreated: "+dateCreated+ " TimesCompleted: "+timesCompleted+ " Percentage "+
                percentageExercisesCompleted+" CurrentWorkout: "+currentWorkout;
    }

    @Override
    public int compareTo(MetaEntity o) {
        DateFormat df = new SimpleDateFormat(Variables.DATE_PATTERN);
        try {
            return df.parse(dateLast).compareTo(df.parse(o.getDateLast()));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}