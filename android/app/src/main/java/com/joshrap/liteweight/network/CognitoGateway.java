package com.joshrap.liteweight.network;

import com.joshrap.liteweight.models.CognitoResponse;
import com.joshrap.liteweight.models.ResultStatus;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class CognitoGateway {
    private static final String baseURL = "https://cognito-idp.us-east-2.amazonaws.com";
    private static final String contentType = "application/x-amz-json-1.1";
    private static final String baseTarget = "com.amazonaws.cognito.identity.idp.model.AWSCognitoIdentityProviderService.";
    private static final String clientId = "59rgofah1ijtsqnucugcri2hl2";

    private static final String signInAction = "InitiateAuth";
    private static final String signUpAction = "SignUp";
    private static final String confirmSignUpAction = "ConfirmSignUp";


    public static ResultStatus<CognitoResponse> initiateAuth(String username, String password) {
        ResultStatus<CognitoResponse> resultStatus = new ResultStatus<>();
        try {

            URL url = new URL(baseURL);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type", contentType);
            httpURLConnection.setRequestProperty("x-amz-target", baseTarget + signInAction);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);

            OutputStream os = httpURLConnection.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
            osw.write("{\n" +
                    "\"ClientId\": \"" + clientId + "\",\n" +
                    "\"AuthFlow\": \"USER_PASSWORD_AUTH\",\n" +
                    "\"AuthParameters\": { \n" +
                    "      \"USERNAME\" : \"" + username + "\",\n" +
                    "      \"PASSWORD\" : \"" + password + "\"\n" +
                    "   }\n" +
                    "}");
            osw.flush();
            osw.close();
            os.close();  //don't forget to close the OutputStream

            httpURLConnection.connect();

            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == 400) {
                String errorMessage = CognitoResponse.deserializeError(httpURLConnection.getErrorStream(), signInAction);
                resultStatus.setErrorMessage(errorMessage);
                resultStatus.setSuccess(false);
            } else if (responseCode == 200) {
                resultStatus.setData(CognitoResponse.deserializeSignIn(httpURLConnection.getInputStream()));
                resultStatus.setSuccess(true);
            }
            httpURLConnection.disconnect();
            System.out.println(resultStatus);

        } catch (IOException io) {
            System.out.println(io.toString());
            resultStatus.setSuccess(false);
            resultStatus.setNetworkError(true);
            resultStatus.setErrorMessage(io.toString());
        } catch (Exception e) {
            // do nothing
            System.out.println(e.toString());
            resultStatus.setSuccess(false);
            resultStatus.setErrorMessage(e.toString());
        }

        return resultStatus;
    }

    public static ResultStatus<Boolean> signUp(String username, String password, String email) {
        ResultStatus<Boolean> resultStatus = new ResultStatus<>();
        try {

            URL url = new URL(baseURL);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type", contentType);
            httpURLConnection.setRequestProperty("x-amz-target", baseTarget + signUpAction);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);

            OutputStream os = httpURLConnection.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
            osw.write("{\n" +
                    "\"ClientId\": \"" + clientId + "\",\n" +
                    "\"Password\": \"" + password + "\",\n" +
                    "\"UserAttributes\": [\n" +
                    "    {\n" +
                    "        \"Name\": \"email\",\n" +
                    "        \"Value\": \"" + email + "\"\n" +
                    "    }\n" +
                    "],\n" +
                    "\"Username\": \"" + username + "\"\n" +
                    "}");
            osw.flush();
            osw.close();
            os.close();  //don't forget to close the OutputStream

            httpURLConnection.connect();

            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == 400) {
                String errorMessage = CognitoResponse.deserializeError(httpURLConnection.getErrorStream(), signUpAction);
                resultStatus.setErrorMessage(errorMessage);
                resultStatus.setSuccess(false);
            } else if (responseCode == 200) {
                resultStatus.setData(CognitoResponse.deserializeSignUp(httpURLConnection.getInputStream()));
                resultStatus.setSuccess(true);
            } // TODO do a general else in case
            httpURLConnection.disconnect();
        } catch (IOException io) {
            resultStatus.setSuccess(false);
            resultStatus.setNetworkError(true);
            resultStatus.setErrorMessage(io.toString());
        } catch (Exception e) {
            // do nothing
            resultStatus.setSuccess(false);
            resultStatus.setErrorMessage(e.toString());
        }

        return resultStatus;
    }

    public static ResultStatus<Boolean> confirmSignUp(String username, String confirmationCode) {
        ResultStatus<Boolean> resultStatus = new ResultStatus<>();
        try {

            URL url = new URL(baseURL);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type", contentType);
            httpURLConnection.setRequestProperty("x-amz-target", baseTarget + confirmSignUpAction);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);

            OutputStream os = httpURLConnection.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
            osw.write("{\n" +
                    "   \"ClientId\": \"" + clientId + "\",\n" +
                    "   \"ConfirmationCode\": \"" + confirmationCode + "\",\n" +
                    "   \"Username\": \"" + username + "\"\n" +
                    "}");
            osw.flush();
            osw.close();
            os.close();  //don't forget to close the OutputStream

            httpURLConnection.connect();

            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == 400) {
                String errorMessage = CognitoResponse.deserializeError(httpURLConnection.getErrorStream(), confirmSignUpAction);
                resultStatus.setErrorMessage(errorMessage);
                resultStatus.setSuccess(false);
                resultStatus.setData(false);
            } else if (responseCode == 200) {
                // success in this method returns an empty body
                resultStatus.setData(true);
                resultStatus.setSuccess(true);
            }
            httpURLConnection.disconnect();
            System.out.println(resultStatus);

        } catch (IOException io) {
            System.out.println(io.toString());
            resultStatus.setSuccess(false);
            resultStatus.setNetworkError(true);
            resultStatus.setErrorMessage(io.toString());
        } catch (Exception e) {
            // do nothing
            System.out.println(e.toString());
            resultStatus.setSuccess(false);
            resultStatus.setErrorMessage(e.toString());
        }

        return resultStatus;
    }

    public static ResultStatus<CognitoResponse> refreshTokens(String refreshToken) {
        ResultStatus<CognitoResponse> resultStatus = new ResultStatus<>();
        try {
            URL url = new URL(baseURL);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type", contentType);
            httpURLConnection.setRequestProperty("x-amz-target", baseTarget + signInAction);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);

            OutputStream os = httpURLConnection.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
            osw.write("{\n" +
                    "\"ClientId\": \"" + clientId + "\",\n" +
                    "\"AuthFlow\": \"REFRESH_TOKEN_AUTH\",\n" +
                    "\"AuthParameters\": { \n" +
                    "      \"REFRESH_TOKEN\" : \"" + refreshToken + "\"}\n" +
                    "}");
            osw.flush();
            osw.close();
            os.close();  //don't forget to close the OutputStream

            httpURLConnection.connect();

            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == 400) {
                String errorMessage = CognitoResponse.deserializeError(httpURLConnection.getErrorStream(), signInAction);
                resultStatus.setErrorMessage(errorMessage);
                resultStatus.setSuccess(false);
            } else if (responseCode == 200) {
                resultStatus.setData(CognitoResponse.deserializeRefresh(httpURLConnection.getInputStream()));
                resultStatus.setSuccess(true);
            }
            httpURLConnection.disconnect();
            System.out.println(resultStatus);

        } catch (IOException io) {
            System.out.println(io.toString());
            resultStatus.setSuccess(false);
            resultStatus.setNetworkError(true);
            resultStatus.setErrorMessage(io.toString());
        } catch (Exception e) {
            // do nothing
            System.out.println(e.toString());
            resultStatus.setSuccess(false);
            resultStatus.setErrorMessage(e.toString());
        }

        return resultStatus;
    }
}
