package com.joshrap.liteweight.injection;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;

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
    ProgressDialog provideProgressDialog(final Context context) {
        ProgressDialog progressDialog = new ProgressDialog(context, R.style.ProgressDialogStyle);
        progressDialog.setCancelable(false);
        return progressDialog;
    }

    @Provides
    VersionModel provideVersionModel(final Context context) {
        String version = null;
        try {
            version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new VersionModel(version);
    }
}
