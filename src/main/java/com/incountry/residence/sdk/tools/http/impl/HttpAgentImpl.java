package com.incountry.residence.sdk.tools.http.impl;

import com.incountry.residence.sdk.tools.models.PopApiResponse;
import com.incountry.residence.sdk.tools.dao.impl.ApiResponse;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.http.HttpAgent;
import com.incountry.residence.sdk.tools.http.TokenClient;
import com.incountry.residence.sdk.version.Version;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.util.Map;

public class HttpAgentImpl extends AbstractHttpRequestCreator implements HttpAgent {

    private static final Logger LOG = LogManager.getLogger(HttpAgentImpl.class);

    private static final String MSG_SERVER_ERROR = "Server request error: [URL=%s, method=%s]";
    private static final String MSG_URL_NULL_ERR = "URL can't be null";
    private static final String MSG_ERR_CONTENT = "Code=%d, endpoint=[%s], content=[%s]";
    private static final String BEARER = "Bearer ";
    private static final String AUTHORIZATION = "Authorization";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String ENV_ID = "x-env-id";
    private static final String USER_AGENT = "User-Agent";

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

    @Override
    public PopApiResponse request(String url, String method, String body, Map<Integer, ApiResponse> codeMap,
                                  String audience, String region, int retryCount, String contentType) throws StorageServerException, StorageClientException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("HTTP request params (url={} , method={} , codeMap={})",
                    url,
                    method,
                    codeMap);
        }
        if (url == null) {
            throw new StorageClientException(MSG_URL_NULL_ERR);
        }
        try {
            HttpRequestBase request = createRequest(url, method, body);
            addHeaders(request, audience, region, contentType);
            CloseableHttpResponse response = httpClient.execute(request);

            int status = response.getStatusLine().getStatusCode();
            String fileExtension = null;
            HttpEntity responseEntity = response.getEntity();
            if (ContentType.get(responseEntity) != null) {
                fileExtension = ContentType.get(responseEntity).getMimeType().split("/")[1];
            }
            String actualResponseContent = EntityUtils.toString(response.getEntity());
            response.close();
            ApiResponse expectedResponse = codeMap.get(status);
            boolean isSuccess = expectedResponse != null && !expectedResponse.isError() && !actualResponseContent.isEmpty();
            boolean isFinish = isSuccess || expectedResponse == null || !canRetry(expectedResponse, retryCount);
            if (!isFinish) {
                tokenClient.refreshToken(true, audience, region);
                return request(url, method, body, codeMap, audience, region, retryCount - 1, contentType);
            }
            if (expectedResponse != null && expectedResponse.isIgnored()) {
                return new PopApiResponse(null);
            }
            if (expectedResponse == null || expectedResponse.isError()) {
                String errorMessage = String.format(MSG_ERR_CONTENT, status, url, actualResponseContent).replaceAll("[\r\n]", "");
                LOG.error(errorMessage);
                throw new StorageServerException(errorMessage);
            }
            return new PopApiResponse(actualResponseContent, fileExtension);
        } catch (IOException ex) {
            String errorMessage = String.format(MSG_SERVER_ERROR, url, method);
            throw new StorageServerException(errorMessage, ex);
        }
    }

    private HttpRequestBase addHeaders(HttpRequestBase request, String audience, String region, String contentType) throws StorageServerException {
        if (audience != null) {
            request.addHeader(AUTHORIZATION, BEARER + tokenClient.getToken(audience, region));
        }
        request.addHeader(CONTENT_TYPE, contentType);
        request.addHeader(ENV_ID, environmentId);
        request.addHeader(USER_AGENT, userAgent);

        return request;
    }

    private boolean canRetry(ApiResponse params, int retryCount) {
        return params.isCanRetry() && retryCount > 0;
    }
}
