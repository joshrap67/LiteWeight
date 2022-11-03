package com.joshrap.liteweight.models;

import androidx.annotation.NonNull;

public class VersionModel {
    private final String version;

    public VersionModel(String version){
        this.version = version;
    }

    @NonNull
    @Override
    public String toString() {
        return this.version;
    }
}
