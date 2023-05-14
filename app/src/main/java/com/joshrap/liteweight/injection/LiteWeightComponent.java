package com.joshrap.liteweight.injection;

import com.joshrap.liteweight.activities.NewAccountActivity;
import com.joshrap.liteweight.activities.LandingActivity;
import com.joshrap.liteweight.activities.ResetPasswordActivity;
import com.joshrap.liteweight.activities.MainActivity;
import com.joshrap.liteweight.activities.SignInActivity;
import com.joshrap.liteweight.activities.UnverifiedActivity;
import com.joshrap.liteweight.fragments.AboutFragment;
import com.joshrap.liteweight.fragments.AccountPreferencesFragment;
import com.joshrap.liteweight.fragments.ClockBottomFragment;
import com.joshrap.liteweight.fragments.PendingWorkoutFragment;
import com.joshrap.liteweight.fragments.CurrentWorkoutFragment;
import com.joshrap.liteweight.fragments.AppSettingsFragment;
import com.joshrap.liteweight.fragments.BrowseReceivedWorkoutFragment;
import com.joshrap.liteweight.fragments.ExerciseDetailsFragment;
import com.joshrap.liteweight.fragments.FriendsListFragment;
import com.joshrap.liteweight.fragments.MyAccountFragment;
import com.joshrap.liteweight.fragments.MyExercisesFragment;
import com.joshrap.liteweight.fragments.MyWorkoutsFragment;
import com.joshrap.liteweight.fragments.NewExerciseFragment;
import com.joshrap.liteweight.fragments.ReceivedWorkoutsFragment;
import com.joshrap.liteweight.services.MyFirebaseMessagingService;
import com.joshrap.liteweight.services.SyncWorkoutService;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = LiteWeightModule.class)
public interface LiteWeightComponent {

    void inject(CurrentWorkoutFragment currentWorkoutFragment);

    void inject(MainActivity mainActivity);

    void inject(LandingActivity landingActivity);

    void inject(SignInActivity signInActivity);

    void inject(ExerciseDetailsFragment exerciseDetailsFragment);

    void inject(NewExerciseFragment exerciseDetailsFragment);
    
    void inject(MyExercisesFragment myExercisesFragment);

    void inject(MyWorkoutsFragment myWorkoutsFragment);

    void inject(MyAccountFragment myAccountFragment);

    void inject(FriendsListFragment friendsListFragment);

    void inject(AccountPreferencesFragment accountPreferencesFragment);

    void inject(AppSettingsFragment appSettingsFragment);

    void inject(ReceivedWorkoutsFragment receivedWorkoutsFragment);

    void inject(BrowseReceivedWorkoutFragment browseReceivedWorkoutFragment);

    void inject(AboutFragment aboutFragment);

    void inject(PendingWorkoutFragment blankFragment);

    void inject(ClockBottomFragment clockBottomFragment);

    void inject(SyncWorkoutService syncWorkoutService);

    void inject(ResetPasswordActivity resetPasswordActivity);

    void inject(NewAccountActivity newAccountActivity);

    void inject(MyFirebaseMessagingService myFirebaseMessagingService);

    void inject(UnverifiedActivity unverifiedActivity);
}
