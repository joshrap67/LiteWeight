package com.joshrap.liteweight.repositories;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.joshrap.liteweight.imports.BackendConfig;
import com.joshrap.liteweight.models.ErrorTypes;
import com.joshrap.liteweight.models.LiteWeightNetworkException;
import com.joshrap.liteweight.models.VersionModel;
import com.joshrap.liteweight.repositories.errorResponses.BadRequestResponse;
import com.joshrap.liteweight.repositories.errorResponses.ErrorResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import lombok.Data;

@Data
public class ApiGateway {

    private static final int versionUpgradeCode = 426; // THIS MUST MATCH THE CODE RETURNED FROM THE API
    private static final String VERSION_CODE_HEADER = "X-LiteWeight-Android-Version-Code";
    private static final String VERSION_NAME_HEADER = "X-LiteWeight-Version-Name";

    private final VersionModel versionModel;
    private final ObjectMapper objectMapper;

    @Inject
    public ApiGateway(VersionModel versionModel, ObjectMapper objectMapper) {
        this.versionModel = versionModel;
        this.objectMapper = objectMapper;
    }

    private String getToken() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Task<GetTokenResult> tokenResultTask = currentUser.getIdToken(true);
        try {
            Tasks.await(tokenResultTask);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        return tokenResultTask.getResult().getToken();
    }

    public String post(String route, BodyRequest body) throws IOException, LiteWeightNetworkException {
        URL url = new URL(BackendConfig.baseUrl + route);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("POST");
        setConnectionProperties(httpURLConnection, getToken());
        httpURLConnection.setRequestProperty("Content-Type", "application/json");
        httpURLConnection.setDoOutput(true);

        OutputStream os = httpURLConnection.getOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
        osw.write(body.toBody());
        osw.flush();
        osw.close();
        os.close();

        httpURLConnection.connect();
        String response = handleResponse(httpURLConnection);
        httpURLConnection.disconnect();

        return response;
    }

	public String put(String route, BodyRequest body) throws IOException, LiteWeightNetworkException {
        URL url = new URL(BackendConfig.baseUrl + route);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("PUT");
        setConnectionProperties(httpURLConnection, getToken());
        httpURLConnection.setRequestProperty("Content-Type", "application/json");
        if (body != null) {
            httpURLConnection.setDoOutput(true);

            OutputStream os = httpURLConnection.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
            osw.write(body.toBody());
            osw.flush();
            osw.close();
            os.close();
        }

        httpURLConnection.connect();
        String response = handleResponse(httpURLConnection);
        httpURLConnection.disconnect();

        return response;
    }

    public String put(String route) throws IOException, LiteWeightNetworkException {
        URL url = new URL(BackendConfig.baseUrl + route);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("PUT");
        setConnectionProperties(httpURLConnection, getToken());

        httpURLConnection.connect();
        String response = handleResponse(httpURLConnection);
        httpURLConnection.disconnect();

        return response;
    }

    public String get(String route) throws IOException, LiteWeightNetworkException {
        URL url = new URL(BackendConfig.baseUrl + route);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("GET");
        setConnectionProperties(httpURLConnection, getToken());
        httpURLConnection.setDoOutput(false);

        httpURLConnection.connect();
        String response = handleResponse(httpURLConnection);
        httpURLConnection.disconnect();

        return response;
    }

    public String delete(String route) throws IOException, LiteWeightNetworkException {
        URL url = new URL(BackendConfig.baseUrl + route);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("DELETE");
        setConnectionProperties(httpURLConnection, getToken());
        httpURLConnection.setDoOutput(false);

        httpURLConnection.connect();
        String response = handleResponse(httpURLConnection);
        httpURLConnection.disconnect();

        return response;
    }

    private String handleResponse(HttpURLConnection httpURLConnection) throws IOException, LiteWeightNetworkException {
        int responseCode = httpURLConnection.getResponseCode();
        if (responseCode >= HttpURLConnection.HTTP_OK && responseCode < 300) {
            return getJsonFromStream(httpURLConnection.getInputStream());
        } else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
            String jsonResponse = getJsonFromStream(httpURLConnection.getErrorStream());
            BadRequestResponse badRequestResponse = this.objectMapper.readValue(jsonResponse, BadRequestResponse.class);
            throw new LiteWeightNetworkException(badRequestResponse.getErrorType(), badRequestResponse.getMessage());
        } else {
            String jsonResponse = getJsonFromStream(httpURLConnection.getErrorStream());
            ErrorResponse errorResponse = this.objectMapper.readValue(jsonResponse, ErrorResponse.class);
            throw new LiteWeightNetworkException(ErrorTypes.serverError, errorResponse.getMessage());
        }
    }

    private void setConnectionProperties(HttpURLConnection httpURLConnection, String idToken) {
        httpURLConnection.setRequestProperty("Authorization", "Bearer " + idToken);
        httpURLConnection.setRequestProperty(VERSION_CODE_HEADER, String.valueOf(versionModel.getVersionCode()));
        httpURLConnection.setRequestProperty(VERSION_NAME_HEADER, versionModel.getVersionName());
        httpURLConnection.setUseCaches(false);
        httpURLConnection.setDoInput(true);
    }

    private String getJsonFromStream(InputStream stream) {
        StringBuilder jsonResponse = new StringBuilder();
        try (Reader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            int c;
            while ((c = reader.read()) != -1) {
                jsonResponse.append((char) c);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return jsonResponse.toString();
    }
}
