package com.incountry.residence.sdk.tools.http.impl;

import com.incountry.residence.sdk.tools.dao.impl.ApiResponse;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.http.HttpAgent;
import com.incountry.residence.sdk.tools.http.TokenClient;
import com.incountry.residence.sdk.version.Version;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;

public class HttpAgentImpl implements HttpAgent {

    private static final Logger LOG = LogManager.getLogger(HttpAgentImpl.class);
    private static final String MSG_SERVER_ERROR = "Server request error";

    private final String environmentId;
    private final Charset charset;
    private final String userAgent;


    public HttpAgentImpl(String environmentId, Charset charset) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("HttpAgentImpl constructor params (environmentId={} , charset={})",
                    environmentId != null ? "[SECURE[" + environmentId.hashCode() + "]]" : null,
                    charset);
        }
        this.environmentId = environmentId;
        this.charset = charset;
        userAgent = "SDK-Java/" + Version.BUILD_VERSION;
    }

    @Override
    public String request(String url, String method, String body, Map<Integer, ApiResponse> codeMap,
                          TokenClient tokenClient, String popInstanceUrl, int retryCount) throws StorageServerException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("HTTP request params (url={} , method={} , codeMap={})",
                    url,
                    method,
                    codeMap);
        }
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod(method);
            con.setRequestProperty("Authorization", "Bearer " + tokenClient.getToken(popInstanceUrl));
            con.setRequestProperty("x-env-id", environmentId);
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("User-Agent", userAgent);
            if (body != null) {
                con.setDoOutput(true);
                OutputStream os = con.getOutputStream();
                os.write(body.getBytes(charset));
                os.flush();
                os.close();
            }
            int status = con.getResponseCode();
            ApiResponse params = codeMap.get(status);
            BufferedReader reader;
            if (params != null && !params.isError()) {
                reader = new BufferedReader(new InputStreamReader(con.getInputStream(), charset));
            } else if (params == null || !canRetry(params, retryCount)) {
                reader = new BufferedReader(new InputStreamReader(con.getErrorStream(), charset));
            } else {
                tokenClient.refreshToken(true, popInstanceUrl);
                return request(url, method, body, codeMap, tokenClient, popInstanceUrl, retryCount - 1);
            }
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = reader.readLine()) != null) {
                content.append(inputLine);
            }
            reader.close();
            if (params != null && params.isIgnored()) {
                return null;
            }
            if (params == null || params.isError()) {
                String errorMessage = (status + " " + url + " - " + content).replaceAll("[\r\n]", "");
                LOG.error(errorMessage);
                throw new StorageServerException(errorMessage);
            }
            return content.toString();
        } catch (IOException ex) {
            throw new StorageServerException(MSG_SERVER_ERROR + method, ex);
        }
    }

    private boolean canRetry(ApiResponse params, int retryCount) {
        return params.isCanRetry() && retryCount > 0;
    }
}
