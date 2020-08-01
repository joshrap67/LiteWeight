package com.joshrap.liteweight.models;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CognitoResponse {

    @JsonProperty("RefreshToken")
    private String refreshToken;
    @JsonProperty("IdToken")
    private String idToken;
    private String errorMessage;

    public CognitoResponse(String refreshToken, String idToken) {
        this.refreshToken = refreshToken;
        this.idToken = idToken;
    }

    public CognitoResponse(String idToken) {
        this.idToken = idToken;
    }


    public static CognitoResponse deserializeSignIn(InputStream rawInput) {
        CognitoResponse retVal = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String, Object> jsonMap = mapper.readValue(rawInput, Map.class);
            Map<String, Object> authenticationResult = (Map<String, Object>) jsonMap.get("AuthenticationResult");
            retVal = new CognitoResponse(authenticationResult.get("RefreshToken").toString(), authenticationResult.get("IdToken").toString());

        } catch (Exception e) {
            // do nothing
            System.out.println(e.toString());
        }
        return retVal;
    }

    public static CognitoResponse deserializeRefresh(InputStream rawInput) {
        CognitoResponse retVal = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String, Object> jsonMap = mapper.readValue(rawInput, Map.class);
            Map<String, Object> authenticationResult = (Map<String, Object>) jsonMap.get("AuthenticationResult");
            retVal = new CognitoResponse(authenticationResult.get(("IdToken")).toString());

        } catch (Exception e) {
            // do nothing
            System.out.println(e.toString());
        }
        return retVal;
    }

    public static boolean deserializeSignUp(InputStream rawInput) {
        boolean retVal = false;
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String, Object> jsonMap = mapper.readValue(rawInput, Map.class);
            retVal = Boolean.parseBoolean(jsonMap.get("UserConfirmed").toString());
//            retVal.setUserConfirmed(userConfirmed);
        } catch (Exception e) {
            // do nothing
            System.out.println(e.toString());
        }
        return retVal;
    }

    public static String deserializeError(InputStream rawInput, String action) {
        // action is either sign in, up, confirm, or reset
        // TODO just return the errorString???
        String retVal = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String, Object> jsonMap = mapper.readValue(rawInput, Map.class);
            String type = jsonMap.get("__type").toString();
            retVal = jsonMap.get("message").toString();
            // TODO only show specific error messages like incorrect username/pass. Otherwise just do generic failed

        } catch (Exception e) {
            // do nothing
            System.out.println(e.toString());
        }
        return retVal;
    }

    public CognitoResponse() {
        this.refreshToken = null;
        this.idToken = null;
        this.errorMessage = null;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @NonNull
    @Override
    public String toString() {
        return "RefreshToken: " + this.refreshToken + " IdToken: " + this.idToken +
                " ErrorMessage: " + this.errorMessage;
    }
}
