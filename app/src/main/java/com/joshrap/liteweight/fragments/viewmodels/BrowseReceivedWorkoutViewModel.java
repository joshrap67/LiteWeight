package com.joshrap.liteweight.fragments.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.AndroidViewModel;

import com.joshrap.liteweight.managers.CurrentUserAndWorkoutProvider;
import com.joshrap.liteweight.managers.SharedWorkoutManager;
import com.joshrap.liteweight.managers.UserManager;
import com.joshrap.liteweight.managers.WorkoutManager;
import com.joshrap.liteweight.models.sharedWorkout.SharedRoutine;
import com.joshrap.liteweight.models.sharedWorkout.SharedWorkout;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class BrowseReceivedWorkoutViewModel extends AndroidViewModel {

    private SharedWorkout sharedWorkout;
    private SharedRoutine sharedRoutine;
    private String workoutName;
    private int currentDayIndex;
    private int currentWeekIndex;
    private AlertDialog alertDialog;
    private String receivedWorkoutId;
    private boolean isMetricUnits;
    private final List<String> existingWorkoutNames = new ArrayList<>();

    @Inject
    WorkoutManager workoutManager;
    @Inject
    SharedWorkoutManager sharedWorkoutManager;
    @Inject
    UserManager userManager;
    @Inject
    CurrentUserAndWorkoutProvider currentUserAndWorkoutProvider;

    public BrowseReceivedWorkoutViewModel(@NonNull Application application) {
        super(application);
    }
}
