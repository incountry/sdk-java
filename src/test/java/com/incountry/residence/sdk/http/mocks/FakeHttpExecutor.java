package com.incountry.residence.sdk.http.mocks;

import com.incountry.residence.sdk.tools.containers.RequestParameters;
import com.incountry.residence.sdk.tools.containers.ApiResponse;
import com.incountry.residence.sdk.tools.http.HttpExecutor;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class FakeHttpExecutor implements HttpExecutor {

    private String callUrl;
    private String callMethod;
    private String callBody;
    private String response;
    private String callRegion;
    private List<Map.Entry<String, Integer>> responseList;
    private int retryCount;
    private String audienceUrl;
    private InputStream dataStream;
    private String fileName;
    private int responseCode;
    private InputStream inputStream;

    public FakeHttpExecutor(String response, int responseCode) {
        this.response = response;
        this.responseCode = responseCode;
    }

    public FakeHttpExecutor(List<Map.Entry<String, Integer>> responseList) {
        this.responseList = responseList;
    }

    public FakeHttpExecutor(String response, String fileName, InputStream inputStream, int responseCode) {
        this.response = response;
        this.fileName = fileName;
        this.inputStream = inputStream;
        this.responseCode = responseCode;
    }

    @Override
    public ApiResponse request(String url, String body, String audience, String region, int retryCount, RequestParameters requestParameters) {
        this.callUrl = url;
        this.callMethod = requestParameters.getMethod();
        this.callBody = body;
        this.retryCount = retryCount;
        this.audienceUrl = audience;
        this.callRegion = region;
        this.dataStream = requestParameters.getDataStream();
        return new ApiResponse(getResponse(), responseCode, fileName, inputStream);
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

    public String getCallRegion() {
        return callRegion;
    }

    public String getResponse() {
        if (responseList != null && !responseList.isEmpty()) {
            response = responseList.get(0).getKey();
            responseCode = responseList.get(0).getValue();
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

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public int getRetryCount() {
        return retryCount;
    }
}
