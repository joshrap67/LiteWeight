package com.joshrap.liteweight.injection;

import com.joshrap.liteweight.activities.SignInActivity;
import com.joshrap.liteweight.activities.SplashActivity;
import com.joshrap.liteweight.activities.WorkoutActivity;
import com.joshrap.liteweight.fragments.ActiveWorkoutFragment;
import com.joshrap.liteweight.fragments.ExerciseDetailsFragment;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = LiteWeightModule.class)
public interface LiteWeightComponent {

    void inject(ActiveWorkoutFragment currentWorkoutFragment);

    void inject(WorkoutActivity workoutActivity);

    void inject(SplashActivity splashActivity);

    void inject(SignInActivity signInActivity);

    void inject(ExerciseDetailsFragment exerciseDetailsFragment);
}
