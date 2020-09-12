package com.joshrap.liteweight.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.joshrap.liteweight.R;
import com.joshrap.liteweight.activities.WorkoutActivity;
import com.joshrap.liteweight.imports.Globals;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.UserPreferences;
import com.joshrap.liteweight.network.repos.UserRepository;
import com.joshrap.liteweight.widgets.ErrorDialog;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import static android.os.Looper.getMainLooper;

public class AccountPreferencesFragment extends Fragment {

    private UserPreferences userPreferences;
    private boolean metricChanged, privateChanged, saveChanged, restartChanged;
    @Inject
    UserRepository userRepository;
    private SwitchCompat privateSwitch, metricSwitch, updateOnSaveSwitch, updateOnRestartSwitch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((WorkoutActivity) getActivity()).updateToolbarTitle(Variables.ACCOUNT_PREFS_TITLE);
        ((WorkoutActivity) getActivity()).toggleBackButton(true);
        Injector.getInjector(getContext()).inject(this);
        View view = inflater.inflate(R.layout.fragment_account_preferences, container, false);
        userPreferences = Globals.user.getUserPreferences();
        metricChanged = false;
        privateChanged = false;
        saveChanged = false;
        restartChanged = false;

        privateSwitch = view.findViewById(R.id.private_switch);
        metricSwitch = view.findViewById(R.id.metric_switch);
        updateOnSaveSwitch = view.findViewById(R.id.update_on_save_switch);
        updateOnRestartSwitch = view.findViewById(R.id.update_on_restart_switch);

        LinearLayout metricLayout = view.findViewById(R.id.metric_layout);
        metricLayout.setOnClickListener(view1 -> metricSwitch.performClick());
        metricSwitch.setChecked(userPreferences.isMetricUnits());
        metricSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> metricChanged = isChecked != userPreferences.isMetricUnits());

        LinearLayout privateLayout = view.findViewById(R.id.private_layout);
        privateLayout.setOnClickListener(view1 -> privateSwitch.performClick());
        privateSwitch.setChecked(userPreferences.isPrivateAccount());
        privateSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> privateChanged = isChecked != userPreferences.isPrivateAccount());

        LinearLayout updateOnRestartLayout = view.findViewById(R.id.update_on_restart_layout);
        updateOnRestartLayout.setOnClickListener(view1 -> updateOnRestartSwitch.performClick()); //todo change to get rid of the sound?
        updateOnRestartSwitch.setChecked(userPreferences.isUpdateDefaultWeightOnRestart());
        updateOnRestartSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> restartChanged = isChecked != userPreferences.isUpdateDefaultWeightOnRestart());

        LinearLayout updateOnSaveLayout = view.findViewById(R.id.update_on_save_layout);
        updateOnSaveLayout.setOnClickListener(view1 -> updateOnSaveSwitch.performClick());
        updateOnSaveSwitch.setChecked(userPreferences.isUpdateDefaultWeightOnSave());
        updateOnSaveSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> saveChanged = isChecked != userPreferences.isUpdateDefaultWeightOnSave());
        return view;
    }

    @Override
    public void onPause() {
        if (metricChanged || privateChanged || restartChanged || saveChanged) {
            // one or more settings has changed, so update it in the DB and the local user settings
            userPreferences.setMetricUnits(metricSwitch.isChecked());
            userPreferences.setPrivateAccount(privateSwitch.isChecked());
            userPreferences.setUpdateDefaultWeightOnSave(updateOnSaveSwitch.isChecked());
            userPreferences.setUpdateDefaultWeightOnRestart(updateOnRestartSwitch.isChecked());

            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                ResultStatus<String> resultStatus = this.userRepository.updateUserPreferences(userPreferences);
                Handler handler = new Handler(getMainLooper());
                handler.post(() -> {
                    if (!resultStatus.isSuccess()) {
                        ErrorDialog.showErrorDialog("Error", resultStatus.getErrorMessage(), getContext());
                    }
                });
            });
        }
        super.onPause();
    }
}
