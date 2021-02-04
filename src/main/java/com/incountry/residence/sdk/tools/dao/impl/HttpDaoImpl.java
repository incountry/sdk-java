package com.incountry.residence.sdk.tools.dao.impl;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.tools.JsonUtils;
import com.incountry.residence.sdk.tools.containers.MetaInfoTypes;
import com.incountry.residence.sdk.tools.containers.RequestParameters;
import com.incountry.residence.sdk.tools.containers.ApiResponse;
import com.incountry.residence.sdk.dto.AttachedFile;
import com.incountry.residence.sdk.dto.AttachmentMeta;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.dao.Dao;
import com.incountry.residence.sdk.tools.dao.POP;
import com.incountry.residence.sdk.tools.http.HttpAgent;
import com.incountry.residence.sdk.tools.http.TokenClient;
import com.incountry.residence.sdk.tools.http.impl.HttpAgentImpl;
import com.incountry.residence.sdk.tools.proxy.ProxyUtils;
import com.incountry.residence.sdk.tools.transfer.TransferFilterContainer;
import com.incountry.residence.sdk.tools.transfer.TransferFindResult;
import com.incountry.residence.sdk.tools.transfer.TransferPop;
import com.incountry.residence.sdk.tools.transfer.TransferPopList;
import com.incountry.residence.sdk.tools.transfer.TransferRecord;
import org.apache.commons.io.IOUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;


public class HttpDaoImpl implements Dao {

    private static final Logger LOG = LogManager.getLogger(HttpDaoImpl.class);

    private static final String DEFAULT_ENDPOINT = "https://us-mt-01.api.incountry.io";
    private static final int RETRY_CNT = 1;
    private static final String DEFAULT_COUNTRY_ENDPOINT = "https://portal-backend.incountry.com/countries";
    private static final String DEFAULT_ENDPOINT_MASK = "-mt-01.api.incountry.io";
    private static final String DEFAULT_COUNTRY = "us";
    private static final String DEFAULT_REGION = "emea";
    private static final String STORAGE_URL = "v2/storage/records";
    private static final String URI_HTTPS = "https://";
    private static final String URI_POST = "POST";
    private static final String URI_PUT = "PUT";
    private static final String URI_PATCH = "PATCH";
    private static final String URI_GET = "GET";
    private static final String URI_DELETE = "DELETE";
    private static final String URI_FIND = "/find";
    private static final String URI_BATCH_WRITE = "/batchWrite";
    private static final String URI_META = "meta";
    private static final String URI_DELIMITER = "/";
    private static final String URI_ATTACHMENTS = "attachments";
    private static final long DEFAULT_UPDATE_INTERVAL = 300_000;
    private static final String MSG_ERR_LOAD_COUNTRIES = "Error during country list loading";
    private static final String MSG_ERR_COUNTRIES_ARE_EMPTY = "Country list is empty";
    private static final String MSG_ERR_USER_INPUT_STREAM = "User's InputStream reading error";
    private static final String MSG_ERR_RESPONSE = "Response parse error";


    private Map<String, POP> popMap = new HashMap<>();

    private final HttpAgent httpAgent;
    private final String endPointUrl;
    private final String endPointMask;
    private final boolean isDefaultEndpoint;
    private final String countriesEndpoint;
    private final AtomicLong lastLoadedTime = new AtomicLong(0);

    public HttpDaoImpl(String environmentId, String endPoint, String endpointMask, String countriesEndpoint, TokenClient tokenClient, CloseableHttpClient httpClient) {
        this(endPoint, endpointMask, countriesEndpoint,
                ProxyUtils.createLoggingProxyForPublicMethods(
                        new HttpAgentImpl(
                                ProxyUtils.createLoggingProxyForPublicMethods(tokenClient),
                                environmentId,
                                httpClient)));
    }

    public HttpDaoImpl(String endPoint, String endpointMask, String countriesEndpoint, HttpAgent agent) {
        isDefaultEndpoint = (endPoint == null);
        this.endPointUrl = isDefaultEndpoint ? DEFAULT_ENDPOINT : endPoint;
        this.countriesEndpoint = countriesEndpoint == null ? DEFAULT_COUNTRY_ENDPOINT : countriesEndpoint;
        this.endPointMask = endpointMask;
        this.httpAgent = agent;
        if (isDefaultEndpoint) {
            loadCountries();
        }
    }

    public static Map<String, POP> getMidiPops(String response, String uriStart, String uriEnd) throws StorageServerException {
        TransferPopList popList;
        try {
            popList = new Gson().fromJson(response, TransferPopList.class);
        } catch (JsonSyntaxException ex) {
            throw new StorageServerException(MSG_ERR_RESPONSE, ex);
        }
        Map<String, POP> result = new HashMap<>();
        TransferPopList.validatePopList(popList);
        for (TransferPop transferPop : popList.getCountries()) {
            if (transferPop.isDirect()) {
                result.put(transferPop.getId(), new POP(uriStart + transferPop.getId() + uriEnd, transferPop.getName(), transferPop.getRegion()));
            }
        }
        return result;
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
                ApiResponse response = httpAgent.request(countriesEndpoint, null, null, null, RETRY_CNT, new RequestParameters(URI_GET, ApiResponseCodes.COUNTRY));
                String content = response.getContent();
                ConcurrentHashMap<String, POP> newCountryMap = new ConcurrentHashMap<>(getMidiPops(content, URI_HTTPS, endPointMask != null ? endPointMask : DEFAULT_ENDPOINT_MASK));
                if (newCountryMap.size() > 0) {
                    popMap = newCountryMap;
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

    private EndPoint getEndpoint(String country) throws StorageServerException {
        if (isDefaultEndpoint) {
            POP pop = getMidPop(country);
            if (pop != null) { //mid pop for default endpoint
                return new EndPoint(pop.getHost(), pop.getHost(), pop.getRegion(DEFAULT_REGION));
            }
            String mainUrl = URI_HTTPS + DEFAULT_COUNTRY + (endPointMask == null ? DEFAULT_ENDPOINT_MASK : endPointMask);
            return new EndPoint(mainUrl, getAudienceForMiniPop(mainUrl, country), DEFAULT_REGION);
        }
        return new EndPoint(endPointUrl, getAudienceForMiniPop(endPointUrl, country), DEFAULT_REGION);
    }

    private POP getMidPop(String country) throws StorageServerException {
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
    public void createRecord(String country, TransferRecord transferRecord) throws StorageClientException, StorageCryptoException, StorageServerException {
        String lowerCountry = country.toLowerCase();
        EndPoint endPoint = getEndpoint(lowerCountry);
        String url = getRecordActionUrl(endPoint.mainUrl, lowerCountry);
        String body =  getGson4Records().toJson(transferRecord);
        httpAgent.request(url, body, endPoint.audience, endPoint.region, RETRY_CNT, new RequestParameters(URI_POST, ApiResponseCodes.WRITE));
    }

    private static Gson getGson4Records() {
        return new GsonBuilder()
                .setFieldNamingStrategy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
    }

    public static JsonObject toJson(Record record) throws StorageClientException, StorageCryptoException {
        Gson gson = getGson4Records();
        JsonObject recordJsonObj = (JsonObject) gson.toJsonTree(record);
//        if (cryptoManager == null) {
//            return recordJsonObj;
//        }
//        REMOVE_KEYS.forEach(recordJsonObj::remove);
        JsonObject bodyJsonObj = new JsonObject();
        if (record.getBody() != null) {
//            bodyJsonObj.addProperty(P_PAYLOAD, record.getBody());
        }
        JsonObject resultJson = (JsonObject) gson.toJsonTree(record);
        return resultJson;
    }

    @Override
    public void createBatch(String country, List<TransferRecord> records) throws StorageClientException, StorageServerException, StorageCryptoException {
//        String lowerCountry = country.toLowerCase();
//        String recListJson = JsonUtils.toJsonString(records, cryptoManager);
//        String recListJson = new Gson().toJson(records);

        JsonArray array = new JsonArray();
        for (Record record : records) {
            array.add(toJson(record));
        }
        JsonObject obj = new JsonObject();
        obj.add("records", array);

        String recListJson = obj.toString();
        EndPoint endPoint = getEndpoint(country);
        String url = getRecordActionUrl(endPoint.mainUrl, country, URI_BATCH_WRITE);
        httpAgent.request(url, recListJson, endPoint.audience, endPoint.region, RETRY_CNT, new RequestParameters(URI_POST, ApiResponseCodes.BATCH_WRITE));
    }

    @Override
    public TransferRecord read(String country, String recordKey) throws StorageClientException, StorageServerException, StorageCryptoException {
//        String lowerCountry = country.toLowerCase();
//        String key = cryptoManager != null ? cryptoManager.createKeyHash(recordKey) : recordKey;
        EndPoint endPoint = getEndpoint(country);
        String url = getRecordUrl(endPoint.mainUrl, country, recordKey);
        ApiResponse response = httpAgent.request(url, null, endPoint.audience, endPoint.region, RETRY_CNT, new RequestParameters(URI_GET, ApiResponseCodes.READ));
        if (response.getContent() == null) {
            return null;
        }
        return getGson4Records().fromJson(response.getContent(), TransferRecord.class);


//        return response.getContent() == null ? null : JsonUtils.recordFromString(response.getContent(), cryptoManager);
//        return new Record();
    }

    @Override
    public void delete(String country, String recordKey) throws StorageServerException, StorageClientException {
        String lowerCountry = country.toLowerCase();
//        String recordHash = cryptoManager != null ? cryptoManager.createKeyHash(key) : key;
        EndPoint endPoint = getEndpoint(lowerCountry);
        String url = getRecordUrl(endPoint.mainUrl, lowerCountry, recordKey);
        httpAgent.request(url, null, endPoint.audience, endPoint.region, RETRY_CNT, new RequestParameters(URI_DELETE, ApiResponseCodes.DELETE));
    }

    @Override
    public TransferFindResult find(String country, TransferFilterContainer filter, int limit, int offset) throws StorageClientException, StorageServerException {
//        String lowerCountry = country.toLowerCase();
        EndPoint endpoint = getEndpoint(country);
        String url = getRecordActionUrl(endpoint.mainUrl, country, URI_FIND);
        String content = getGson4Records().toJson(filter);

//        ApiResponse response = httpAgent.request(url, builder.toString(), endpoint.audience, endpoint.region, RETRY_CNT, new RequestParameters(URI_POST, ApiResponseCodes.FIND));
        ApiResponse response = httpAgent.request(url, content, endpoint.audience, endpoint.region, RETRY_CNT, new RequestParameters(URI_POST, ApiResponseCodes.FIND));
        if (response.getContent() == null) {
            new TransferFindResult(new ArrayList<>(), null);
        }

        return getGson4Records().fromJson(response.getContent(), TransferFindResult.class);
    }

    @Override
    public AttachmentMeta addAttachment(String country, String recordKey, InputStream inputStream, String fileName, boolean upsert, String mimeType) throws StorageClientException, StorageServerException {
        String lowerCountry = country.toLowerCase();
        EndPoint endPoint = getEndpoint(lowerCountry);
        String url = getAttachmentUrl(endPoint.mainUrl, STORAGE_URL, lowerCountry, recordKey, URI_ATTACHMENTS);
        String method = upsert ? URI_PUT : URI_POST;
        String body;
        try {
            body = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
        } catch (IOException ex) {
            LOG.error(MSG_ERR_USER_INPUT_STREAM);
            throw new StorageClientException(MSG_ERR_USER_INPUT_STREAM, ex);
        }
        ApiResponse response = httpAgent.request(url, body, endPoint.audience, endPoint.region, RETRY_CNT, new RequestParameters(method, ApiResponseCodes.ADD_ATTACHMENT, mimeType, true, fileName));
        // TODO: 01.02.2021 cut this stuff
        return new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create()
                .fromJson(response.getContent(), AttachmentMeta.class);
    }

    @Override
    public void deleteAttachment(String country, String recordKey, String fileId) throws StorageClientException, StorageServerException {
        String lowerCountry = country.toLowerCase();
        EndPoint endPoint = getEndpoint(lowerCountry);
        String url = getAttachmentUrl(endPoint.mainUrl, STORAGE_URL, lowerCountry, recordKey, URI_ATTACHMENTS, fileId);
        httpAgent.request(url, null, endPoint.audience, endPoint.region, RETRY_CNT, new RequestParameters(URI_DELETE, ApiResponseCodes.DELETE_ATTACHMENT));
    }

    @Override
    public AttachedFile getAttachmentFile(String country, String recordKey, String fileId) throws StorageClientException, StorageServerException {
        String lowerCountry = country.toLowerCase();
        EndPoint endPoint = getEndpoint(lowerCountry);
        String url = getAttachmentUrl(endPoint.mainUrl, STORAGE_URL, lowerCountry, recordKey, URI_ATTACHMENTS, fileId);
        ApiResponse response = httpAgent.request(url, null, endPoint.audience, endPoint.region, RETRY_CNT, new RequestParameters(URI_GET, ApiResponseCodes.GET_ATTACHMENT_FILE));
        InputStream content = response.getContent() == null ? null : new ByteArrayInputStream(response.getContent().getBytes(StandardCharsets.UTF_8));
        String fileName = null;
        if (response.getMetaInfo() != null) {
            fileName = response.getMetaInfo().get(MetaInfoTypes.NAME);
        }
        return new AttachedFile(content, fileName);
    }

    @Override
    public AttachmentMeta updateAttachmentMeta(String country, String recordKey, String fileId, String fileName, String mimeType) throws StorageClientException, StorageServerException {
        String lowerCountry = country.toLowerCase();
        EndPoint endPoint = getEndpoint(lowerCountry);
        String url = getAttachmentUrl(endPoint.mainUrl, STORAGE_URL, lowerCountry, recordKey, URI_ATTACHMENTS, fileId, URI_META);
        ApiResponse response = httpAgent.request(url, JsonUtils.createUpdatedMetaJson(fileName, mimeType), endPoint.audience, endPoint.region, RETRY_CNT, new RequestParameters(URI_PATCH, ApiResponseCodes.UPDATE_ATTACHMENT_META));
        // TODO: 01.02.2021 cut this stuff
        return new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create()
                .fromJson(response.getContent(), AttachmentMeta.class);
    }

    @Override
    public AttachmentMeta getAttachmentMeta(String country, String recordKey, String fileId) throws StorageClientException, StorageServerException {
        String lowerCountry = country.toLowerCase();
        EndPoint endPoint = getEndpoint(lowerCountry);
        String url = getAttachmentUrl(endPoint.mainUrl, STORAGE_URL, lowerCountry, recordKey, URI_ATTACHMENTS, fileId, URI_META);
        ApiResponse response = httpAgent.request(url, null, endPoint.audience, endPoint.region, RETRY_CNT, new RequestParameters(URI_GET, ApiResponseCodes.GET_ATTACHMENT_META));
        return new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create()
                .fromJson(response.getContent(), AttachmentMeta.class);
    }

    private String getAttachmentUrl(String... urlParts) {
        return String.join(URI_DELIMITER, urlParts);
    }

    private String getRecordUrl(String endPoint, String country, String keyHash) {
        return new StringBuilder(endPoint)
                .append(URI_DELIMITER)
                .append(STORAGE_URL)
                .append(URI_DELIMITER)
                .append(country)
                .append(URI_DELIMITER)
                .append(keyHash).toString();
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
