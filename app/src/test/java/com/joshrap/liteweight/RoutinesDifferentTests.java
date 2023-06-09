package com.joshrap.liteweight;

import org.junit.Test;

import static org.junit.Assert.*;

import com.joshrap.liteweight.helpers.RoutineExerciseBuilder;
import com.joshrap.liteweight.models.workout.Routine;
import com.joshrap.liteweight.models.workout.RoutineDay;
import com.joshrap.liteweight.models.workout.RoutineExercise;

public class RoutinesDifferentTests {

    @Test
    public void routines_differ_total_days_mismatch() {
        Routine routine1 = new Routine();
        routine1.addEmptyWeek();
        routine1.addEmptyWeek();

        Routine routine2 = new Routine();
        routine2.addEmptyWeek();

        boolean doRoutinesDiffer = Routine.routinesDifferent(routine1, routine2);
        assertTrue("Routines differ from total days mismatch", doRoutinesDiffer);
    }

    @Test
    public void routines_differ_week_mismatch() {
        Routine routine1 = new Routine();
        routine1.addEmptyWeek();
        routine1.addEmptyWeek();

        Routine routine2 = new Routine();
        routine2.addEmptyWeek();
        routine2.appendEmptyDay(0);

        boolean doRoutinesDiffer = Routine.routinesDifferent(routine1, routine2);
        assertTrue("Routines differ from week mismatch", doRoutinesDiffer);
    }

    @Test
    public void routines_differ_week_number_of_days_mismatch() {
        Routine routine1 = new Routine();
        routine1.addEmptyWeek();
        routine1.appendEmptyDay(0);
        routine1.addEmptyWeek();

        Routine routine2 = new Routine();
        routine2.addEmptyWeek();
        routine2.addEmptyWeek();
        routine2.appendEmptyDay(1);

        boolean doRoutinesDiffer = Routine.routinesDifferent(routine1, routine2);
        assertTrue("Routines differ from one week having a different number of days than the other", doRoutinesDiffer);
    }

    @Test
    public void routines_differ_day_number_of_exercises_mismatch() {
        Routine routine1 = new Routine();
        routine1.addEmptyWeek();
        routine1.appendEmptyDay(0);
        RoutineExercise exercise1 = new RoutineExerciseBuilder().build();
        RoutineExercise exercise2 = new RoutineExerciseBuilder().build();
        routine1.addExercise(0, 0, exercise1);
        routine1.addExercise(0, 0, exercise2);

        Routine routine2 = new Routine();
        routine2.addEmptyWeek();
        routine2.appendEmptyDay(0);
        RoutineExercise exercise3 = new RoutineExerciseBuilder().build();
        routine2.addExercise(0, 0, exercise3);

        boolean doRoutinesDiffer = Routine.routinesDifferent(routine1, routine2);
        assertTrue("Routines differ from one day having a different number of exercises", doRoutinesDiffer);
    }

    @Test
    public void routines_differ_day_exercise_mismatch() {
        // yeah yeah i know this is why static methods are not good
        Routine routine1 = new Routine();
        routine1.addEmptyWeek();
        routine1.appendEmptyDay(0);
        RoutineExercise exercise1 = new RoutineExerciseBuilder().build();
        RoutineExercise exercise2 = new RoutineExerciseBuilder().build();
        routine1.addExercise(0, 0, exercise1);
        routine1.addExercise(0, 0, exercise2);

        Routine routine2 = new Routine();
        routine2.addEmptyWeek();
        routine2.appendEmptyDay(0);
        RoutineExercise exercise3 = new RoutineExerciseBuilder().build();
        RoutineExercise exercise4 = new RoutineExerciseBuilder().build();
        routine2.addExercise(0, 0, exercise3);
        routine2.addExercise(0, 0, exercise4);

        boolean doRoutinesDiffer = Routine.routinesDifferent(routine1, routine2);
        assertTrue("Routines differ from exercise mismatch", doRoutinesDiffer);
    }

    @Test
    public void routines_differ_day_tag_mismatch() {
        Routine routine1 = new Routine();
        routine1.addEmptyWeek();
        routine1.appendEmptyDay(0);
        RoutineDay routineDay1 = new RoutineDay();
        routineDay1.setTag("A");
        routine1.appendDay(1, routineDay1);
        RoutineExercise exercise1 = new RoutineExerciseBuilder()
                .withCompleted(true)
                .withExerciseId("exerciseId")
                .withWeight(24.0)
                .withSets(3)
                .withReps(15)
                .withDetails("details")
                .build();
        RoutineExercise exercise2 = new RoutineExerciseBuilder()
                .withCompleted(true)
                .withExerciseId("exerciseId")
                .withWeight(24.0)
                .withSets(3)
                .withReps(15)
                .withDetails("details")
                .build();
        routine1.addExercise(0, 0, exercise1);
        routine1.addExercise(0, 0, exercise2);

        Routine routine2 = new Routine();
        routine2.addEmptyWeek();
        RoutineDay routineDay2 = new RoutineDay();
        routineDay2.setTag("B");
        routine2.appendDay(1, routineDay2);
        RoutineExercise exercise3 = new RoutineExerciseBuilder()
                .withCompleted(true)
                .withExerciseId("exerciseId")
                .withWeight(24.0)
                .withSets(3)
                .withReps(15)
                .withDetails("details")
                .build();
        RoutineExercise exercise4 = new RoutineExerciseBuilder()
                .withCompleted(true)
                .withExerciseId("exerciseId")
                .withWeight(24.0)
                .withSets(3)
                .withReps(15)
                .withDetails("details")
                .build();
        routine2.addExercise(0, 0, exercise3);
        routine2.addExercise(0, 0, exercise4);

        boolean doRoutinesDiffer = Routine.routinesDifferent(routine1, routine2);
        assertTrue("Routines differ from exercise mismatch", doRoutinesDiffer);
    }

    @Test
    public void routines_differ_exercise_mismatch_identical_ids() {
        Routine routine1 = new Routine();
        routine1.addEmptyWeek();
        routine1.appendEmptyDay(0);
        RoutineExercise exercise1 = new RoutineExerciseBuilder().build();
        RoutineExercise exercise2 = new RoutineExerciseBuilder().build();
        routine1.addExercise(0, 0, exercise1);
        routine1.addExercise(0, 0, exercise2);

        Routine routine2 = new Routine();
        routine2.addEmptyWeek();
        routine2.appendEmptyDay(0);
        RoutineExercise exercise3 = new RoutineExerciseBuilder()
                .withExerciseId(exercise1.getExerciseId())
                .build();
        RoutineExercise exercise4 = new RoutineExerciseBuilder()
                .withExerciseId(exercise2.getExerciseId())
                .build();
        routine2.addExercise(0, 0, exercise3);
        routine2.addExercise(0, 0, exercise4);

        boolean doRoutinesDiffer = Routine.routinesDifferent(routine1, routine2);
        assertTrue("Routines differ from exercise mismatch", doRoutinesDiffer);
    }

    @Test
    public void routines_identical_empty() {
        Routine routine1 = new Routine();
        Routine routine2 = new Routine();

        boolean doRoutinesDiffer = Routine.routinesDifferent(routine1, routine2);
        assertFalse("Identical empty routines", doRoutinesDiffer);
    }

    @Test
    public void routines_identical() {
        Routine routine1 = new Routine();
        routine1.addEmptyWeek();
        routine1.appendEmptyDay(0);
        RoutineExercise exercise1 = new RoutineExerciseBuilder()
                .withCompleted(true)
                .withExerciseId("exerciseId")
                .withWeight(24.0)
                .withSets(3)
                .withReps(15)
                .withDetails("details")
                .build();
        RoutineExercise exercise2 = new RoutineExerciseBuilder()
                .withCompleted(true)
                .withExerciseId("exerciseId")
                .withWeight(24.0)
                .withSets(3)
                .withReps(15)
                .withDetails("details")
                .build();
        routine1.addExercise(0, 0, exercise1);
        routine1.addExercise(0, 0, exercise2);

        Routine routine2 = new Routine();
        routine2.addEmptyWeek();
        routine2.appendEmptyDay(0);
        RoutineExercise exercise3 = new RoutineExerciseBuilder()
                .withCompleted(true)
                .withExerciseId("exerciseId")
                .withWeight(24.0)
                .withSets(3)
                .withReps(15)
                .withDetails("details")
                .build();
        RoutineExercise exercise4 = new RoutineExerciseBuilder()
                .withCompleted(true)
                .withExerciseId("exerciseId")
                .withWeight(24.0)
                .withSets(3)
                .withReps(15)
                .withDetails("details")
                .build();
        routine2.addExercise(0, 0, exercise3);
        routine2.addExercise(0, 0, exercise4);

        boolean doRoutinesDiffer = Routine.routinesDifferent(routine1, routine2);
        assertFalse("Identical routines", doRoutinesDiffer);
    }
}