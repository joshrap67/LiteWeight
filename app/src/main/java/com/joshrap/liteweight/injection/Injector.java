package com.joshrap.liteweight.injection;

import android.content.Context;

public class Injector {
    public static LiteWeightComponent getInjector(final Context context) {
        return DaggerLiteWeightComponent
                .builder()
                .liteWeightModule(new LiteWeightModule(context))
                .build();
    }

    public static LiteWeightComponent getInjector(final Context context, final String refreshToken, String idToken) {
        return DaggerLiteWeightComponent
                .builder()
                .liteWeightModule(new LiteWeightModule(context, refreshToken, idToken))
                .build();
    }
}