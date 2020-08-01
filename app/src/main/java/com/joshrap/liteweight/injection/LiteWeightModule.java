package com.joshrap.liteweight.injection;

import com.joshrap.liteweight.models.User;
import com.joshrap.liteweight.models.Workout;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class LiteWeightModule {
    private Workout workout;
    private User user;

    @Provides
    @Singleton
    public Workout getWorkoutObject(){
        return this.workout;
    }
}
