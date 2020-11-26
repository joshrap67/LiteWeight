package com.joshrap.liteweight.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joshrap.liteweight.imports.ApiConfig;
import com.joshrap.liteweight.models.CognitoResponse;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.Tokens;
import com.joshrap.liteweight.network.repos.CognitoRepository;

import java.io.BufferedReader;
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

import javax.inject.Inject;

import lombok.Data;

@Data
public class ApiGateway {

    private static final int successCode = 200; // THIS MUST MATCH THE CODE RETURNED FROM THE API

    private final Tokens tokens;
    private final CognitoRepository cognitoRepository;

    @Inject
    public ApiGateway(Tokens tokens, CognitoRepository cognitoRepository) {
        this.tokens = tokens;
        this.cognitoRepository = cognitoRepository;
    }

    public ResultStatus<String> makeRequest(String action, Map<String, Object> body, boolean firstTry) {
        ResultStatus<String> resultStatus = new ResultStatus<>();

        if (this.tokens != null) {
            try {
                URL url = new URL(ApiConfig.apiUrl + ApiConfig.deploymentStage + action);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Authorization", "Bearer " + this.tokens.getIdToken());
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
                    if (firstTry && responseCode == 401) {
                        if (refreshIdToken(this.tokens.getRefreshToken())) {
                            resultStatus = makeRequest(action, body, false);
                        }

                    } else {
                        // refresh didn't work, so it was a real error.
                        StringBuilder jsonResponse = new StringBuilder();
                        try (Reader reader = new BufferedReader(new InputStreamReader
                                (httpURLConnection.getErrorStream(), Charset.forName(StandardCharsets.UTF_8.name())))) {
                            int c;
                            while ((c = reader.read()) != -1) {
                                jsonResponse.append((char) c);
                            }
                        }
                        resultStatus.setErrorMessage(jsonResponse.toString());
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

    private boolean refreshIdToken(String refreshToken) {
        ResultStatus<CognitoResponse> resultStatus = this.cognitoRepository.refreshIdToken(refreshToken);
        if (resultStatus.isSuccess()) {
            this.tokens.setIdToken(resultStatus.getData().getIdToken());
        }
        return resultStatus.isSuccess();
    }


}
