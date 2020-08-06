package com.joshrap.liteweight.injection;

import android.content.Context;
import android.content.SharedPreferences;

import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.models.User;
import com.joshrap.liteweight.models.Workout;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class LiteWeightModule {
    private Workout workout;
    private User user;
    private Context context;

    LiteWeightModule(Context context) {
        this.context = context;
    }

    @Provides
    Context provideContext() {
        return this.context;
    }

    @Provides
    @Singleton
    Workout getWorkoutObject() {
        return this.workout;
    }

    @Provides
    @Singleton
    SharedPreferences provideSharedPreference(Context context) {
        return context.getSharedPreferences(Variables.SHARED_PREF_SETTINGS, Context.MODE_PRIVATE);
    }
}
