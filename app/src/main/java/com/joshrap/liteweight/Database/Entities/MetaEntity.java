package com.joshrap.liteweight.Database.Entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.joshrap.liteweight.Globals.Variables;

import java.text.DateFormat;
import java.text.SimpleDateFormat;


@Entity(tableName = "meta_table")
public class MetaEntity implements Comparable<MetaEntity> {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int timesCompleted;
    private int completedSum;
    private int totalSum;
    private int currentDay;
    private int maxDayIndex;
    private int numDays;
    private String workoutName;
    private String dateLast;
    private String dateCreated;
    private String mostFrequentFocus;
    private double percentageExercisesCompleted;
    private boolean currentWorkout;

    public MetaEntity(String workoutName, int currentDay, int maxDayIndex, int numDays, String dateLast, String dateCreated, int timesCompleted,
                      double percentageExercisesCompleted, boolean currentWorkout, String mostFrequentFocus, int completedSum,
                      int totalSum) {
        this.workoutName = workoutName;
        this.currentDay = currentDay;
        this.dateLast = dateLast;
        this.dateCreated = dateCreated;
        this.numDays = numDays;
        this.timesCompleted = timesCompleted;
        this.percentageExercisesCompleted = percentageExercisesCompleted;
        this.currentWorkout = currentWorkout;
        this.maxDayIndex = maxDayIndex;
        this.mostFrequentFocus = mostFrequentFocus;
        this.completedSum = completedSum;
        this.totalSum = totalSum;
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

    public int getNumDays() {
        return numDays;
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

    public int getMaxDayIndex() {
        return maxDayIndex;
    }

    public int getCompletedSum() {
        return completedSum;
    }

    public int getTotalSum() {
        return totalSum;
    }

    public void setCurrentWorkout(boolean status) {
        this.currentWorkout = status;
    }

    public int getCurrentDay() {
        return currentDay;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCurrentDay(int currentDay) {
        this.currentDay = currentDay;
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

    public void setCompletedSum(int completedSum) {
        this.completedSum = completedSum;
    }

    public void setTotalSum(int totalSum) {
        this.totalSum = totalSum;
    }

    public void setPercentageExercisesCompleted(double percentageExercisesCompleted) {
        this.percentageExercisesCompleted = percentageExercisesCompleted;
    }

    @Override
    public String toString() {
        return "Id:" + getId() + " Workout: " + workoutName + " CurrentDay: " + currentDay + " TotalDays: " + maxDayIndex + " DateLast: " + dateLast +
                " DateCreated: " + dateCreated + " TimesCompleted: " + timesCompleted + " Percentage " +
                percentageExercisesCompleted + " CurrentWorkout: " + currentWorkout;
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
