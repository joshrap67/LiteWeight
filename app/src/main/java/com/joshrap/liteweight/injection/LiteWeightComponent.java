package com.joshrap.liteweight.injection;

import com.joshrap.liteweight.activities.SignInActivity;
import com.joshrap.liteweight.activities.SplashActivity;
import com.joshrap.liteweight.activities.WorkoutActivity;
import com.joshrap.liteweight.fragments.AboutFragment;
import com.joshrap.liteweight.fragments.AccountPreferencesFragment;
import com.joshrap.liteweight.fragments.BlankFragment;
import com.joshrap.liteweight.fragments.CurrentWorkoutFragment;
import com.joshrap.liteweight.fragments.AppSettingsFragment;
import com.joshrap.liteweight.fragments.BlockedListFragment;
import com.joshrap.liteweight.fragments.BrowseReceivedWorkoutFragment;
import com.joshrap.liteweight.fragments.EditWorkoutFragment;
import com.joshrap.liteweight.fragments.ExerciseDetailsFragment;
import com.joshrap.liteweight.fragments.FriendsListFragment;
import com.joshrap.liteweight.fragments.MyAccountFragment;
import com.joshrap.liteweight.fragments.MyExercisesFragment;
import com.joshrap.liteweight.fragments.MyWorkoutsFragment;
import com.joshrap.liteweight.fragments.NewExerciseFragment;
import com.joshrap.liteweight.fragments.NewWorkoutFragment;
import com.joshrap.liteweight.fragments.ReceivedWorkoutsFragment;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = LiteWeightModule.class)
public interface LiteWeightComponent {

    void inject(CurrentWorkoutFragment currentWorkoutFragment);

    void inject(WorkoutActivity workoutActivity);

    void inject(SplashActivity splashActivity);

    void inject(SignInActivity signInActivity);

    void inject(ExerciseDetailsFragment exerciseDetailsFragment);

    void inject(NewExerciseFragment exerciseDetailsFragment);

    void inject(EditWorkoutFragment editWorkoutFragment);

    void inject(MyExercisesFragment myExercisesFragment);

    void inject(NewWorkoutFragment newWorkoutFragment);

    void inject(MyWorkoutsFragment myWorkoutsFragment);

    void inject(MyAccountFragment myAccountFragment);

    void inject(FriendsListFragment friendsListFragment);

    void inject(AccountPreferencesFragment accountPreferencesFragment);

    void inject(AppSettingsFragment appSettingsFragment);

    void inject(BlockedListFragment blockedListFragment);

    void inject(ReceivedWorkoutsFragment receivedWorkoutsFragment);

    void inject(BrowseReceivedWorkoutFragment browseReceivedWorkoutFragment);

    void inject(AboutFragment aboutFragment);

    void inject(BlankFragment blankFragment);
}
