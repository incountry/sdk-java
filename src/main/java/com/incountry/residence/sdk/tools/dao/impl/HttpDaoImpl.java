package com.incountry.residence.sdk.tools.dao.impl;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.incountry.residence.sdk.tools.ValidationHelper;
import com.incountry.residence.sdk.tools.containers.RequestParameters;
import com.incountry.residence.sdk.tools.containers.ApiResponse;
import com.incountry.residence.sdk.dto.AttachedFile;
import com.incountry.residence.sdk.dto.AttachmentMeta;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.dao.Dao;
import com.incountry.residence.sdk.tools.dao.POP;
import com.incountry.residence.sdk.tools.http.HttpExecutor;
import com.incountry.residence.sdk.tools.transfer.TransferFilterContainer;
import com.incountry.residence.sdk.tools.transfer.TransferFindResult;
import com.incountry.residence.sdk.tools.transfer.TransferPop;
import com.incountry.residence.sdk.tools.transfer.TransferPopList;
import com.incountry.residence.sdk.tools.transfer.TransferRecord;
import com.incountry.residence.sdk.tools.transfer.TransferRecordList;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static com.incountry.residence.sdk.tools.ValidationHelper.isNullOrEmpty;


public class HttpDaoImpl implements Dao {
    private static final Logger LOG = LogManager.getLogger(HttpDaoImpl.class);
    private static final ValidationHelper HELPER = new ValidationHelper(LOG);

    private static final int RETRY_CNT = 1;
    private static final String DEFAULT_ENDPOINT = "https://us-mt-01.api.incountry.io";
    private static final String DEFAULT_COUNTRY_ENDPOINT = "https://portal-backend.incountry.com/countries";
    private static final String DEFAULT_ENDPOINT_MASK = "-mt-01.api.incountry.io";
    private static final String DEFAULT_COUNTRY = "us";
    private static final String DEFAULT_REGION = "emea";
    private static final String STORAGE_URL = "v2/storage/records";
    private static final String URI_HTTPS = "https://";
    private static final String URI_FIND = "/find";
    private static final String URI_BATCH_WRITE = "/batchWrite";
    private static final String URI_HEALTH_CHECK = "/healthcheck";
    private static final String URI_META = "meta";
    private static final String URI_DELIMITER = "/";
    private static final String URI_ATTACHMENTS = "attachments";
    private static final String METHOD_POST = "POST";
    private static final String METHOD_PUT = "PUT";
    private static final String METHOD_PATCH = "PATCH";
    private static final String METHOD_GET = "GET";
    private static final String METHOD_DELETE = "DELETE";
    private static final long DEFAULT_UPDATE_INTERVAL = 300_000;

    private static final String MSG_ERR_LOAD_COUNTRIES = "Error during country list loading";
    private static final String MSG_ERR_COUNTRIES_ARE_EMPTY = "Country list is empty";
    private static final String MSG_ERR_PARSE_RESPONSE = "Response parse error";
    private static final String MSG_ERR_UNEXPECTED_RESPONSE = "Unexpected server response";
    private static final String MSG_ERR_CONTENT = "Code=%d, url=[%s], content=[%s]";
    private static final String MSG_ERR_UNSUPPORTED_COUNTRY = "Country [%s] is not supported";


    private Map<String, POP> popMap = new HashMap<>();

    private final HttpExecutor httpExecutor;
    private final String endPointUrl;
    private final String endPointMask;
    private final boolean isDefaultEndpoint;
    private final String countriesEndpoint;
    private final Gson gson;
    private final Gson gsonWithNull;
    private final AtomicLong lastLoadedTime = new AtomicLong(0);

    public HttpDaoImpl(String endPoint, String endpointMask, String countriesEndpoint, HttpExecutor agent) {
        this.gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                .create();
        this.gsonWithNull = new GsonBuilder()
                .serializeNulls()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                .create();
        this.isDefaultEndpoint = (endPoint == null);
        this.endPointUrl = isDefaultEndpoint ? DEFAULT_ENDPOINT : endPoint;
        this.countriesEndpoint = countriesEndpoint == null ? DEFAULT_COUNTRY_ENDPOINT : countriesEndpoint;
        this.endPointMask = endpointMask;
        this.httpExecutor = agent;
        if (isDefaultEndpoint) {
            loadCountries();
        }
    }

    private void loadCountries() {
        //update country list cache every 5 min
        if (System.currentTimeMillis() - lastLoadedTime.get() < DEFAULT_UPDATE_INTERVAL) {
            return;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Start loading country list");
        }
        synchronized (lastLoadedTime) {
            if (System.currentTimeMillis() - lastLoadedTime.get() < DEFAULT_UPDATE_INTERVAL) {
                return;
            }
            try {
                ApiResponse response = httpExecutor.request(countriesEndpoint, null, null, null, RETRY_CNT, new RequestParameters(METHOD_GET));
                if (response.getResponseCode() == 200) {
                    String content = response.getContent();
                    ConcurrentHashMap<String, POP> newCountryMap = new ConcurrentHashMap<>(getCountryList(content, URI_HTTPS, endPointMask != null ? endPointMask : DEFAULT_ENDPOINT_MASK));
                    if (newCountryMap.size() > 0) {
                        popMap = newCountryMap;
                    }
                } else {
                    throw generateServerException(response, countriesEndpoint, false);
                }
            } catch (StorageException ex) {
                LOG.error(MSG_ERR_LOAD_COUNTRIES, ex);
            }
            lastLoadedTime.set(System.currentTimeMillis());
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loaded country list: {}", popMap.keySet());
        }
    }

    public static Map<String, POP> getCountryList(String response, String uriStart, String uriEnd) throws StorageServerException {
        TransferPopList popList;
        try {
            popList = new Gson().fromJson(response, TransferPopList.class);
        } catch (JsonSyntaxException ex) {
            throw new StorageServerException(MSG_ERR_PARSE_RESPONSE, ex);
        }
        Map<String, POP> result = new HashMap<>();
        TransferPopList.validatePopList(popList);
        for (TransferPop transferPop : popList.getCountries()) {
            result.put(transferPop.getId(), new POP(uriStart + transferPop.getId() + uriEnd, transferPop.getName(), transferPop.getRegion(), transferPop.isDirect()));
        }
        return result;
    }

    private EndPoint getEndpoint(String country) throws StorageServerException, StorageClientException {
        if (isDefaultEndpoint) {
            POP pop = getPop(country);
            HELPER.check(StorageClientException.class, pop == null, MSG_ERR_UNSUPPORTED_COUNTRY, country);
            if (pop.isMidPop()) { //mid pop for default endpoint
                return new EndPoint(pop.getHost(), pop.getHost(), pop.getRegion(DEFAULT_REGION));
            }
            String mainUrl = URI_HTTPS + DEFAULT_COUNTRY + (endPointMask == null ? DEFAULT_ENDPOINT_MASK : endPointMask);
            return new EndPoint(mainUrl, getAudienceForMiniPop(mainUrl, country), DEFAULT_REGION);
        }
        return new EndPoint(endPointUrl, getAudienceForMiniPop(endPointUrl, country), DEFAULT_REGION);
    }

    private POP getPop(String country) throws StorageServerException {
        loadCountries();
        Map<String, POP> tempMap = popMap;
        if (popMap.isEmpty()) {
            throw new StorageServerException(MSG_ERR_COUNTRIES_ARE_EMPTY);
        }
        return tempMap.get(country);
    }

    private String getAudienceForMiniPop(String mainUrl, String country) {
        String mask = endPointMask;
        if (isDefaultEndpoint && mask == null) {
            mask = DEFAULT_ENDPOINT_MASK;
        }
        if (mask == null) {
            return mainUrl;
        } else {
            String secondaryUrl = URI_HTTPS + country + mask;
            String resultAudience = mainUrl;
            if (!mainUrl.equals(secondaryUrl)) {
                resultAudience += " " + secondaryUrl;
            }
            return resultAudience;
        }
    }

    @Override
    public TransferRecord createRecord(String country, TransferRecord transferRecord) throws StorageClientException, StorageServerException {
        String lowerCountry = country.toLowerCase();
        EndPoint endPoint = getEndpoint(lowerCountry);
        String url = getRecordActionUrl(endPoint.mainUrl, lowerCountry);
        String body = gson.toJson(transferRecord);
        ApiResponse response = httpExecutor.request(url, body, endPoint.audience, endPoint.region, RETRY_CNT, new RequestParameters(METHOD_POST));
        if (response.getResponseCode() == 201) {
            return containsWrittenResponse(response.getContent()) ? gson.fromJson(response.getContent(), TransferRecord.class) : null;
        }
        throw generateServerException(response, url, true);
    }

    private boolean containsWrittenResponse(String content) {
        return !isNullOrEmpty(content) && content.contains("record_key");
    }

    private StorageServerException generateServerException(ApiResponse response, String url, boolean logError) {
        StorageServerException exception = new StorageServerException(
                String.format(MSG_ERR_CONTENT, response.getResponseCode(), url, response.getContent())
                        .replaceAll("[\r\n]", ""));
        if (logError) {
            LOG.error(MSG_ERR_UNEXPECTED_RESPONSE, exception);
        }
        return exception;
    }

    @Override
    public TransferRecordList createBatch(String country, List<TransferRecord> records) throws StorageClientException, StorageServerException {
        String lowerCountry = country.toLowerCase();
        String body = gson.toJson(new TransferRecordList(records));
        EndPoint endPoint = getEndpoint(lowerCountry);
        String url = getRecordActionUrl(endPoint.mainUrl, lowerCountry, URI_BATCH_WRITE);
        ApiResponse response = httpExecutor.request(url, body, endPoint.audience, endPoint.region, RETRY_CNT, new RequestParameters(METHOD_POST));
        if (response.getResponseCode() == 201) {
            return containsWrittenResponse(response.getContent()) ? gson.fromJson(response.getContent(), TransferRecordList.class) : null;
        }
        throw generateServerException(response, url, true);
    }

    @Override
    public TransferRecord read(String country, String recordKey) throws StorageClientException, StorageServerException {
        String lowerCountry = country.toLowerCase();
        EndPoint endPoint = getEndpoint(lowerCountry);
        String url = getRecordUrl(endPoint.mainUrl, lowerCountry, recordKey);
        ApiResponse response = httpExecutor.request(url, null, endPoint.audience, endPoint.region, RETRY_CNT, new RequestParameters(METHOD_GET));
        if (response.getResponseCode() == 200) {
            return gson.fromJson(response.getContent(), TransferRecord.class);
        } else if (response.getResponseCode() == 404) {
            return null;
        }
        throw generateServerException(response, url, true);
    }

    @Override
    public void delete(String country, String recordKey) throws StorageServerException, StorageClientException {
        String lowerCountry = country.toLowerCase();
        EndPoint endPoint = getEndpoint(lowerCountry);
        String url = getRecordUrl(endPoint.mainUrl, lowerCountry, recordKey);
        ApiResponse response = httpExecutor.request(url, null, endPoint.audience, endPoint.region, RETRY_CNT, new RequestParameters(METHOD_DELETE));
        if (response.getResponseCode() == 200) {
            return;
        }
        throw generateServerException(response, url, true);
    }

    @Override
    public TransferFindResult find(String country, TransferFilterContainer filterContainer) throws StorageClientException, StorageServerException {
        String lowerCountry = country.toLowerCase();
        EndPoint endpoint = getEndpoint(lowerCountry);
        String url = getRecordActionUrl(endpoint.mainUrl, lowerCountry, URI_FIND);
        String postData = gsonWithNull.toJson(filterContainer);
        ApiResponse response = httpExecutor.request(url, postData, endpoint.audience, endpoint.region, RETRY_CNT, new RequestParameters(METHOD_POST));
        if (response.getResponseCode() == 200) {
            if (response.getContent() == null) {
                return new TransferFindResult();
            }
            return gson.fromJson(response.getContent(), TransferFindResult.class);
        }
        throw generateServerException(response, url, true);
    }

    @Override
    public AttachmentMeta addAttachment(String country, String recordKey, InputStream inputStream, String fileName, boolean upsert, String mimeType) throws StorageClientException, StorageServerException {
        String lowerCountry = country.toLowerCase();
        EndPoint endPoint = getEndpoint(lowerCountry);
        String url = getAttachmentUrl(endPoint.mainUrl, STORAGE_URL, lowerCountry, recordKey, URI_ATTACHMENTS);
        String method = upsert ? METHOD_PUT : METHOD_POST;
        ApiResponse response = httpExecutor.request(url, null, endPoint.audience, endPoint.region, RETRY_CNT, new RequestParameters(method, mimeType, inputStream, fileName));
        if (response.getResponseCode() == 201) {
            return gson.fromJson(response.getContent(), AttachmentMeta.class);
        }
        throw generateServerException(response, url, true);
    }

    @Override
    public void deleteAttachment(String country, String recordKey, String fileId) throws StorageClientException, StorageServerException {
        String lowerCountry = country.toLowerCase();
        EndPoint endPoint = getEndpoint(lowerCountry);
        String url = getAttachmentUrl(endPoint.mainUrl, STORAGE_URL, lowerCountry, recordKey, URI_ATTACHMENTS, fileId);
        ApiResponse response = httpExecutor.request(url, null, endPoint.audience, endPoint.region, RETRY_CNT, new RequestParameters(METHOD_DELETE));
        if (response.getResponseCode() == 204) {
            return;
        }
        throw generateServerException(response, url, true);
    }

    @Override
    public AttachedFile getAttachmentFile(String country, String recordKey, String fileId) throws StorageClientException, StorageServerException {
        String lowerCountry = country.toLowerCase();
        EndPoint endPoint = getEndpoint(lowerCountry);
        String url = getAttachmentUrl(endPoint.mainUrl, STORAGE_URL, lowerCountry, recordKey, URI_ATTACHMENTS, fileId);
        ApiResponse response = httpExecutor.request(url, null, endPoint.audience, endPoint.region, RETRY_CNT, new RequestParameters(METHOD_GET));
        if (response.getResponseCode() == 200) {
            InputStream content = response.getInputStream() == null ? null : response.getInputStream();
            String fileName = response.getFileName();
            return new AttachedFile(content, fileName);
        } else if (response.getResponseCode() == 404) {
            return null;
        }
        throw generateServerException(response, url, true);
    }

    @Override
    public AttachmentMeta updateAttachmentMeta(String country, String recordKey, String fileId, AttachmentMeta updatedMeta) throws StorageClientException, StorageServerException {
        String lowerCountry = country.toLowerCase();
        EndPoint endPoint = getEndpoint(lowerCountry);
        String url = getAttachmentUrl(endPoint.mainUrl, STORAGE_URL, lowerCountry, recordKey, URI_ATTACHMENTS, fileId, URI_META);
        ApiResponse response = httpExecutor.request(url, gson.toJson(updatedMeta), endPoint.audience, endPoint.region, RETRY_CNT, new RequestParameters(METHOD_PATCH));
        if (response.getResponseCode() == 200) {
            return gson.fromJson(response.getContent(), AttachmentMeta.class);
        }
        throw generateServerException(response, url, true);
    }

    @Override
    public AttachmentMeta getAttachmentMeta(String country, String recordKey, String fileId) throws StorageClientException, StorageServerException {
        String lowerCountry = country.toLowerCase();
        EndPoint endPoint = getEndpoint(lowerCountry);
        String url = getAttachmentUrl(endPoint.mainUrl, STORAGE_URL, lowerCountry, recordKey, URI_ATTACHMENTS, fileId, URI_META);
        ApiResponse response = httpExecutor.request(url, null, endPoint.audience, endPoint.region, RETRY_CNT, new RequestParameters(METHOD_GET));
        if (response.getResponseCode() == 200) {
            return gson.fromJson(response.getContent(), AttachmentMeta.class);
        } else if (response.getResponseCode() == 404) {
            return null;
        }
        throw generateServerException(response, url, true);
    }

    @Override
    public boolean healthCheck(String country) throws StorageServerException, StorageClientException {
        String lowerCountry = country.toLowerCase();
        EndPoint endPoint = getEndpoint(lowerCountry);
        String url = endPoint.mainUrl + URI_HEALTH_CHECK;
        ApiResponse response = httpExecutor.request(url, null, endPoint.audience, endPoint.region, RETRY_CNT, new RequestParameters(METHOD_GET));
        return response.getResponseCode() == 200;
    }

    private String getAttachmentUrl(String... urlParts) {
        return String.join(URI_DELIMITER, urlParts);
    }

    private String getRecordUrl(String endPoint, String country, String keyHash) {
        return endPoint +
                URI_DELIMITER +
                STORAGE_URL +
                URI_DELIMITER +
                country +
                URI_DELIMITER +
                keyHash;
    }

    private String getRecordActionUrl(String endpoint, String country, String... other) {
        StringBuilder builder = new StringBuilder(endpoint).append(URI_DELIMITER).append(STORAGE_URL).append(URI_DELIMITER);
        builder.append(country);
        for (String part : other) {
            builder.append(part);
        }
        return builder.toString();
    }

    private static class EndPoint {
        String mainUrl;
        String audience;
        String region;

        EndPoint(String mainUrl, String audience, String region) {
            this.mainUrl = mainUrl;
            this.audience = audience;
            this.region = region;
        }
    }
}
