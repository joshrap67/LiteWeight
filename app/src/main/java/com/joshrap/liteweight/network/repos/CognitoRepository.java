package com.joshrap.liteweight.network.repos;

import com.joshrap.liteweight.models.CognitoResponse;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.network.CognitoGateway;
import com.joshrap.liteweight.network.RequestFields;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class CognitoRepository {
    public static final String initiateAuthAction = "InitiateAuth";
    public static final String signUpAction = "SignUp";
    public static final String confirmSignUpAction = "ConfirmSignUp";
    public static final String resendCodeAction = "ResendConfirmationCode";
    public static final String forgotPasswordAction = "ForgotPassword";
    public static final String confirmForgotPasswordAction = "ConfirmForgotPassword";

    private final CognitoGateway cognitoGateway;

    @Inject
    public CognitoRepository(CognitoGateway cognitoGateway) {
        this.cognitoGateway = cognitoGateway;
    }

    public ResultStatus<CognitoResponse> initiateAuth(String username, String password) {
        ResultStatus<CognitoResponse> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        Map<String, String> authParameters = new HashMap<>();
        authParameters.put("USERNAME", username);
        authParameters.put("PASSWORD", password);
        requestBody.put("AuthParameters", authParameters);
        requestBody.put("AuthFlow", "USER_PASSWORD_AUTH");

        ResultStatus<String> cognitoResponse = this.cognitoGateway.makeRequest(initiateAuthAction, requestBody);
        if (cognitoResponse.isSuccess()) {
            resultStatus.setSuccess(true);
            resultStatus.setData(CognitoResponse.deserializeInitiateAuth(cognitoResponse.getData()));
        } else {
            resultStatus.setErrorMessage(cognitoResponse.getErrorMessage());
        }

        return resultStatus;
    }

    public ResultStatus<Boolean> signUp(String username, String password, String email, String optionalIdToken) {
        ResultStatus<Boolean> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("Username", username);
        requestBody.put("Password", password);
        List<Map<String, String>> userAttributesList = new ArrayList<>();
        Map<String, String> emailAttributes = new HashMap<>();

        emailAttributes.put("Name", "email");
        emailAttributes.put("Value", email);

        if (optionalIdToken != null) {
            Map<String, String> tokenAttributes = new HashMap<>();
            List<Map<String, String>> validationAttributesList = new ArrayList<>();
            tokenAttributes.put("Name", RequestFields.ID_TOKEN_GOOGLE);
            tokenAttributes.put("Value", optionalIdToken);
            validationAttributesList.add(tokenAttributes);
            requestBody.put("ValidationData", validationAttributesList);
        }

        userAttributesList.add(emailAttributes);
        requestBody.put("UserAttributes", userAttributesList);
        requestBody.put("AuthFlow", "USER_PASSWORD_AUTH");

        ResultStatus<String> cognitoResponse = this.cognitoGateway.makeRequest(signUpAction, requestBody);
        if (cognitoResponse.isSuccess()) {
            resultStatus.setSuccess(true);
            resultStatus.setData(CognitoResponse.deserializeSignUp(cognitoResponse.getData()));
        } else {
            resultStatus.setErrorMessage(cognitoResponse.getErrorMessage());
        }

        return resultStatus;
    }

    public ResultStatus<CognitoResponse> confirmSignUp(String username, String confirmationCode) {
        ResultStatus<CognitoResponse> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("ConfirmationCode", confirmationCode);
        requestBody.put("Username", username);

        ResultStatus<String> cognitoResponse = this.cognitoGateway.makeRequest(confirmSignUpAction, requestBody);
        if (cognitoResponse.isSuccess()) {
            resultStatus.setSuccess(true);
            resultStatus.setData(new CognitoResponse(cognitoResponse.getData()));
        } else {
            resultStatus.setErrorMessage(cognitoResponse.getErrorMessage());
        }

        return resultStatus;
    }

    public ResultStatus<CognitoResponse> refreshIdToken(String refreshToken) {
        ResultStatus<CognitoResponse> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("AuthFlow", "REFRESH_TOKEN_AUTH");
        Map<String, String> authParameters = new HashMap<>();
        authParameters.put("REFRESH_TOKEN", refreshToken);
        requestBody.put("AuthParameters", authParameters);

        ResultStatus<String> cognitoResponse = this.cognitoGateway.makeRequest(initiateAuthAction, requestBody);
        if (cognitoResponse.isSuccess()) {
            resultStatus.setSuccess(true);
            resultStatus.setData(CognitoResponse.deserializeRefresh(cognitoResponse.getData()));
        } else {
            resultStatus.setErrorMessage(cognitoResponse.getErrorMessage());
        }

        return resultStatus;
    }

    public ResultStatus<Boolean> resendEmailConfirmationCode(String username) {
        ResultStatus<Boolean> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("Username", username);

        ResultStatus<String> cognitoResponse = this.cognitoGateway.makeRequest(resendCodeAction, requestBody);
        if (cognitoResponse.isSuccess()) {
            resultStatus.setSuccess(true);
            resultStatus.setData(true);
        } else {
            resultStatus.setErrorMessage(cognitoResponse.getErrorMessage());
        }

        return resultStatus;
    }

    public ResultStatus<Boolean> forgotPassword(String username) {
        ResultStatus<Boolean> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("Username", username);

        ResultStatus<String> cognitoResponse = this.cognitoGateway.makeRequest(forgotPasswordAction, requestBody);
        if (cognitoResponse.isSuccess()) {
            resultStatus.setSuccess(true);
            resultStatus.setData(true);
        } else {
            resultStatus.setErrorMessage(cognitoResponse.getErrorMessage());
        }

        return resultStatus;
    }

    public ResultStatus<Boolean> confirmForgotPassword(String username, String newPassword, String confirmationCode) {
        ResultStatus<Boolean> resultStatus = new ResultStatus<>();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("Username", username);
        requestBody.put("ConfirmationCode", confirmationCode);
        requestBody.put("Password", newPassword);

        ResultStatus<String> cognitoResponse = this.cognitoGateway.makeRequest(confirmForgotPasswordAction, requestBody);
        if (cognitoResponse.isSuccess()) {
            resultStatus.setSuccess(true);
            resultStatus.setData(true);
        } else {
            resultStatus.setErrorMessage(cognitoResponse.getErrorMessage());
        }

        return resultStatus;
    }
}
