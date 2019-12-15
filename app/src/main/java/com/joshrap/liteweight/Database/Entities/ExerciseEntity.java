package com.joshrap.liteweight.Database.Entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;


@Entity(tableName = "exercise_table")
public class ExerciseEntity implements Comparable<ExerciseEntity> {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int timesCompleted;
    private String exerciseName;
    private String focus;
    private String url;
    private boolean defaultExercise;
    private double currentWeight;
    private double minWeight;
    private double maxWeight;

    public ExerciseEntity(String exerciseName, String focus, String url, boolean defaultExercise, double currentWeight, double minWeight,
                          double maxWeight, int timesCompleted) {
        this.exerciseName = exerciseName;
        this.focus = focus;
        this.url = url;
        this.defaultExercise = defaultExercise;
        this.currentWeight = currentWeight;
        this.minWeight = minWeight;
        this.maxWeight = maxWeight;
        this.timesCompleted = timesCompleted;
    }

    public int getId() {
        return id;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public String getFocus() {
        return focus;
    }

    public String getUrl() {
        return url;
    }

    public boolean isDefaultExercise() {
        return defaultExercise;
    }

    public double getCurrentWeight() {
        return currentWeight;
    }

    public double getMinWeight() {
        return minWeight;
    }

    public double getMaxWeight() {
        return maxWeight;
    }

    public int getTimesCompleted() {
        return timesCompleted;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public void setCurrentWeight(double currentWeight) {
        this.currentWeight = currentWeight;
    }

    public void setMinWeight(double minWeight) {
        this.minWeight = minWeight;
    }

    public void setTimesCompleted(int timesCompleted) {
        this.timesCompleted = timesCompleted;
    }

    public void setMaxWeight(double maxWeight) {
        this.maxWeight = maxWeight;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "Id:" + getId() + " Exercise: " + exerciseName + " URL: " + url + " defaultExercise: " + defaultExercise +
                " currentWeight: " + currentWeight + " minWeight: " + minWeight + " maxWeight: " + maxWeight + "timesCompleted " +
                timesCompleted;
    }

    @Override
    public int compareTo(ExerciseEntity o) {
        return this.getExerciseName().compareTo(o.getExerciseName());
    }

}
