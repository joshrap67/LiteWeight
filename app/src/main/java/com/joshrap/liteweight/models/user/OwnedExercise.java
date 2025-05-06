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
        this.links = ownedExercise.getLinks().stream().map(Link::new).collect(Collectors.toList());
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

    public static boolean exercisesDifferent(OwnedExercise exercise1, OwnedExercise exercise2) {
        // these are the only fields that matter for comparison. can modify in the future if necessary to include id/workout list
        if (!exercise1.getName().equals(exercise2.getName())) {
            return true;
        } else if (exercise1.getDefaultWeight() != exercise2.getDefaultWeight()) {
            return true;
        } else if (exercise1.getDefaultSets() != exercise2.getDefaultSets()) {
            return true;
        } else if (exercise1.getDefaultReps() != exercise2.getDefaultReps()) {
            return true;
        } else if (!exercise1.getNotes().equals(exercise2.getNotes())) {
            return true;
        } else if (!focusesEqual(exercise1.getFocuses(), exercise2.getFocuses())) {
            return true;
        } else if (!linksEqual(exercise1.getLinks(), exercise2.getLinks())) {
            return true;
        }

        return false;
    }

    public static boolean focusesEqual(List<String> list1, List<String> list2) {
        if (list1.size() < list2.size()) {
            return false;
        }

        if (list1.size() > list2.size()) {
            return false;
        }

        boolean retVal = true;
        for (String val : list1) {
            // order doesn't matter for focuses
            boolean found = list2.stream().anyMatch(x -> x.equals(val));
            if (!found) {
                retVal = false;
            }
        }
        return retVal;
    }

    public static boolean linksEqual(List<Link> list1, List<Link> list2) {
        if (list1.size() < list2.size()) {
            return false;
        }

        if (list1.size() > list2.size()) {
            return false;
        }

        boolean retVal = true;
        for (int i = 0; i < list1.size(); i++) {
            // order doesn't matter
            Link val1 = list1.get(i);
            Link val2 = list2.get(i);
            if (!Link.linksEqual(val1, val2)) {
                retVal = false;
            }
        }
        return retVal;
    }

    @Override
    public int compareTo(OwnedExercise o) {
        return this.getName().toLowerCase().compareTo(o.getName().toLowerCase());
    }
}

