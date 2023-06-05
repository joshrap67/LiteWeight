package com.joshrap.liteweight.repositories.self.requests;

import com.joshrap.liteweight.models.user.UserSettings;
import com.joshrap.liteweight.repositories.BodyRequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SetUserSettingsRequest extends BodyRequest {

    private boolean privateAccount;
    private boolean updateDefaultWeightOnSave;
    private boolean updateDefaultWeightOnRestart;
    private boolean metricUnits;

    public SetUserSettingsRequest(UserSettings userSettings) {
        this.privateAccount = userSettings.isPrivateAccount();
        this.metricUnits = userSettings.isMetricUnits();
        this.updateDefaultWeightOnRestart = userSettings.isUpdateDefaultWeightOnRestart();
        this.updateDefaultWeightOnSave = userSettings.isUpdateDefaultWeightOnSave();
    }
}
