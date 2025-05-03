package com.joshrap.liteweight.models.user;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OwnedExercise implements Comparable<OwnedExercise> {

    private String id;
    private String name;
    private double defaultWeight; // stored in lbs
    private int defaultSets;
    private int defaultReps;
    private String notes;
    private List<Link> links = new ArrayList<>();
    private List<String> focuses = new ArrayList<>();
    private List<OwnedExerciseWorkout> workouts = new ArrayList<>();

    public OwnedExercise(OwnedExercise ownedExercise) {
        this.id = ownedExercise.getId();
        this.name = ownedExercise.getName();
        this.defaultWeight = ownedExercise.getDefaultWeight();
        this.defaultSets = ownedExercise.getDefaultSets();
        this.defaultReps = ownedExercise.getDefaultReps();
        this.notes = ownedExercise.getNotes();
        this.links = ownedExercise.getLinks();
        this.focuses = new ArrayList<>(ownedExercise.getFocuses());
        this.workouts = ownedExercise.getWorkouts().stream().map(OwnedExerciseWorkout::new).collect(Collectors.toList());
    }

    public void update(OwnedExercise ownedExercise) {
        this.name = ownedExercise.name;
        this.defaultWeight = ownedExercise.defaultWeight;
        this.defaultSets = ownedExercise.defaultSets;
        this.defaultReps = ownedExercise.defaultReps;
        this.notes = ownedExercise.notes;
        this.focuses = ownedExercise.focuses;
        this.links = ownedExercise.links;
    }

    public void removeWorkout(String workoutId) {
        this.workouts.removeIf(x -> x.getWorkoutId().equals(workoutId));
    }

    public void updateWorkoutName(String workoutId, String newName) {
        this.workouts.stream().filter(x -> x.getWorkoutId().equals(workoutId)).findFirst().ifPresent(workout -> workout.setWorkoutName(newName));
    }

    @Override
    public int compareTo(OwnedExercise o) {
        return this.getName().toLowerCase().compareTo(o.getName().toLowerCase());
    }
}

