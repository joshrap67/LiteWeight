package com.joshrap.liteweight.models;

import com.joshrap.liteweight.utils.JsonUtils;
import com.joshrap.liteweight.network.repos.CognitoRepository;

import java.util.Map;

import lombok.Data;

@Data
public class CognitoResponse {
    public static final String expiredCodeErrorMsg = "The code has expired. Please request another one below.";
    public static final String incorrectCodeErrorMsg = "The entered code is incorrect. Please try again.";

    private String refreshToken;
    private String idToken;
    private String errorMessage;

    public CognitoResponse(String refreshToken, String idToken) {
        this.refreshToken = refreshToken;
        this.idToken = idToken;
    }

    public CognitoResponse(String idToken) {
        this.idToken = idToken;
    }

    public static CognitoResponse deserializeInitiateAuth(String jsonString) {
        CognitoResponse retVal = null;
        try {
            Map<String, Object> jsonMap = JsonUtils.deserialize(jsonString);
            Map<String, Object> authenticationResult = (Map<String, Object>) jsonMap.get("AuthenticationResult");
            retVal = new CognitoResponse(authenticationResult.get("RefreshToken").toString(), authenticationResult.get("IdToken").toString());
        } catch (Exception e) {
            // do nothing
            e.printStackTrace();
        }
        return retVal;
    }

    public static CognitoResponse deserializeRefresh(String jsonString) {
        CognitoResponse retVal = null;
        try {
            Map<String, Object> jsonMap = JsonUtils.deserialize(jsonString);
            Map<String, Object> authenticationResult = (Map<String, Object>) jsonMap.get("AuthenticationResult");
            retVal = new CognitoResponse(authenticationResult.get(("IdToken")).toString());
        } catch (Exception e) {
            // do nothing
            e.printStackTrace();
        }
        return retVal;
    }

    public static boolean deserializeSignUp(String jsonString) {
        boolean retVal = false;
        try {
            Map<String, Object> jsonMap = JsonUtils.deserialize(jsonString);
            retVal = Boolean.parseBoolean(jsonMap.get("UserConfirmed").toString());
        } catch (Exception e) {
            // do nothing
            e.printStackTrace();
        }
        return retVal;
    }

    public static String deserializeError(String rawInput, String action) {
        String retVal = null;
        switch (action) {
            case CognitoRepository.initiateAuthAction:
                retVal = deserializeSignInError(rawInput);
                break;
            case CognitoRepository.signUpAction:
                retVal = deserializeSignUpError(rawInput);
                break;
            case CognitoRepository.confirmSignUpAction:
                retVal = deserializeConfirmSignUpError(rawInput);
                break;
            case CognitoRepository.resendCodeAction:
                retVal = deserializeResendCodeError(rawInput);
                break;
            case CognitoRepository.forgotPasswordAction:
                retVal = deserializeForgotPasswordError(rawInput);
                break;
            case CognitoRepository.confirmForgotPasswordAction:
                retVal = deserializeConfirmForgotPasswordError(rawInput);
                break;
        }
        return retVal;
    }

    private static String deserializeSignInError(String rawInput) {
        String retVal = null;
        try {
            Map<String, Object> jsonMap = JsonUtils.deserialize(rawInput);
            retVal = jsonMap.get("message").toString();
        } catch (Exception e) {
            // do nothing
            e.printStackTrace();
        }
        return retVal;
    }

    private static String deserializeSignUpError(String rawInput) {
        String retVal = null;
        try {
            Map<String, Object> jsonMap = JsonUtils.deserialize(rawInput);
            String msg;
            if (jsonMap.containsKey("Message")) {
                // .... for some reason cognito has this as capitalized on certain return values...
                msg = (String) jsonMap.get("Message");
            } else {
                msg = (String) jsonMap.get("message");
            }
            retVal = msg;
        } catch (Exception e) {
            // do nothing
            e.printStackTrace();
        }
        return retVal;
    }

    private static String deserializeConfirmSignUpError(String rawInput) {
        String retVal = null;
        try {
            Map<String, Object> jsonMap = JsonUtils.deserialize(rawInput);
            String type = jsonMap.get("__type").toString();
            if (type.equals("ExpiredCodeException")) {
                retVal = expiredCodeErrorMsg;
            } else if (type.equals("CodeMismatchException")) {
                retVal = incorrectCodeErrorMsg;
            } else {
                retVal = jsonMap.get("message").toString();
            }
        } catch (Exception e) {
            // do nothing
            e.printStackTrace();
        }
        return retVal;
    }

    private static String deserializeResendCodeError(String rawInput) {
        String retVal = null;
        try {
            Map<String, Object> jsonMap = JsonUtils.deserialize(rawInput);
            retVal = jsonMap.get("message").toString();
        } catch (Exception e) {
            // do nothing
            e.printStackTrace();
        }
        return retVal;
    }

    private static String deserializeForgotPasswordError(String rawInput) {
        String retVal = null;
        try {
            Map<String, Object> jsonMap = JsonUtils.deserialize(rawInput);
            retVal = jsonMap.get("message").toString();
        } catch (Exception e) {
            // do nothing
            e.printStackTrace();
        }
        return retVal;
    }

    private static String deserializeConfirmForgotPasswordError(String rawInput) {
        String retVal = null;
        try {
            Map<String, Object> jsonMap = JsonUtils.deserialize(rawInput);
            retVal = jsonMap.get("message").toString();
        } catch (Exception e) {
            // do nothing
            e.printStackTrace();
        }
        return retVal;
    }
}
