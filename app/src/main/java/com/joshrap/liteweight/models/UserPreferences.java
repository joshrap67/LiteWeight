package com.joshrap.liteweight.models;

import com.joshrap.liteweight.interfaces.Model;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserPreferences implements Model {

    public static final String PRIVATE_ACCOUNT = "private";
    public static final String UPDATE_DEFAULT_WEIGHT_ON_SAVE = "updateDefaultWeightOnSave";
    public static final String UPDATE_DEFAULT_WEIGHT_ON_RESTART = "updateDefaultWeightOnRestart";
    public static final String METRIC = "metric";

    private boolean privateAccount;
    private boolean updateDefaultWeightOnSave;
    private boolean updateDefaultWeightOnRestart;
    private boolean metricUnits;

    UserPreferences(Map<String, Object> json) {
        this.setPrivateAccount((Boolean) json.get(PRIVATE_ACCOUNT));
        this.setUpdateDefaultWeightOnRestart((Boolean) json.get(UPDATE_DEFAULT_WEIGHT_ON_RESTART));
        this.setUpdateDefaultWeightOnSave((Boolean) json.get(UPDATE_DEFAULT_WEIGHT_ON_SAVE));
        this.setMetricUnits((Boolean) json.get(METRIC));
    }

    public UserPreferences(UserPreferences userPreferences) {
        this.privateAccount = userPreferences.isPrivateAccount();
        this.updateDefaultWeightOnRestart = userPreferences.isUpdateDefaultWeightOnRestart();
        this.updateDefaultWeightOnSave = userPreferences.isUpdateDefaultWeightOnSave();
        this.metricUnits = userPreferences.isMetricUnits();
    }

    @Override
    public Map<String, Object> asMap() {
        Map<String, Object> retVal = new HashMap<>();
        retVal.put(PRIVATE_ACCOUNT, this.privateAccount);
        retVal.put(METRIC, this.metricUnits);
        retVal.put(UPDATE_DEFAULT_WEIGHT_ON_SAVE, this.updateDefaultWeightOnSave);
        retVal.put(UPDATE_DEFAULT_WEIGHT_ON_RESTART, this.updateDefaultWeightOnRestart);
        return retVal;
    }
}
