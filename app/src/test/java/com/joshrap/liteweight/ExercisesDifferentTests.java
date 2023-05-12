package com.joshrap.liteweight;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.joshrap.liteweight.helpers.RoutineExerciseBuilder;
import com.joshrap.liteweight.models.workout.RoutineExercise;

import org.junit.Test;

public class ExercisesDifferentTests {

    @Test
    public void exercises_differ_by_completed() {
        RoutineExercise exercise1 = new RoutineExerciseBuilder()
                .withCompleted(true)
                .build();
        RoutineExercise exercise2 = new RoutineExerciseBuilder()
                .withCompleted(false)
                .build();

        boolean doExercisesDiffer = RoutineExercise.exercisesDifferent(exercise1, exercise2);
        assertTrue("Exercises differ by completed value", doExercisesDiffer);
    }

    @Test
    public void exercises_differ_by_id() {
        RoutineExercise exercise1 = new RoutineExerciseBuilder()
                .withCompleted(true)
                .build();
        RoutineExercise exercise2 = new RoutineExerciseBuilder()
                .withCompleted(true)
                .build();

        boolean doExercisesDiffer = RoutineExercise.exercisesDifferent(exercise1, exercise2);
        assertTrue("Exercises differ by id", doExercisesDiffer);
    }

    @Test
    public void exercises_differ_by_weight() {
        RoutineExercise exercise1 = new RoutineExerciseBuilder()
                .withCompleted(true)
                .withExerciseId("exerciseId")
                .build();
        RoutineExercise exercise2 = new RoutineExerciseBuilder()
                .withCompleted(true)
                .withExerciseId("exerciseId")
                .build();

        boolean doExercisesDiffer = RoutineExercise.exercisesDifferent(exercise1, exercise2);
        assertTrue("Exercises differ by weight", doExercisesDiffer);
    }

    @Test
    public void exercises_differ_by_sets() {
        RoutineExercise exercise1 = new RoutineExerciseBuilder()
                .withCompleted(true)
                .withExerciseId("exerciseId")
                .withWeight(24.0)
                .build();
        RoutineExercise exercise2 = new RoutineExerciseBuilder()
                .withCompleted(true)
                .withExerciseId("exerciseId")
                .withWeight(24.0)
                .build();

        boolean doExercisesDiffer = RoutineExercise.exercisesDifferent(exercise1, exercise2);
        assertTrue("Exercises differ by sets", doExercisesDiffer);
    }

    @Test
    public void exercises_differ_by_reps() {
        RoutineExercise exercise1 = new RoutineExerciseBuilder()
                .withCompleted(true)
                .withExerciseId("exerciseId")
                .withWeight(24.0)
                .withSets(3)
                .build();
        RoutineExercise exercise2 = new RoutineExerciseBuilder()
                .withCompleted(true)
                .withExerciseId("exerciseId")
                .withWeight(24.0)
                .withSets(3)
                .build();

        boolean doExercisesDiffer = RoutineExercise.exercisesDifferent(exercise1, exercise2);
        assertTrue("Exercises differ by reps", doExercisesDiffer);
    }

    @Test
    public void exercises_differ_by_details() {
        RoutineExercise exercise1 = new RoutineExerciseBuilder()
                .withCompleted(true)
                .withExerciseId("exerciseId")
                .withWeight(24.0)
                .withSets(3)
                .withReps(15)
                .build();
        RoutineExercise exercise2 = new RoutineExerciseBuilder()
                .withCompleted(true)
                .withExerciseId("exerciseId")
                .withWeight(24.0)
                .withSets(3)
                .withReps(15)
                .build();

        boolean doExercisesDiffer = RoutineExercise.exercisesDifferent(exercise1, exercise2);
        assertTrue("Exercises differ by details", doExercisesDiffer);
    }

    @Test
    public void exercises_identical() {
        RoutineExercise exercise1 = new RoutineExerciseBuilder()
                .withCompleted(true)
                .withExerciseId("exerciseId")
                .withWeight(24.0)
                .withSets(3)
                .withReps(15)
                .withDetails("Details")
                .build();
        RoutineExercise exercise2 = new RoutineExerciseBuilder()
                .withCompleted(true)
                .withExerciseId("exerciseId")
                .withWeight(24.0)
                .withSets(3)
                .withReps(15)
                .withDetails("Details")
                .build();

        boolean doExercisesDiffer = RoutineExercise.exercisesDifferent(exercise1, exercise2);
        assertFalse("Exercises identical", doExercisesDiffer);
    }
}