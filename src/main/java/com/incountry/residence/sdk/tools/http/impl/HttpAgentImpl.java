package com.incountry.residence.sdk.tools.http.impl;

import com.incountry.residence.sdk.tools.dao.impl.ApiResponse;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.http.HttpAgent;
import com.incountry.residence.sdk.tools.http.TokenClient;
import com.incountry.residence.sdk.version.Version;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class HttpAgentImpl implements HttpAgent {

    private static final Logger LOG = LogManager.getLogger(HttpAgentImpl.class);
    private static final String MSG_SERVER_ERROR = "Server request error: %s";
    private static final String MSG_ERR_CONTENT = "Code=%d, endpoint=[%s], content=[%s]";
    private static final String MSG_ERR_URL = "URL error";
    private static final String MSG_ERR_SERVER_REQUES = "Server request error: %s";
    private static final String MSG_ERR_NULL_BODY = "Body must not be null";
    private static final String POST = "POST";
    private static final String GET = "GET";

    private final TokenClient tokenClient;
    private final String environmentId;
    private final String userAgent;
    private final CloseableHttpClient httpClient;


    public HttpAgentImpl(TokenClient tokenClient, String environmentId, CloseableHttpClient httpClient) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("HttpAgentImpl constructor params (tokenClient={} , environmentId={})",
                    tokenClient,
                    environmentId != null ? "[SECURE[" + environmentId.hashCode() + "]]" : null);
        }
        this.tokenClient = tokenClient;
        this.environmentId = environmentId;
        this.userAgent = "SDK-Java/" + Version.BUILD_VERSION;
        this.httpClient = httpClient;
    }

    private HttpRequestBase addHeaders(HttpRequestBase request, String audience, String region) throws StorageServerException {
        if (audience != null) {
            request.addHeader("Authorization", "Bearer " + tokenClient.getToken(audience, region));
        }
        request.addHeader("Content-Type", "application/json");
        request.addHeader("x-env-id", environmentId);
        request.addHeader("User-Agent", userAgent);

        return request;
    }

    @Override
    public String request(String url, String method, String body, Map<Integer, ApiResponse> codeMap,
                          String audience, String region, int retryCount) throws StorageServerException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("HTTP request params (url={} , method={} , codeMap={})",
                    url,
                    method,
                    codeMap);
        }
        try {
            if (url == null) {
                throw new StorageServerException(String.format(MSG_SERVER_ERROR, method), new NullPointerException("Url must not be null."));
            }
            HttpRequestBase request = addHeaders(createRequest(url, method, body), audience, region);
            CloseableHttpResponse response = httpClient.execute(request);

            int status = response.getStatusLine().getStatusCode();
            String responseContent = EntityUtils.toString(response.getEntity());
            String result;
            ApiResponse params = codeMap.get(status);
            if ((params != null && !params.isError() && responseContent != null)
                    || (params == null || !canRetry(params, retryCount))) {
                result = responseContent;
            } else {
                tokenClient.refreshToken(true, audience, region);
                return request(url, method, body, codeMap, audience, region, retryCount - 1);
            }

            if (params != null && params.isIgnored()) {
                response.close();
                return null;
            }
            if (params == null || params.isError()) {
                String errorMessage = String.format(MSG_ERR_CONTENT, status, url, result).replaceAll("[\r\n]", "");
                LOG.error(errorMessage);
                throw new StorageServerException(errorMessage);
            }
            response.close();
            return result;

        } catch (IOException ex) {
            throw new StorageServerException(String.format(MSG_SERVER_ERROR, method), ex);
        }
    }

    private boolean canRetry(ApiResponse params, int retryCount) {
        return params.isCanRetry() && retryCount > 0;
    }

    public static HttpRequestBase createRequest(String url, String method, String body) throws UnsupportedEncodingException, StorageServerException {
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException ex) {
            throw new StorageServerException(MSG_ERR_URL, ex);
        }

        if (method.equals(POST)) {
            if (body == null) {
                LOG.error(MSG_ERR_NULL_BODY);
                throw new StorageServerException(String.format(MSG_ERR_SERVER_REQUES, method), new NullPointerException(MSG_ERR_NULL_BODY));
            }
            HttpPost request = new HttpPost(uri);
            StringEntity entity = new StringEntity(body);
            request.setEntity(entity);
            return request;
        } else if (method.equals(GET)) {
            return new HttpGet(uri);
        } else {
            return new HttpDelete(uri);
        }
    }
}
