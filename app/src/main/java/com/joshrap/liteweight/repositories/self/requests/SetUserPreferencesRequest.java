package com.joshrap.liteweight.repositories.self.requests;

import com.joshrap.liteweight.models.user.UserPreferences;
import com.joshrap.liteweight.repositories.BodyRequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SetUserPreferencesRequest extends BodyRequest {

    private boolean privateAccount;
    private boolean updateDefaultWeightOnSave;
    private boolean updateDefaultWeightOnRestart;
    private boolean metricUnits;

    public SetUserPreferencesRequest(UserPreferences userPreferences) {
        this.privateAccount = userPreferences.isPrivateAccount();
        this.metricUnits = userPreferences.isMetricUnits();
        this.updateDefaultWeightOnRestart = userPreferences.isUpdateDefaultWeightOnRestart();
        this.updateDefaultWeightOnSave = userPreferences.isUpdateDefaultWeightOnSave();
    }
}
