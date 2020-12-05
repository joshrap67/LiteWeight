package com.joshrap.liteweight.models;

import javax.inject.Inject;

import lombok.Data;

@Data
public class Tokens {

    private String refreshToken;
    private String idToken;

    @Inject
    public Tokens(String refreshToken, String idToken) {
        this.refreshToken = refreshToken;
        this.idToken = idToken;
    }
}
