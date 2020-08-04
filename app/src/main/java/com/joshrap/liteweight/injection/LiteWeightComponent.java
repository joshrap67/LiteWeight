package com.joshrap.liteweight.injection;

import com.joshrap.liteweight.fragments.ActiveWorkoutFragment;

import dagger.Component;

@Component
public interface LiteWeightComponent {

    void inject(ActiveWorkoutFragment currentWorkoutFragment);
}
