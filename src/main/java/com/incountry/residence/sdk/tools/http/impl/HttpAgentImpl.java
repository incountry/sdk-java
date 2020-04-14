package com.incountry.residence.sdk.tools.http.impl;

import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.http.HttpAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

public class HttpAgentImpl implements HttpAgent {

    private static final Logger LOG = LoggerFactory.getLogger(HttpAgentImpl.class);
    private static final String MSG_SERVER_ERROR = "Server request error";


    private String apiKey;
    private String environmentId;
    private Charset charset;


    public HttpAgentImpl(String apiKey, String environmentId, Charset charset) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("HttpAgentImpl constructor params (apiKey={} , environmentId={} , charset={})",
                    apiKey != null ? "[SECURE[" + apiKey.hashCode() + "]]" : null,
                    environmentId != null ? "[SECURE[" + environmentId.hashCode() + "]]" : null,
                    charset);
        }
        this.apiKey = apiKey;
        this.environmentId = environmentId;
        this.charset = charset;
    }

    @Override
    public String request(String endpoint, String method, String body, boolean allowNone) throws StorageServerException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("HTTP request params (endpoint={} , method={} , allowNone={})",
                    endpoint,
                    method,
                    allowNone);
        }
        try {
            URL url = new URL(endpoint);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(method);
            con.setRequestProperty("Authorization", "Bearer " + apiKey);
            con.setRequestProperty("x-env-id", environmentId);
            con.setRequestProperty("Content-Type", "application/json");
            if (body != null) {
                con.setDoOutput(true);
                OutputStream os = con.getOutputStream();
                os.write(body.getBytes(charset));
                os.flush();
                os.close();
            }
            int status = con.getResponseCode();
            BufferedReader reader;
            if (status < 400) {
                reader = new BufferedReader(new InputStreamReader(con.getInputStream(), charset));
            } else {
                reader = new BufferedReader(new InputStreamReader(con.getErrorStream(), charset));
            }
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = reader.readLine()) != null) {
                content.append(inputLine);
            }
            reader.close();
            if (allowNone && status == 404) {
                return null;
            }
            if (status >= 400) {
                String error = status + " " + endpoint + " - " + content;
                if (LOG.isErrorEnabled()) {
                    LOG.error(error.replaceAll("[\r\n]", ""));
                }
                throw new StorageServerException(error);
            }
            return content.toString();
        } catch (IOException ex) {
            throw new StorageServerException(MSG_SERVER_ERROR + method, ex);
        }
    }
}
