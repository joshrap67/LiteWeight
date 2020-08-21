package com.joshrap.liteweight.viewmodels;

import android.os.Handler;

import androidx.lifecycle.ViewModel;

import com.joshrap.liteweight.imports.Globals;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.User;
import com.joshrap.liteweight.models.UserWithWorkout;
import com.joshrap.liteweight.models.Workout;
import com.joshrap.liteweight.network.repos.UserRepository;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static android.os.Looper.getMainLooper;

public class SplashViewModel extends ViewModel {
    private UserRepository userRepository;
    private Workout workout;
    private User user;

}
