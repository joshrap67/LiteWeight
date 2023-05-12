package com.joshrap.liteweight.repositories.currentUser.requests;

import com.joshrap.liteweight.models.user.UserPreferences;
import com.joshrap.liteweight.repositories.BodyRequest;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SetUserPreferencesRequest extends BodyRequest {

    public boolean privateAccount;
    public boolean updateDefaultWeightOnSave;
    public boolean updateDefaultWeightOnRestart;
    public boolean metricUnits;

    public SetUserPreferencesRequest(UserPreferences userPreferences) {
        this.privateAccount = userPreferences.isPrivateAccount();
        this.metricUnits = userPreferences.isMetricUnits();
        this.updateDefaultWeightOnRestart = userPreferences.isUpdateDefaultWeightOnRestart();
        this.updateDefaultWeightOnSave = userPreferences.isUpdateDefaultWeightOnSave();
    }
}
