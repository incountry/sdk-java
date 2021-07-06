package com.incountry.residence.sdk.tools.http.impl;

import com.incountry.residence.sdk.tools.ValidationHelper;
import com.incountry.residence.sdk.tools.containers.RequestParameters;
import com.incountry.residence.sdk.tools.containers.ApiResponse;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.http.HttpExecutor;
import com.incountry.residence.sdk.tools.http.TokenClient;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpExecutorImpl extends ThrottlingRequestExecutor implements HttpExecutor {

    private static final Logger LOG = LogManager.getLogger(HttpExecutorImpl.class);
    private static final ValidationHelper HELPER = new ValidationHelper(LOG);

    private static final String MSG_SERVER_ERROR = "Server request error: [URL=%s, method=%s]";
    private static final String MSG_URL_NULL_ERR = "URL can't be null";
    private static final String MSG_REQ_PARAMS_NULL_ERR = "Request parameters can't be null";

    private static final String BEARER = "Bearer ";
    private static final String AUTHORIZATION = "Authorization";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String ENV_ID = "x-env-id";
    private static final String ATTACHMENTS = "attachments";
    private static final String META = "meta";
    private static final String METHOD_GET = "GET";
    private static final String CONTENT_DISPOSITION = "Content-disposition";

    private final TokenClient tokenClient;
    private final String environmentId;


    public HttpExecutorImpl(TokenClient tokenClient, String environmentId, CloseableHttpClient httpClient, int retryBaseDelay, int retryMaxDelay) {
        super(httpClient, retryBaseDelay, retryMaxDelay);
        if (LOG.isDebugEnabled()) {
            LOG.debug("HttpAgentImpl constructor params (tokenClient={}, environmentId={}, retryBaseDelay={}, retryMaxDelay={})",
                    tokenClient,
                    environmentId != null ? "[SECURE[" + environmentId.hashCode() + "]]" : null,
                    retryBaseDelay,
                    retryMaxDelay);
        }
        this.tokenClient = tokenClient;
        this.environmentId = environmentId;
    }

    @SuppressWarnings("java:S2142")
    @Override
    public ApiResponse request(String url, String body, String audience, String region, int retryCount,
                               RequestParameters requestParameters) throws StorageServerException, StorageClientException {
        HELPER.check(StorageClientException.class, url == null, MSG_URL_NULL_ERR);
        HELPER.check(StorageClientException.class, requestParameters == null, MSG_REQ_PARAMS_NULL_ERR);
        String method = requestParameters.getMethod();
        try {
            HttpRequestBase request = createRequest(url, method, body, requestParameters);
            addHeaders(request, audience, region, requestParameters.getContentType(), requestParameters.getDataStream() != null);
            try (CloseableHttpResponse response = executeWithDelay(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                HttpEntity responseEntity = response.getEntity();
                String fileName = null;
                boolean isSuccess = statusCode < 400;
                InputStream inputStream = null;
                String stringContent = null;
                boolean isFileDownload = isFileDownloadRequest(url, requestParameters.getMethod());
                if (isSuccess && ContentType.get(responseEntity) != null && isFileDownload) {
                    fileName = getFileName(response);
                    inputStream = new ByteArrayInputStream(EntityUtils.toByteArray(responseEntity));
                } else if (responseEntity != null) {
                    stringContent = EntityUtils.toString(responseEntity);
                }
                if (!isSuccess && canRetry(statusCode, retryCount)) {
                    tokenClient.refreshToken(true, audience, region);
                    return request(url, body, audience, region, retryCount - 1, requestParameters);
                }
                return new ApiResponse(stringContent, statusCode, fileName, inputStream);
            }
        } catch (StorageServerException | StorageClientException ex) {
            throw ex;
        } catch (Exception ex) {
            String errorMessage = String.format(MSG_SERVER_ERROR, url, method);
            LOG.error(errorMessage, ex);
            throw new StorageServerException(errorMessage, ex);
        }
    }

    private String getFileName(CloseableHttpResponse response) throws UnsupportedEncodingException {
        String fileName = null;
        Header[] contentDispositionHeader = response.getHeaders(CONTENT_DISPOSITION);
        if (contentDispositionHeader.length != 0) {
            Pattern pattern = Pattern.compile(".*filename\\*=UTF-8\\'\\'(.*)");
            Matcher matcher = pattern.matcher(contentDispositionHeader[0].getValue());
            matcher.matches();
            fileName = URLDecoder.decode(matcher.group(1), StandardCharsets.UTF_8.name());
        }
        return fileName;
    }

    private boolean isFileDownloadRequest(String url, String method) {
        return url.contains(ATTACHMENTS) && !url.endsWith(META) && method.equals(METHOD_GET);
    }

    private void addHeaders(HttpRequestBase request, String audience, String region, String contentType, boolean isFileUpload) throws StorageServerException {
        if (audience != null) {
            request.addHeader(AUTHORIZATION, BEARER + tokenClient.refreshToken(false, audience, region));
        }
        if (!isFileUpload) {
            request.addHeader(CONTENT_TYPE, contentType);
        }
        request.addHeader(ENV_ID, environmentId);
    }

    private boolean canRetry(int statusCode, int retryCount) {
        return statusCode == 401 && retryCount > 0;
    }
}
