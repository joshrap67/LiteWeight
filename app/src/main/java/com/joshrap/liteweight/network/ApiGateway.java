package com.joshrap.liteweight.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joshrap.liteweight.imports.Globals;
import com.joshrap.liteweight.models.CognitoResponse;
import com.joshrap.liteweight.models.ResultStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ApiGateway {

    private static final String ApiUrl = "https://vixcdm7fz5.execute-api.us-east-2.amazonaws.com/";
    private static final int successCode = 200; // THIS MUST MATCH THE CODE RETURNED FROM THE API

    public static ResultStatus<String> makeRequest(String action, Map<String, Object> body, boolean firstTry) {
        ResultStatus<String> resultStatus = new ResultStatus<>();

        if (Globals.idToken != null) {
            try {
                URL url = new URL(ApiUrl + Globals.deploymentStage + action);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Authorization", "Bearer " + Globals.idToken);
                httpURLConnection.setUseCaches(false);
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);

                OutputStream os = httpURLConnection.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
                osw.write(new ObjectMapper().writeValueAsString(body));
                osw.flush();
                osw.close();
                os.close();  //don't forget to close the OutputStream

                httpURLConnection.connect();

                int responseCode = httpURLConnection.getResponseCode();
                if (responseCode == successCode) {
                    // parse the response into a json string
                    StringBuilder jsonResponse = new StringBuilder();
                    try (Reader reader = new BufferedReader(new InputStreamReader
                            (httpURLConnection.getInputStream(), Charset.forName(StandardCharsets.UTF_8.name())))) {
                        int c;
                        while ((c = reader.read()) != -1) {
                            jsonResponse.append((char) c);
                        }
                    }
                    resultStatus.setData(jsonResponse.toString());
                    resultStatus.setSuccess(true);
                } else {
                    // TODO handle specific error codes?
                    if (firstTry) {
                        if (refreshIdToken(Globals.refreshToken)) {
                            resultStatus = makeRequest(action, body, false);
                        }

                    }
                }
                httpURLConnection.disconnect();

            } catch (SocketException se) {
                // TODO don't think this works, need to accurately find out if internet issue or not
                resultStatus.setSuccess(false);
                resultStatus.setNetworkError(true);
                resultStatus.setErrorMessage(se.toString());
            } catch (Exception e) {
                // do nothing
                resultStatus.setSuccess(false);
                resultStatus.setErrorMessage(e.toString());
            }
        }
        return resultStatus;
    }

    private static boolean refreshIdToken(String refreshToken) {
        ResultStatus<CognitoResponse> resultStatus = CognitoGateway.refreshIdToken(refreshToken);
        if (resultStatus.isSuccess()) {
            Globals.idToken = resultStatus.getData().getIdToken();
        }
        return resultStatus.isSuccess();
    }


}
