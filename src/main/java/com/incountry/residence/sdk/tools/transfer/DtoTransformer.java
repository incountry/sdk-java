package com.incountry.residence.sdk.tools.transfer;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.dto.search.filters.Filter;
import com.incountry.residence.sdk.dto.search.filters.FindFilter;
import com.incountry.residence.sdk.dto.FindResult;
import com.incountry.residence.sdk.dto.search.fields.NumberField;
import com.incountry.residence.sdk.dto.search.fields.SortField;
import com.incountry.residence.sdk.dto.search.SortingParam;
import com.incountry.residence.sdk.dto.search.fields.StringField;
import com.incountry.residence.sdk.dto.search.filters.StringFilter;
import com.incountry.residence.sdk.tools.crypto.ciphers.CipherText;
import com.incountry.residence.sdk.tools.crypto.CryptoProvider;
import com.incountry.residence.sdk.tools.crypto.HashUtils;
import com.incountry.residence.sdk.tools.exceptions.RecordException;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.keyaccessor.SecretKeyAccessor;
import com.incountry.residence.sdk.tools.keyaccessor.key.SecretsData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DtoTransformer {

    private static final Logger LOG = LogManager.getLogger(DtoTransformer.class);

    private static final String MSG_ERR_NULL_RECORD = "Record is null";
    private static final String MSG_ERR_NULL_SECRET_ACCESSOR = "Secret accessor returns null secret";
    private static final String MSG_ERR_NULL_FILTERS = "Filters can't be null";
    private static final String MSG_ERR_NULL_FIND_RESPONSE = "Response error: Find response is null";
    private static final String MSG_ERR_NULL_METADATA = "Response error: Find result metadata is null";
    private static final String MSG_ERR_VALUES_IN_FIND_RESULT_METADATA = "Response error: Negative values in find result metadata";
    private static final String MSG_ERR_COUNT_DIFFERS_FROM_DATA_SIZE = "Response error: count in find results metadata differs from data size";
    private static final String MSG_ERR_INCORRECT_TOTAL = "Response error: incorrect total in find results metadata, less then received";
    private static final String MSG_RECORD_PARSE_EXCEPTION = "Record Parse Exception";
    private static final String MSG_ERR_KEY_LENGTH = "Key1-Key10 length can't be more than 256 chars with option 'hashSearchKeys' = false";
    private static final String MSG_ERR_NULL_RECORD_KEY = "Null required record fields: recordKey";
    private static final String MSG_ERR_NULL_BODY = "Null required record fields: body";

    private static final int MAX_STRING_KEY_LENGTH = 256;
    private static final String SEARCH_KEYS = "search_keys";

    private CryptoProvider cryptoProvider;
    private HashUtils hashUtils;
    private boolean hashSearchKeys;
    private SecretKeyAccessor secretKeyAccessor;

    @SuppressWarnings("java:S3740")
    private Map<Enum, String> enumMapping = new HashMap<>();

    public DtoTransformer(CryptoProvider cryptoProvider, HashUtils hashUtils) {
        this(cryptoProvider, hashUtils, true, null);
    }

    public DtoTransformer(CryptoProvider cryptoProvider, HashUtils hashUtils, boolean hashSearchKeys) {
        this(cryptoProvider, hashUtils, hashSearchKeys, null);
    }

    public DtoTransformer(CryptoProvider cryptoProvider, HashUtils hashUtils, boolean hashSearchKeys, SecretKeyAccessor secretKeyAccessor) {
        this.cryptoProvider = cryptoProvider;
        this.hashUtils = hashUtils;
        this.hashSearchKeys = hashSearchKeys;
        this.secretKeyAccessor = secretKeyAccessor;

        for (SortField field : SortField.values()) {
            enumMapping.put(field, getGson4Records().toJson(field).replace("\"", ""));
        }

        for (StringField field : StringField.values()) {
            enumMapping.put(field, getGson4Records().toJson(field).replace("\"", ""));
        }

        for (NumberField field : NumberField.values()) {
            enumMapping.put(field, getGson4Records().toJson(field).replace("\"", ""));
        }
    }

    private static Gson getGson4Records() {
        return new GsonBuilder()
                .setFieldNamingStrategy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
    }

    public TransferRecord getTransferRecord(Record record) throws StorageClientException, StorageCryptoException {
        TransferRecord recordMeta = cloneRecordForMeta(record);
        ComplexBody newBodyObject = new ComplexBody(recordMeta, record.getBody());
        String newBodyJson = getGson4Records().toJson(newBodyObject);
        return getEncryptedTransferRecord(newBodyJson, record);
    }

    private TransferRecord getEncryptedTransferRecord(String complexBody, Record originalRecord) throws StorageClientException, StorageCryptoException {
        SecretsData secretsData = getSecretsData(secretKeyAccessor != null);
        CipherText encryptedBodyPair = cryptoProvider.encrypt(complexBody, secretsData);
        TransferRecord resultRecord = new TransferRecord();
        resultRecord.setRecordKey(hashUtils.getSha256Hash(originalRecord.getRecordKey()));
        resultRecord.setBody(encryptedBodyPair.getData());
        resultRecord.setPrecommitBody(originalRecord.getPrecommitBody() != null
                ? cryptoProvider.encrypt(originalRecord.getPrecommitBody(), secretsData).getData()
                : null);
        resultRecord.setProfileKey(hashUtils.getSha256Hash(originalRecord.getProfileKey()));
        resultRecord.setKey1(transformSearchKey(originalRecord.getKey1(), hashSearchKeys, true));
        resultRecord.setKey2(transformSearchKey(originalRecord.getKey2(), hashSearchKeys, true));
        resultRecord.setKey3(transformSearchKey(originalRecord.getKey3(), hashSearchKeys, true));
        resultRecord.setKey4(transformSearchKey(originalRecord.getKey4(), hashSearchKeys, true));
        resultRecord.setKey5(transformSearchKey(originalRecord.getKey5(), hashSearchKeys, true));
        resultRecord.setKey6(transformSearchKey(originalRecord.getKey6(), hashSearchKeys, true));
        resultRecord.setKey7(transformSearchKey(originalRecord.getKey7(), hashSearchKeys, true));
        resultRecord.setKey8(transformSearchKey(originalRecord.getKey8(), hashSearchKeys, true));
        resultRecord.setKey9(transformSearchKey(originalRecord.getKey9(), hashSearchKeys, true));
        resultRecord.setKey10(transformSearchKey(originalRecord.getKey10(), hashSearchKeys, true));
        resultRecord.setServiceKey1(hashUtils.getSha256Hash(originalRecord.getServiceKey1()));
        resultRecord.setServiceKey2(hashUtils.getSha256Hash(originalRecord.getServiceKey2()));
        resultRecord.setRangeKey1(originalRecord.getRangeKey1());
        resultRecord.setRangeKey2(originalRecord.getRangeKey2());
        resultRecord.setRangeKey3(originalRecord.getRangeKey3());
        resultRecord.setRangeKey4(originalRecord.getRangeKey4());
        resultRecord.setRangeKey5(originalRecord.getRangeKey5());
        resultRecord.setRangeKey6(originalRecord.getRangeKey6());
        resultRecord.setRangeKey7(originalRecord.getRangeKey7());
        resultRecord.setRangeKey8(originalRecord.getRangeKey8());
        resultRecord.setRangeKey9(originalRecord.getRangeKey9());
        resultRecord.setRangeKey10(originalRecord.getRangeKey10());
        resultRecord.setVersion(encryptedBodyPair.getKeyVersion());
        resultRecord.setEncrypted(secretKeyAccessor != null);

        return resultRecord;
    }

    private String transformSearchKey(String value, boolean hashSearchKeys, boolean validateLength) throws StorageClientException {
        if (hashSearchKeys) {
            return hashUtils.getSha256Hash(value);
        }
        if (value != null && validateLength && value.length() > MAX_STRING_KEY_LENGTH) {
            LOG.error(MSG_ERR_KEY_LENGTH);
            throw new StorageClientException(MSG_ERR_KEY_LENGTH);
        }
        return value;
    }

    public Record getRecord(TransferRecord transferRecord) throws StorageClientException, StorageCryptoException {
        if (transferRecord == null) {
            return null;
        }
        validateTransferRecord(transferRecord);
        Integer recordVersion = transferRecord.getVersion() != null ? transferRecord.getVersion() : 0;
        SecretsData secretsData = getSecretsData(secretKeyAccessor != null);
        String complexBodyJson = cryptoProvider.decrypt(transferRecord.getBody(), secretsData, recordVersion);
        String precommitBody = null;
        if (transferRecord.getPrecommitBody() != null && !transferRecord.getPrecommitBody().isEmpty()) {
            precommitBody = cryptoProvider.decrypt(transferRecord.getPrecommitBody(), secretsData, recordVersion);
        }
        ComplexBody complexBody = getGson4Records().fromJson(complexBodyJson, ComplexBody.class);
        Record resultRecord = new Record();
        TransferRecord complexBodyMeta = complexBody.getMeta();
        resultRecord.setRecordKey(complexBodyMeta.getRecordKey() != null ? complexBodyMeta.getRecordKey() : complexBodyMeta.getKey());
        resultRecord.setBody(complexBody.getPayload());
        resultRecord.setPrecommitBody(precommitBody);
        resultRecord.setProfileKey(complexBodyMeta.getProfileKey());
        resultRecord.setKey1(complexBodyMeta.getKey1());
        resultRecord.setKey2(complexBodyMeta.getKey2());
        resultRecord.setKey3(complexBodyMeta.getKey3());
        resultRecord.setKey4(complexBodyMeta.getKey4());
        resultRecord.setKey5(complexBodyMeta.getKey5());
        resultRecord.setKey6(complexBodyMeta.getKey6());
        resultRecord.setKey7(complexBodyMeta.getKey7());
        resultRecord.setKey8(complexBodyMeta.getKey8());
        resultRecord.setKey9(complexBodyMeta.getKey9());
        resultRecord.setKey10(complexBodyMeta.getKey10());
        resultRecord.setServiceKey1(complexBodyMeta.getServiceKey1());
        resultRecord.setServiceKey2(complexBodyMeta.getServiceKey2());
        resultRecord.setRangeKey1(transferRecord.getRangeKey1());
        resultRecord.setRangeKey2(transferRecord.getRangeKey2());
        resultRecord.setRangeKey3(transferRecord.getRangeKey3());
        resultRecord.setRangeKey4(transferRecord.getRangeKey4());
        resultRecord.setRangeKey5(transferRecord.getRangeKey5());
        resultRecord.setRangeKey6(transferRecord.getRangeKey6());
        resultRecord.setRangeKey7(transferRecord.getRangeKey7());
        resultRecord.setRangeKey8(transferRecord.getRangeKey8());
        resultRecord.setRangeKey9(transferRecord.getRangeKey9());
        resultRecord.setRangeKey10(transferRecord.getRangeKey10());
        resultRecord.setCreatedAt(transferRecord.getCreatedAt());
        resultRecord.setUpdatedAt(transferRecord.getUpdatedAt());
        resultRecord.setVersion(recordVersion);
        resultRecord.setAttachments(transferRecord.getAttachments() == null || transferRecord.getAttachments().isEmpty()
                ? null : transferRecord.getAttachments());
        return resultRecord;
    }

    public List<TransferRecord> getTransferRecordList(List<Record> recordList) throws StorageClientException, StorageCryptoException {
        List<TransferRecord> transferRecordList = new ArrayList<>();
        for (Record record : recordList) {
            transferRecordList.add(getTransferRecord(record));
        }
        return transferRecordList;
    }

    private static TransferRecord cloneRecordForMeta(Record sourceRecord) throws StorageClientException {
        if (sourceRecord == null) {
            LOG.error(MSG_ERR_NULL_RECORD);
            throw new StorageClientException(MSG_ERR_NULL_RECORD);
        }
        TransferRecord outputRecord = new TransferRecord();
        outputRecord.setRecordKey(sourceRecord.getRecordKey());
        outputRecord.setProfileKey(sourceRecord.getProfileKey());
        outputRecord.setKey1(sourceRecord.getKey1());
        outputRecord.setKey2(sourceRecord.getKey2());
        outputRecord.setKey3(sourceRecord.getKey3());
        outputRecord.setKey4(sourceRecord.getKey4());
        outputRecord.setKey5(sourceRecord.getKey5());
        outputRecord.setKey6(sourceRecord.getKey6());
        outputRecord.setKey7(sourceRecord.getKey7());
        outputRecord.setKey8(sourceRecord.getKey8());
        outputRecord.setKey9(sourceRecord.getKey9());
        outputRecord.setKey10(sourceRecord.getKey10());
        outputRecord.setServiceKey1(sourceRecord.getServiceKey1());
        outputRecord.setServiceKey2(sourceRecord.getServiceKey2());

        return outputRecord;
    }

    public static void validateTransferRecord(TransferRecord record) throws StorageClientException {
        if (record.getRecordKey() == null || record.getRecordKey().isEmpty()) {
            LOG.error(MSG_ERR_NULL_RECORD_KEY);
            throw new StorageClientException(MSG_ERR_NULL_RECORD_KEY);
        }
        if (record.getBody() == null || record.getBody().isEmpty()) {
            LOG.error(MSG_ERR_NULL_BODY);
            throw new StorageClientException(MSG_ERR_NULL_BODY);
        }
    }

    private class ComplexBody {

        private TransferRecord meta;
        private String payload;

        ComplexBody(TransferRecord meta, String payload) {
            this.meta = meta;
            this.payload = payload;
        }

        public TransferRecord getMeta() {
            return meta;
        }

        public String getPayload() {
            return payload;
        }
    }

    public TransferFilterContainer transformFilter(FindFilter filter) throws StorageClientException {
        Map<String, Object> transformedFilters = new HashMap<>();

        for (Map.Entry<NumberField, Filter> entry :  filter.getNumberFilters().entrySet()) {
            transformedFilters.put(enumMapping.get(entry.getKey()).toLowerCase(), entry.getValue().toTransferObject());
        }

        for (Map.Entry<StringField, Filter> entry : filter.getStringFilters().entrySet()) {
            Filter oneFilter = entry.getValue();
            if (oneFilter instanceof StringFilter) {
                StringFilter stringFilter = (StringFilter) oneFilter;
                boolean needHash = hashSearchKeys || !FindFilter.getNonHashedKeyList().contains(entry.getKey());
                List<String> hashedValues = new ArrayList<>();
                for (String stringFilterValue : stringFilter.getValues()) {
                    hashedValues.add(transformSearchKey(stringFilterValue, needHash, false));
                }
                transformedFilters.put(enumMapping.get(entry.getKey()).toLowerCase(),
                        new StringFilter(hashedValues, stringFilter.isNotCondition()).toTransferObject());
            } else {
                transformedFilters.put(enumMapping.get(entry.getKey()).toLowerCase(), oneFilter.toTransferObject());
            }
        }

        if (filter.getSearchKeys() != null) {
            transformedFilters.put(SEARCH_KEYS, filter.getSearchKeys());
        }

        if (transformedFilters.size() == 0) {
            LOG.error(MSG_ERR_NULL_FILTERS);
            throw new StorageClientException(MSG_ERR_NULL_FILTERS);
        }
        List<SortingParam> sorting = filter.getSortingList();
        List<Map<String, String>> transferSorting = sorting.stream()
                .map(param -> {
                    Map<String, String> params = new HashMap<>();
                    params.put(enumMapping.get(param.getField()).toLowerCase(), param.getOrder().toString().toLowerCase());
                    return params;
                }).collect(Collectors.toList());

        return new TransferFilterContainer(transformedFilters, filter.getLimit(), filter.getOffset(), transferSorting);
    }

    public FindResult getFindResult(TransferFindResult findResult) throws StorageServerException {
        validateTransferFindResult(findResult);

        List<RecordException> recordExceptions = new ArrayList<>();
        List<Record> records = new ArrayList<>();

        if (findResult.getMeta().getCount() != 0) {
            for (TransferRecord transferRecord : findResult.getData()) {
                if (transferRecord.getVersion() == null) {
                    transferRecord.setVersion(0);
                }
                try {
                    Record record = getRecord(transferRecord);
                    records.add(record);
                } catch (Exception e) {
                    recordExceptions.add(new RecordException(MSG_RECORD_PARSE_EXCEPTION, getGson4Records().toJson(transferRecord), e));
                }
            }
        }
        return new FindResult(
                records,
                recordExceptions,
                findResult.getMeta().getLimit(),
                findResult.getMeta().getOffset(),
                findResult.getMeta().getTotal(),
                findResult.getMeta().getCount()
        );
    }

    private static void validateTransferFindResult(TransferFindResult findResult) throws StorageServerException {
        if (findResult == null) {
            LOG.error(MSG_ERR_NULL_FIND_RESPONSE);
            throw new StorageServerException(MSG_ERR_NULL_FIND_RESPONSE);
        }
        TransferFindResult.FindMeta meta = findResult.getMeta();
        List<TransferRecord> data = findResult.getData();
        if (meta == null) {
            LOG.error(MSG_ERR_NULL_METADATA);
            throw new StorageServerException(MSG_ERR_NULL_METADATA);
        }
        if (meta.getCount() < 0 || meta.getLimit() < 0 || meta.getOffset() < 0 || meta.getTotal() < 0) {
            LOG.error(MSG_ERR_VALUES_IN_FIND_RESULT_METADATA);
            throw new StorageServerException(MSG_ERR_VALUES_IN_FIND_RESULT_METADATA);
        }
        if ((meta.getCount() > 0 && (data == null || data.isEmpty() || data.size() != meta.getCount()))
                || (meta.getCount() == 0 && data != null && !data.isEmpty())) {
            LOG.error(MSG_ERR_COUNT_DIFFERS_FROM_DATA_SIZE);
            throw new StorageServerException(MSG_ERR_COUNT_DIFFERS_FROM_DATA_SIZE);
        }
        if (meta.getCount() > meta.getTotal()) {
            LOG.error(MSG_ERR_INCORRECT_TOTAL);
            throw new StorageServerException(MSG_ERR_INCORRECT_TOTAL);
        }
    }

    public SecretKeyAccessor getSecretKeyAccessor() {
        return secretKeyAccessor;
    }

    private SecretsData getSecretsData(Boolean exceptionIfNull) throws StorageClientException {
        SecretsData result = secretKeyAccessor != null ? secretKeyAccessor.getSecretsData() : null;
        if (result == null && exceptionIfNull) {
            throw new StorageClientException(MSG_ERR_NULL_SECRET_ACCESSOR);
        }
        return result;
    }

}
