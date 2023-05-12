package com.joshrap.liteweight.models.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPreferences {

    private boolean privateAccount;
    private boolean updateDefaultWeightOnSave;
    private boolean updateDefaultWeightOnRestart;
    private boolean metricUnits;
}
