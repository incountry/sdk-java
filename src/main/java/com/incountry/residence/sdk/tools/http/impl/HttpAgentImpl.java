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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;

public class HttpAgentImpl implements HttpAgent {

    private static final Logger LOG = LogManager.getLogger(HttpAgentImpl.class);
    private static final String MSG_SERVER_ERROR = "Server request error: %s";
    private static final String MSG_ERR_CONTENT = "Code=%d, endpoint=[%s], content=[%s]";

    private final TokenClient tokenClient;
    private final String environmentId;
    private final Charset charset;
    private final String userAgent;
    private final Integer timeout;


    public HttpAgentImpl(TokenClient tokenClient, String environmentId, Charset charset, Integer timeoutInMs) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("HttpAgentImpl constructor params (tokenClient={} , environmentId={} , charset={}, timeoutInMs={})",
                    tokenClient,
                    environmentId != null ? "[SECURE[" + environmentId.hashCode() + "]]" : null,
                    charset,
                    timeoutInMs);
        }
        this.tokenClient = tokenClient;
        this.environmentId = environmentId;
        this.charset = charset;
        this.timeout = timeoutInMs;
        this.userAgent = "SDK-Java/" + Version.BUILD_VERSION;
    }

    @Override
    public String request(String url, String method, String body, Map<Integer, ApiResponse> codeMap,
                          String audience, int retryCount) throws StorageServerException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("HTTP request params (url={} , method={} , codeMap={})",
                    url,
                    method,
                    codeMap);
        }
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod(method);
            con.setConnectTimeout(timeout);
            con.setReadTimeout(timeout);
            con.setRequestProperty("Authorization", "Bearer " + tokenClient.getToken(audience));
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
            InputStream responseStream;
            if (params != null && !params.isError()) {
                responseStream = con.getInputStream();
            } else if (params == null || !canRetry(params, retryCount)) {
                responseStream = con.getErrorStream();
            } else {
                tokenClient.refreshToken(true, audience);
                return request(url, method, body, codeMap, audience, retryCount - 1);
            }
            StringBuilder content = new StringBuilder();
            if (responseStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream, charset));
                String inputLine;
                while ((inputLine = reader.readLine()) != null) {
                    content.append(inputLine);
                }
                reader.close();
            }
            if (params != null && params.isIgnored()) {
                return null;
            }
            if (params == null || params.isError()) {
                String errorMessage = String.format(MSG_ERR_CONTENT, status, url, content.toString()).replaceAll("[\r\n]", "");
                LOG.error(errorMessage);
                throw new StorageServerException(errorMessage);
            }
            return content.toString();
        } catch (IOException ex) {
            throw new StorageServerException(String.format(MSG_SERVER_ERROR, method), ex);
        }
    }

    private boolean canRetry(ApiResponse params, int retryCount) {
        return params.isCanRetry() && retryCount > 0;
    }
}
