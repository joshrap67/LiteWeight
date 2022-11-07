package com.joshrap.liteweight.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joshrap.liteweight.imports.BackendConfig;
import com.joshrap.liteweight.models.CognitoResponse;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.Tokens;
import com.joshrap.liteweight.models.VersionModel;
import com.joshrap.liteweight.network.repos.CognitoRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.inject.Inject;

import lombok.Data;

@Data
public class ApiGateway {

    private static final int successCode = 200; // THIS MUST MATCH THE CODE RETURNED FROM THE API
    private static final int versionUpgradeCode = 426; // THIS MUST MATCH THE CODE RETURNED FROM THE API
    private static final String VERSION_CODE_HEADER = "X-LiteWeight-Version-Code";
    private static final String VERSION_NAME_HEADER = "X-LiteWeight-Version-Name";

    private final Tokens tokens;
    private final CognitoRepository cognitoRepository;
    private final VersionModel versionModel;

    @Inject
    public ApiGateway(Tokens tokens, CognitoRepository cognitoRepository, VersionModel versionModel) {
        this.tokens = tokens;
        this.cognitoRepository = cognitoRepository;
        this.versionModel = versionModel;
    }

    public ResultStatus<String> makeRequest(String action, Map<String, Object> body, boolean firstTry) {
        ResultStatus<String> resultStatus = new ResultStatus<>();

        if (this.tokens != null) {
            try {
                URL url = new URL(BackendConfig.apiUrl + BackendConfig.deploymentStage + action);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Authorization", "Bearer " + this.tokens.getIdToken());
                httpURLConnection.setRequestProperty(VERSION_CODE_HEADER, String.valueOf(versionModel.getVersionCode()));
                httpURLConnection.setRequestProperty(VERSION_NAME_HEADER, versionModel.getVersionName());
                httpURLConnection.setUseCaches(false);
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);

                OutputStream os = httpURLConnection.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
                osw.write(new ObjectMapper().writeValueAsString(body));
                osw.flush();
                osw.close();
                os.close();

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
                } else if (responseCode == versionUpgradeCode) {
                    resultStatus.setData("You must upgrade your version of LiteWeight to continue.");
                    resultStatus.setOutDatedVersion(true);
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
            } catch (Exception e) {
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
