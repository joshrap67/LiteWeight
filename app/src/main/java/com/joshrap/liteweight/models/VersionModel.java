package com.joshrap.liteweight.models;

import lombok.Data;

@Data
public class VersionModel {
    private final String versionName;
    private final int versionCode;

    public VersionModel(String version, int versionCode){
        this.versionCode = versionCode;
        this.versionName = version;
    }
}
