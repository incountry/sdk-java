package com.incountry.residence.sdk.tools.http.impl;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.incountry.residence.sdk.tools.dao.impl.ApiResponse;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.http.HttpAgent;
import com.incountry.residence.sdk.tools.http.TokenClient;
import com.incountry.residence.sdk.tools.http.utils.HttpUtils;
import com.incountry.residence.sdk.version.Version;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
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
    private final HttpRequestFactory requestFactory;


    public HttpAgentImpl(TokenClient tokenClient, String environmentId, Charset charset, Integer timeoutInMs, Integer poolSize) throws StorageServerException {
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
        this.requestFactory = HttpUtils.provideHttpRequestFactory(poolSize);
    }

    private HttpRequest addHeaders(HttpRequest request, String audience) throws StorageServerException {
        HttpHeaders headers = request.getHeaders();
        if (audience != null) {
            headers.setAuthorization("Bearer " + tokenClient.getToken(audience));
        }
        headers.setContentType("application/json");
        headers.set("x-env-id", environmentId);
        headers.set("User-Agent", userAgent);
        request.setHeaders(headers);
        return request;
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

        String result;
        try {
            if (body == null) {
                body = "";
            }
            HttpRequest request = HttpUtils.buildRequest(requestFactory, url, method, body, timeout);
            request = addHeaders(request, audience);

            RequestResult requestResult = executeRequest(request);
            Integer status = requestResult.first;
            String response = requestResult.second;

            ApiResponse params = codeMap.get(status);
            if ((params != null && !params.isError() && response != null)
                    || (params == null || !canRetry(params, retryCount))) {
                result = response;
            } else {
                tokenClient.refreshToken(true, audience);
                return request(url, method, body, codeMap, audience, retryCount - 1);
            }

            if (params != null && params.isIgnored()) {
                return null;
            }
            if (params == null || params.isError()) {
                String errorMessage = String.format(MSG_ERR_CONTENT, status, url, result).replaceAll("[\r\n]", "");
                LOG.error(errorMessage);
                throw new StorageServerException(errorMessage);
            }
            return result;

        } catch (IOException ex) {
            throw new StorageServerException(String.format(MSG_SERVER_ERROR, method), ex);
        } catch (IllegalArgumentException ex) {
            throw new StorageServerException(String.format(MSG_SERVER_ERROR, method), ex);
        }
    }

    private RequestResult executeRequest(HttpRequest request) throws IOException {
        try {
            HttpResponse response = request.execute();
            Integer status = response.getStatusCode();
            return new RequestResult(status, response.parseAsString());
        } catch (HttpResponseException ex) {
            Integer status = ex.getStatusCode();
            String errorMassage = ex.getMessage();
            return new RequestResult(status, errorMassage);
        }
    }

    private boolean canRetry(ApiResponse params, int retryCount) {
        return params.isCanRetry() && retryCount > 0;
    }

    static class RequestResult {
        public Integer first;
        public String second;

        RequestResult(Integer first, String second) {
            this.first = first;
            this.second = second;
        }
    }
}
