package com.joshrap.liteweight.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.joshrap.liteweight.R;
import com.joshrap.liteweight.activities.MainActivity;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.managers.UserManager;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.UserPreferences;
import com.joshrap.liteweight.providers.UserAndWorkoutProvider;
import com.joshrap.liteweight.utils.AndroidUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import static android.os.Looper.getMainLooper;

public class AccountPreferencesFragment extends Fragment {

    private UserPreferences userPreferences;
    private boolean metricChanged, privateChanged, saveChanged, restartChanged;

    @Inject
    UserManager userManager;
    @Inject
    UserAndWorkoutProvider userAndWorkoutProvider;
    private SwitchCompat privateSwitch, metricSwitch, updateOnSaveSwitch, updateOnRestartSwitch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        ((MainActivity) getActivity()).updateToolbarTitle(Variables.ACCOUNT_PREFS_TITLE);
        ((MainActivity) getActivity()).toggleBackButton(true);
        Injector.getInjector(getContext()).inject(this);
        View view = inflater.inflate(R.layout.fragment_account_preferences, container, false);

        userPreferences = new UserPreferences(userAndWorkoutProvider.provideUserAndWorkout().getUser().getUserPreferences());
        metricChanged = false;
        privateChanged = false;
        saveChanged = false;
        restartChanged = false;

        privateSwitch = view.findViewById(R.id.private_account_switch);
        metricSwitch = view.findViewById(R.id.metric_switch);
        updateOnSaveSwitch = view.findViewById(R.id.update_on_save_switch);
        updateOnRestartSwitch = view.findViewById(R.id.update_on_restart_switch);

        LinearLayout metricLayout = view.findViewById(R.id.metric_container);
        metricLayout.setOnClickListener(view1 -> metricSwitch.performClick());
        metricSwitch.setChecked(userPreferences.isMetricUnits());
        metricSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> metricChanged = isChecked != userPreferences.isMetricUnits());

        LinearLayout privateLayout = view.findViewById(R.id.private_account_container);
        privateLayout.setOnClickListener(view1 -> privateSwitch.performClick());
        privateSwitch.setChecked(userPreferences.isPrivateAccount());
        privateSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> privateChanged = isChecked != userPreferences.isPrivateAccount());

        LinearLayout updateOnRestartLayout = view.findViewById(R.id.update_on_restart_container);
        updateOnRestartLayout.setOnClickListener(view1 -> updateOnRestartSwitch.performClick());
        updateOnRestartSwitch.setChecked(userPreferences.isUpdateDefaultWeightOnRestart());
        updateOnRestartSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> restartChanged = isChecked != userPreferences.isUpdateDefaultWeightOnRestart());

        LinearLayout updateOnSaveLayout = view.findViewById(R.id.update_on_save_container);
        updateOnSaveLayout.setOnClickListener(view1 -> updateOnSaveSwitch.performClick());
        updateOnSaveSwitch.setChecked(userPreferences.isUpdateDefaultWeightOnSave());
        updateOnSaveSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> saveChanged = isChecked != userPreferences.isUpdateDefaultWeightOnSave());
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (metricChanged || privateChanged || restartChanged || saveChanged) {
            // one or more settings has changed, so update it in the DB and the local user settings
            userPreferences.setMetricUnits(metricSwitch.isChecked());
            userPreferences.setPrivateAccount(privateSwitch.isChecked());
            userPreferences.setUpdateDefaultWeightOnSave(updateOnSaveSwitch.isChecked());
            userPreferences.setUpdateDefaultWeightOnRestart(updateOnRestartSwitch.isChecked());

            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                ResultStatus<String> resultStatus = this.userManager.updateUserPreferences(userPreferences);
                Handler handler = new Handler(getMainLooper());
                handler.post(() -> {
                    if (resultStatus.isFailure()) {
                        AndroidUtils.showErrorDialog(resultStatus.getErrorMessage(), getContext());
                    }
                });
            });
        }
    }
}
