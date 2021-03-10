package com.incountry.residence.sdk.http.mocks;

import com.incountry.residence.sdk.tools.dao.impl.ApiResponseCodes;
import com.incountry.residence.sdk.tools.containers.MetaInfoTypes;
import com.incountry.residence.sdk.tools.containers.RequestParameters;
import com.incountry.residence.sdk.tools.containers.ApiResponse;
import com.incountry.residence.sdk.tools.http.HttpAgent;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FakeHttpAgent implements HttpAgent {

    private String callUrl;
    private String callMethod;
    private String callBody;
    private String response;
    private String callRegion;
    private List<String> responseList;
    private Map<Integer, ApiResponseCodes> codeMap;
    private int retryCount;
    private String audienceUrl;
    private InputStream dataStream;
    private Map<MetaInfoTypes, String> metaInfo = new HashMap<>();
    private InputStream inputStream;

    public FakeHttpAgent(String response) {
        this.response = response;
    }

    public FakeHttpAgent(List<String> responseList) {
        this.responseList = responseList;
    }

    public FakeHttpAgent(String response, Map<MetaInfoTypes, String> metaInfo, InputStream inputStream) {
        this.response = response;
        this.metaInfo = metaInfo;
        this.inputStream = inputStream;
    }

    @Override
    public ApiResponse request(String url, String body, String audience, String region, int retryCount, RequestParameters requestParameters) {
        this.callUrl = url;
        this.callMethod = requestParameters.getMethod();
        this.callBody = body;
        this.codeMap = requestParameters.getCodeMap();
        this.retryCount = retryCount;
        this.audienceUrl = audience;
        this.callRegion = region;
        this.dataStream = requestParameters.getDataStream();
        return new ApiResponse(getResponse(), metaInfo, inputStream);
    }

    public InputStream getDataStream() {
        return dataStream;
    }

    public String getCallUrl() {
        return callUrl;
    }

    public String getCallMethod() {
        return callMethod;
    }

    public String getCallBody() {
        return callBody;
    }

    public String getAudienceUrl() {
        return audienceUrl;
    }

    public Map<Integer, ApiResponseCodes> getCodeMap() {
        return codeMap;
    }

    public String getCallRegion() {
        return callRegion;
    }

    public String getResponse() {
        if (responseList != null && !responseList.isEmpty()) {
            response = responseList.get(0);
            if (responseList.size() == 1) {
                responseList = null;
            } else {
                responseList = responseList.subList(1, responseList.size());
            }
        }
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public int getRetryCount() {
        return retryCount;
    }
}
