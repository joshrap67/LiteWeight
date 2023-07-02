package com.joshrap.liteweight.utils;

import com.google.firebase.auth.FirebaseUser;

public class FirebaseUtils {

    public static boolean userHasPassword(FirebaseUser user) {
        return user != null && user.getProviderData().stream().anyMatch(x -> x.getProviderId().equalsIgnoreCase("password"));
    }
}
