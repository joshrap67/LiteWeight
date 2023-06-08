package com.joshrap.liteweight.utils;

import com.joshrap.liteweight.models.LiteWeightNetworkException;

public class NetworkUtils {

    public static String getRoute(String... params) {
        return String.join("/", params);
    }

    public static String getLiteWeightError(LiteWeightNetworkException e) {
        return e.getErrorType();
    }
}
