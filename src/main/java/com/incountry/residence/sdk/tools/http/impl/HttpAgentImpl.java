package com.incountry.residence.sdk.tools.http.impl;

import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.http.HttpAgent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpAgentImpl implements HttpAgent {
    private String apiKey;
    private String environmentId;

    public HttpAgentImpl(String apiKey, String environmentId) {
        this.apiKey = apiKey;
        this.environmentId = environmentId;
    }

    @Override
    public String request(String endpoint, String method, String body, boolean allowNone) throws IOException, StorageServerException {
        URL url = new URL(endpoint);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(method);
        con.setRequestProperty("Authorization", "Bearer "+apiKey);
        con.setRequestProperty("x-env-id", environmentId);
        con.setRequestProperty("Content-Type", "application/json");
        if (body != null){
            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();
            os.write(body.getBytes());
            os.flush();
            os.close();
        }
        int status = con.getResponseCode();
        BufferedReader in = null;
        if (status < 400) {
            in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        }
        else {
            in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
        }
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        if (allowNone && status == 404) return null;
        if (status >= 400)
            throw new StorageServerException(status + " " + endpoint + " - " + content);

        return content.toString();
    }
}
