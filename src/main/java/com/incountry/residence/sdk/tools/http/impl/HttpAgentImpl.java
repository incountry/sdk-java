package com.incountry.residence.sdk.tools.http.impl;

import com.incountry.residence.sdk.tools.NullChecker;
import com.incountry.residence.sdk.tools.dao.impl.ApiResponseCodes;
import com.incountry.residence.sdk.tools.containers.MetaInfoTypes;
import com.incountry.residence.sdk.tools.containers.RequestParameters;
import com.incountry.residence.sdk.tools.containers.ApiResponse;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.http.HttpAgent;
import com.incountry.residence.sdk.tools.http.TokenClient;
import com.incountry.residence.sdk.version.Version;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpAgentImpl extends AbstractHttpRequestCreator implements HttpAgent {

    private static final Logger LOG = LogManager.getLogger(HttpAgentImpl.class);

    private static final String MSG_SERVER_ERROR = "Server request error: [URL=%s, method=%s]";
    private static final String MSG_URL_NULL_ERR = "URL can't be null";
    private static final String MSG_REQ_PARAMS_NULL_ERR = "Request parameters can't be null";
    private static final String MSG_ERR_CONTENT = "Code=%d, endpoint=[%s], content=[%s]";
    private static final String BEARER = "Bearer ";
    private static final String AUTHORIZATION = "Authorization";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String ENV_ID = "x-env-id";
    private static final String USER_AGENT = "User-Agent";
    private static final String ATTACHMENTS = "attachments";
    private static final String META = "meta";
    private static final String METHOD_GET = "GET";
    private static final String CONTENT_DISPOSITION = "Content-disposition";

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
    public ApiResponse request(String url, String body,
                               String audience, String region, int retryCount, RequestParameters requestParameters) throws StorageServerException, StorageClientException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("HTTP request params (url={}, audience={}, region={}, retryCount={}, httpParameters={})",
                    url,
                    audience,
                    region,
                    retryCount,
                    requestParameters);
        }
        NullChecker.checkNull(LOG, url, new StorageClientException(MSG_URL_NULL_ERR), MSG_URL_NULL_ERR);
        NullChecker.checkNull(LOG, requestParameters, new StorageClientException(MSG_REQ_PARAMS_NULL_ERR), MSG_REQ_PARAMS_NULL_ERR);
        String method = requestParameters.getMethod();
        Map<Integer, ApiResponseCodes> codeMap = requestParameters.getCodeMap();
        try {
            HttpRequestBase request = createRequest(url, method, body, requestParameters);
            addHeaders(request, audience, region, requestParameters.getContentType(), requestParameters.isFileUpload());
            CloseableHttpResponse response = httpClient.execute(request);

            int status = response.getStatusLine().getStatusCode();
            HttpEntity responseEntity = response.getEntity();
            Map<MetaInfoTypes, String> metaInfo = new EnumMap<>(MetaInfoTypes.class);
            if (ContentType.get(responseEntity) != null && isFileDownloadRequest(url, requestParameters.getMethod())) {
                metaInfo = getResponseMetaInfo(response);
            }
            String actualResponseContent = "";
            if (response.getEntity() != null) {
                actualResponseContent = EntityUtils.toString(response.getEntity());
            }
            response.close();
            ApiResponseCodes expectedResponse = codeMap.get(status);
            boolean isSuccess = expectedResponse != null && !expectedResponse.isError() && !actualResponseContent.isEmpty();
            boolean isFinish = isSuccess || expectedResponse == null || !canRetry(expectedResponse.isCanRetry(), retryCount);
            if (!isFinish) {
                tokenClient.refreshToken(true, audience, region);
                return request(url, body, audience, region, retryCount - 1, requestParameters);
            }
            if (expectedResponse != null && expectedResponse.isIgnored()) {
                return new ApiResponse();
            }
            if (expectedResponse == null || expectedResponse.isError()) {
                String errorMessage = String.format(MSG_ERR_CONTENT, status, url, actualResponseContent).replaceAll("[\r\n]", "");
                LOG.error(errorMessage);
                throw new StorageServerException(errorMessage);
            }
            ApiResponse apiResponse = new ApiResponse(actualResponseContent, metaInfo);
            if (LOG.isTraceEnabled()) {
                LOG.trace("HTTP response={}",
                        apiResponse);
            }
            return apiResponse;
        } catch (IOException ex) {
            String errorMessage = String.format(MSG_SERVER_ERROR, url, method);
            throw new StorageServerException(errorMessage, ex);
        }
    }

    private Map<MetaInfoTypes, String> getResponseMetaInfo(CloseableHttpResponse response) throws UnsupportedEncodingException {
        Header[] contentDispositionHeader = response.getHeaders(CONTENT_DISPOSITION);
        Map<MetaInfoTypes, String> metaInfo = new EnumMap<>(MetaInfoTypes.class);
        if (contentDispositionHeader.length != 0) {
            Pattern pattern = Pattern.compile(".*filename\\*=UTF-8\\'\\'(.*)");
            Matcher matcher = pattern.matcher(contentDispositionHeader[0].getValue());
            matcher.matches();
            String fileName = URLDecoder.decode(matcher.group(1), StandardCharsets.UTF_8.name());
            metaInfo.put(MetaInfoTypes.NAME, fileName);
            metaInfo.put(MetaInfoTypes.EXTENSION, FilenameUtils.getExtension(fileName));
        }
        return metaInfo;
    }

    private boolean isFileDownloadRequest(String url, String method) {
        return url.contains(ATTACHMENTS) && !url.endsWith(META) && method.equals(METHOD_GET);
    }

    private HttpRequestBase addHeaders(HttpRequestBase request, String audience, String region, String contentType, boolean isFileUpload) throws StorageServerException {
        if (audience != null) {
            request.addHeader(AUTHORIZATION, BEARER + tokenClient.getToken(audience, region));
        }
        if (!isFileUpload) {
            request.addHeader(CONTENT_TYPE, contentType);
        }
        request.addHeader(ENV_ID, environmentId);
        request.addHeader(USER_AGENT, userAgent);

        return request;
    }

    private boolean canRetry(boolean isCanRetry, int retryCount) {
        return isCanRetry && retryCount > 0;
    }
}
