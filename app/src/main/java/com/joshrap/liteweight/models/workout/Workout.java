package com.joshrap.liteweight.models.workout;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Workout {

    private String id;
    private String name;
    private String creationUtc;
    private String creatorId;
    private Routine routine;

    public Workout(Workout toBeCopied) {
        // copy constructor
        this.id = toBeCopied.getId();
        this.name = toBeCopied.getName();
        this.creationUtc = toBeCopied.getCreationUtc();
        this.creatorId = toBeCopied.getCreatorId();
        this.routine = new Routine(toBeCopied.getRoutine());
    }

    public static boolean workoutsDifferent(Workout workout1, Workout workout2) {
        boolean retVal = false;
        if (workout1 == null || workout2 == null) {
            return true;
        }

        if (Routine.routinesDifferent(workout1.getRoutine(), workout2.getRoutine())) {
            retVal = true;
        }
        return retVal;
    }
}
