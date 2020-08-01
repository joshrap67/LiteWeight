package com.joshrap.liteweight.injection;

import com.joshrap.liteweight.fragments.CurrentWorkoutFragment;

import dagger.Component;

@Component
public interface LiteWeightComponent {

    void inject(CurrentWorkoutFragment currentWorkoutFragment);
}
