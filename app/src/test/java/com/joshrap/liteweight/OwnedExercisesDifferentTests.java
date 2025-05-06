package com.joshrap.liteweight;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.joshrap.liteweight.helpers.OwnedExerciseBuilder;
import com.joshrap.liteweight.models.user.Link;
import com.joshrap.liteweight.models.user.OwnedExercise;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class OwnedExercisesDifferentTests {

    @Test
    public void exercises_differ_by_name() {
        OwnedExercise exercise1 = new OwnedExerciseBuilder()
                .withExerciseName("A")
                .build();
        OwnedExercise exercise2 = new OwnedExerciseBuilder()
                .withExerciseName("B")
                .build();

        boolean doExercisesDiffer = OwnedExercise.exercisesDifferent(exercise1, exercise2);
        assertTrue("Exercises differ by completed value", doExercisesDiffer);
    }

    @Test
    public void exercises_differ_by_weight() {
        OwnedExercise exercise1 = new OwnedExerciseBuilder()
                .withExerciseName("A")
                .build();
        OwnedExercise exercise2 = new OwnedExerciseBuilder()
                .withExerciseName("A")
                .build();

        boolean doExercisesDiffer = OwnedExercise.exercisesDifferent(exercise1, exercise2);
        assertTrue("Exercises differ by weight", doExercisesDiffer);
    }

    @Test
    public void exercises_differ_by_sets() {
        OwnedExercise exercise1 = new OwnedExerciseBuilder()
                .withExerciseName("A")
                .withDefaultWeight(24.0)
                .build();
        OwnedExercise exercise2 = new OwnedExerciseBuilder()
                .withExerciseName("A")
                .withDefaultWeight(24.0)
                .build();

        boolean doExercisesDiffer = OwnedExercise.exercisesDifferent(exercise1, exercise2);
        assertTrue("Exercises differ by sets", doExercisesDiffer);
    }

    @Test
    public void exercises_differ_by_reps() {
        OwnedExercise exercise1 = new OwnedExerciseBuilder()
                .withExerciseName("A")
                .withDefaultWeight(24.0)
                .withDefaultSets(3)
                .build();
        OwnedExercise exercise2 = new OwnedExerciseBuilder()
                .withExerciseName("A")
                .withDefaultWeight(24.0)
                .withDefaultSets(3)
                .build();

        boolean doExercisesDiffer = OwnedExercise.exercisesDifferent(exercise1, exercise2);
        assertTrue("Exercises differ by reps", doExercisesDiffer);
    }

    @Test
    public void exercises_differ_by_note() {
        OwnedExercise exercise1 = new OwnedExerciseBuilder()
                .withExerciseName("A")
                .withDefaultWeight(24.0)
                .withDefaultSets(3)
                .withDefaultReps(15)
                .build();
        OwnedExercise exercise2 = new OwnedExerciseBuilder()
                .withExerciseName("A")
                .withDefaultWeight(24.0)
                .withDefaultSets(3)
                .withDefaultReps(15)
                .build();

        boolean doExercisesDiffer = OwnedExercise.exercisesDifferent(exercise1, exercise2);
        assertTrue("Exercises differ by notes", doExercisesDiffer);
    }

    @Test
    public void exercises_differ_by_focuses() {
        OwnedExercise exercise1 = new OwnedExerciseBuilder()
                .withExerciseName("A")
                .withDefaultWeight(24.0)
                .withDefaultSets(3)
                .withDefaultReps(15)
                .withNotes("A")
                .build();
        OwnedExercise exercise2 = new OwnedExerciseBuilder()
                .withExerciseName("A")
                .withDefaultWeight(24.0)
                .withDefaultSets(3)
                .withDefaultReps(15)
                .withNotes("A")
                .build();

        boolean doExercisesDiffer = OwnedExercise.exercisesDifferent(exercise1, exercise2);
        assertTrue("Exercises differ by focuses", doExercisesDiffer);
    }

    @Test
    public void exercises_differ_by_links() {
        OwnedExercise exercise1 = new OwnedExerciseBuilder()
                .withExerciseName("A")
                .withDefaultWeight(24.0)
                .withDefaultSets(3)
                .withDefaultReps(15)
                .withNotes("A")
                .withFocuses(List.of("a", "b"))
                .build();
        OwnedExercise exercise2 = new OwnedExerciseBuilder()
                .withExerciseName("A")
                .withDefaultWeight(24.0)
                .withDefaultSets(3)
                .withDefaultReps(15)
                .withNotes("A")
                .withFocuses(List.of("a", "b"))
                .build();

        boolean doExercisesDiffer = OwnedExercise.exercisesDifferent(exercise1, exercise2);
        assertTrue("Exercises differ by links", doExercisesDiffer);
    }


    @Test
    public void exercises_identical() {
        List<Link> links = new ArrayList<>();
        links.add(new Link("a","b"));
        links.add(new Link("c","d"));
        OwnedExercise exercise1 = new OwnedExerciseBuilder()
                .withExerciseName("A")
                .withDefaultWeight(24.0)
                .withDefaultSets(3)
                .withDefaultReps(15)
                .withNotes("A")
                .withFocuses(List.of("a", "b"))
                .withLinks(links)
                .build();
        OwnedExercise exercise2 = new OwnedExerciseBuilder()
                .withExerciseName("A")
                .withDefaultWeight(24.0)
                .withDefaultSets(3)
                .withDefaultReps(15)
                .withNotes("A")
                .withFocuses(List.of("a", "b"))
                .withLinks(links)
                .build();

        boolean doExercisesDiffer = OwnedExercise.exercisesDifferent(exercise1, exercise2);
        assertFalse("Exercises identical", doExercisesDiffer);
    }
}