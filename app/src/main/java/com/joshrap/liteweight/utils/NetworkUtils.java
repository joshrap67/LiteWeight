package com.joshrap.liteweight.utils;

public class NetworkUtils {

    public static String getRoute(String... params) {
        return String.join("/", params);
    }
}
