package com.joshrap.liteweight.injection;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AlertDialog;

import com.joshrap.liteweight.R;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.models.Tokens;
import com.joshrap.liteweight.models.VersionModel;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
class LiteWeightModule {
    private final Context context;

    LiteWeightModule(final Context context) {
        this.context = context;
    }

    @Provides
    Context provideContext() {
        return this.context;
    }


    @Provides
    @Singleton
    SharedPreferences provideSharedPreference(final Context context) {
        return context.getSharedPreferences(Variables.SHARED_PREF_SETTINGS, Context.MODE_PRIVATE);
    }

    @Provides
    @Singleton
    Tokens provideTokens(final SharedPreferences sharedPreferences) {
        String refreshToken = sharedPreferences.getString(Variables.REFRESH_TOKEN_KEY, null);
        String idToken = sharedPreferences.getString(Variables.ID_TOKEN_KEY, null);
        return new Tokens(refreshToken, idToken);
    }

    @Provides
    AlertDialog provideAlertDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setView(R.layout.popup_loading);
        return builder.create();
    }

    @Provides
    VersionModel provideVersionModel(final Context context) {
        String versionName = null;
        int versionCode = 0;
        try {
            versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new VersionModel(versionName, versionCode);
    }
}
