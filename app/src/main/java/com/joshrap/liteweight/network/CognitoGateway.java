package com.joshrap.liteweight.network;

import com.joshrap.liteweight.utils.JsonUtils;
import com.joshrap.liteweight.imports.ApiConfig;
import com.joshrap.liteweight.models.CognitoResponse;
import com.joshrap.liteweight.models.ResultStatus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.inject.Inject;

public class CognitoGateway {
    private static final String baseURL = "https://cognito-idp.us-east-1.amazonaws.com";
    private static final String contentType = "application/x-amz-json-1.1";
    private static final String baseTarget = "com.amazonaws.cognito.identity.idp.model.AWSCognitoIdentityProviderService.";

    @Inject
    public CognitoGateway() {
    }

    public ResultStatus<String> makeRequest(String action, Map<String, Object> body) {
        ResultStatus<String> resultStatus = new ResultStatus<>();
        body.put("ClientId", ApiConfig.cognitoClientId);

        try {

            URL url = new URL(baseURL);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type", contentType);
            httpURLConnection.setRequestProperty("x-amz-target", baseTarget + action);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);

            OutputStream os = httpURLConnection.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
            osw.write(JsonUtils.serializeMap(body));
            osw.flush();
            osw.close();
            os.close();

            httpURLConnection.connect();

            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == 200) {
                ByteArrayOutputStream result = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = httpURLConnection.getInputStream().read(buffer)) != -1) {
                    result.write(buffer, 0, length);
                }
                String jsonResponse = result.toString("UTF-8");
                resultStatus.setData(jsonResponse);
                resultStatus.setSuccess(true);
            } else if (responseCode == 400) {
                ByteArrayOutputStream result = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = httpURLConnection.getErrorStream().read(buffer)) != -1) {
                    result.write(buffer, 0, length);
                }
                String jsonResponse = result.toString("UTF-8");
                String errorMessage = CognitoResponse.deserializeError(jsonResponse, action);
                resultStatus.setErrorMessage(errorMessage);
                resultStatus.setSuccess(false);
            }
            httpURLConnection.disconnect();

        } catch (Exception e) {
            resultStatus.setSuccess(false);
            resultStatus.setErrorMessage(e.toString());
        }
        return resultStatus;

    }
}
