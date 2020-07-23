package com.joshrap.liteweight.models;

public class ExerciseRoutine {
    private String exerciseName;
    private double weight;
    private boolean extraShown; // used for the list view
    private int reps;
    private int sets;
    private String note;
    // TODO need to handle change in urls lmao

    public ExerciseRoutine(String exerciseName, double weight, boolean extraShown, int reps, int sets, String note) {
        this.exerciseName = exerciseName;
        this.weight = weight;
        this.extraShown = extraShown;
        this.reps = reps;
        this.sets = sets;
        this.note = note;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public boolean isExtraShown() {
        return extraShown;
    }

    public void setExtraShown(boolean extraShown) {
        this.extraShown = extraShown;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public int getSets() {
        return sets;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
