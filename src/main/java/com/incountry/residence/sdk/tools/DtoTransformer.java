package com.incountry.residence.sdk.tools;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.incountry.residence.sdk.crypto.SecretsData;
import com.incountry.residence.sdk.dto.FindResult;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.dto.search.DateField;
import com.incountry.residence.sdk.dto.search.FindFilter;
import com.incountry.residence.sdk.dto.search.NumberField;
import com.incountry.residence.sdk.dto.search.StringField;
import com.incountry.residence.sdk.dto.search.internal.AbstractFilter;
import com.incountry.residence.sdk.dto.search.internal.SortingParam;
import com.incountry.residence.sdk.dto.search.internal.StringFilter;
import com.incountry.residence.sdk.tools.crypto.CryptoProvider;
import com.incountry.residence.sdk.tools.crypto.HashUtils;
import com.incountry.residence.sdk.tools.crypto.cipher.Ciphertext;
import com.incountry.residence.sdk.tools.exceptions.RecordException;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.crypto.SecretKeyAccessor;
import com.incountry.residence.sdk.tools.transfer.TransferFilterContainer;
import com.incountry.residence.sdk.tools.transfer.TransferFindResult;
import com.incountry.residence.sdk.tools.transfer.TransferRecord;
import com.incountry.residence.sdk.tools.transfer.TransferRecordList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.incountry.residence.sdk.tools.ValidationHelper.isNullOrEmpty;

public class DtoTransformer {
    private static final Logger LOG = LogManager.getLogger(DtoTransformer.class);
    private static final ValidationHelper HELPER = new ValidationHelper(LOG);

    private static final int MAX_STRING_KEY_LENGTH = 256;
    private static final int DEFAULT_RECORD_VERSION = 0;

    private static final String MSG_ERR_UNEXPECTED = "Unexpected error";
    private static final String MSG_ERR_NULL_SECRET = "Secret accessor returns null secret";
    private static final String MSG_ERR_INVALID_LENGTH = "key1-key20 length can't be more than " + MAX_STRING_KEY_LENGTH
            + " chars with option 'hashSearchKeys' = false";
    private static final String MSG_DEBUG_NULL_RECORD = "Received record is null";
    private static final String MSG_ERR_NULL_RECORD_KEY = "Record key can't be null";
    private static final String MSG_ERR_NULL_BODY = "Transfer record body can't be null";
    private static final String MSG_ERR_RECORD_PARSE = "Record parse exception";
    private static final String MSG_ERR_NULL_META = "Response error: Meta is null";
    private static final String MSG_ERR_NEGATIVE_META = "Response error: negative values in batch metadata";
    private static final String MSG_ERR_INCORRECT_COUNT = "Response error: count in batch metadata differs from data size";
    private static final String MSG_ERR_INCORRECT_TOTAL = "Response error: incorrect total in batch metadata, less then received";

    private static final String SEARCH_KEYS = "search_keys";

    private final CryptoProvider cryptoProvider;
    private final HashUtils hashUtils;
    private final boolean hashSearchKeys;
    private final SecretKeyAccessor keyAccessor;
    private final Gson gson;

    public DtoTransformer(CryptoProvider cryptoProvider, HashUtils hashUtils, boolean hashSearchKeys, SecretKeyAccessor keyAccessor) {
        this.cryptoProvider = cryptoProvider;
        this.hashUtils = hashUtils;
        this.hashSearchKeys = hashSearchKeys;
        this.keyAccessor = keyAccessor;
        gson = new GsonBuilder()
                .setFieldNamingStrategy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .disableHtmlEscaping()
                .create();
    }

    public SecretKeyAccessor getKeyAccessor() {
        return keyAccessor;
    }

    public TransferRecord getTransferRecord(Record originalRecord) throws StorageClientException, StorageCryptoException {
        TransferRecord meta = cloneRecordForMeta(originalRecord);
        ComplexBody newBodyObject = new ComplexBody(meta, originalRecord.getBody());
        String newBodyJson = gson.toJson(newBodyObject);
        return getEncryptedTransferRecord(newBodyJson, originalRecord);
    }

    private static TransferRecord cloneRecordForMeta(Record sourceRecord) {
        TransferRecord resRecord = new TransferRecord(sourceRecord.getRecordKey());
        resRecord.setProfileKey(sourceRecord.getProfileKey())
                .setKey1(sourceRecord.getKey1())
                .setKey2(sourceRecord.getKey2())
                .setKey3(sourceRecord.getKey3())
                .setKey4(sourceRecord.getKey4())
                .setKey5(sourceRecord.getKey5())
                .setKey6(sourceRecord.getKey6())
                .setKey7(sourceRecord.getKey7())
                .setKey8(sourceRecord.getKey8())
                .setKey9(sourceRecord.getKey9())
                .setKey10(sourceRecord.getKey10())
                .setKey11(sourceRecord.getKey11())
                .setKey12(sourceRecord.getKey12())
                .setKey13(sourceRecord.getKey13())
                .setKey14(sourceRecord.getKey14())
                .setKey15(sourceRecord.getKey15())
                .setKey16(sourceRecord.getKey16())
                .setKey17(sourceRecord.getKey17())
                .setKey18(sourceRecord.getKey18())
                .setKey19(sourceRecord.getKey19())
                .setKey20(sourceRecord.getKey20())
                .setParentKey(sourceRecord.getParentKey())
                .setServiceKey1(sourceRecord.getServiceKey1())
                .setServiceKey2(sourceRecord.getServiceKey2())
                .setServiceKey3(sourceRecord.getServiceKey3())
                .setServiceKey4(sourceRecord.getServiceKey4())
                .setServiceKey5(sourceRecord.getServiceKey5());
        return resRecord;
    }

    private TransferRecord getEncryptedTransferRecord(String complexBody, Record originalRecord) throws StorageClientException, StorageCryptoException {
        SecretsData secretsData = getSecretsData();
        Ciphertext ciphertext = cryptoProvider.encrypt(complexBody, secretsData);
        TransferRecord resultRecord = new TransferRecord(hashUtils.getSha256Hash(originalRecord.getRecordKey()));
        resultRecord.setProfileKey(hashUtils.getSha256Hash(originalRecord.getProfileKey()))
                .setServiceKey1(hashUtils.getSha256Hash(originalRecord.getServiceKey1()))
                .setServiceKey2(hashUtils.getSha256Hash(originalRecord.getServiceKey2()))
                .setServiceKey3(hashUtils.getSha256Hash(originalRecord.getServiceKey3()))
                .setServiceKey4(hashUtils.getSha256Hash(originalRecord.getServiceKey4()))
                .setServiceKey5(hashUtils.getSha256Hash(originalRecord.getServiceKey5()))
                .setParentKey(hashUtils.getSha256Hash(originalRecord.getParentKey()));
        resultRecord.setBody(ciphertext.getData());
        resultRecord.setPrecommitBody(originalRecord.getPrecommitBody() != null
                ? cryptoProvider.encrypt(originalRecord.getPrecommitBody(), secretsData).getData()
                : null);
        resultRecord.setEncrypted(keyAccessor != null)
                .setVersion(ciphertext.getKeyVersion());
        resultRecord.setRangeKey1(originalRecord.getRangeKey1())
                .setRangeKey2(originalRecord.getRangeKey2())
                .setRangeKey3(originalRecord.getRangeKey3())
                .setRangeKey4(originalRecord.getRangeKey4())
                .setRangeKey5(originalRecord.getRangeKey5())
                .setRangeKey6(originalRecord.getRangeKey6())
                .setRangeKey7(originalRecord.getRangeKey7())
                .setRangeKey8(originalRecord.getRangeKey8())
                .setRangeKey9(originalRecord.getRangeKey9())
                .setRangeKey10(originalRecord.getRangeKey10())
                .setExpiresAt(originalRecord.getExpiresAt());
        resultRecord.setKey1(transformSearchKey(originalRecord.getKey1(), hashSearchKeys, true))
                .setKey2(transformSearchKey(originalRecord.getKey2(), hashSearchKeys, true))
                .setKey3(transformSearchKey(originalRecord.getKey3(), hashSearchKeys, true))
                .setKey4(transformSearchKey(originalRecord.getKey4(), hashSearchKeys, true))
                .setKey5(transformSearchKey(originalRecord.getKey5(), hashSearchKeys, true))
                .setKey6(transformSearchKey(originalRecord.getKey6(), hashSearchKeys, true))
                .setKey7(transformSearchKey(originalRecord.getKey7(), hashSearchKeys, true))
                .setKey8(transformSearchKey(originalRecord.getKey8(), hashSearchKeys, true))
                .setKey9(transformSearchKey(originalRecord.getKey9(), hashSearchKeys, true))
                .setKey10(transformSearchKey(originalRecord.getKey10(), hashSearchKeys, true))
                .setKey11(transformSearchKey(originalRecord.getKey11(), hashSearchKeys, true))
                .setKey12(transformSearchKey(originalRecord.getKey12(), hashSearchKeys, true))
                .setKey13(transformSearchKey(originalRecord.getKey13(), hashSearchKeys, true))
                .setKey14(transformSearchKey(originalRecord.getKey14(), hashSearchKeys, true))
                .setKey15(transformSearchKey(originalRecord.getKey15(), hashSearchKeys, true))
                .setKey16(transformSearchKey(originalRecord.getKey16(), hashSearchKeys, true))
                .setKey17(transformSearchKey(originalRecord.getKey17(), hashSearchKeys, true))
                .setKey18(transformSearchKey(originalRecord.getKey18(), hashSearchKeys, true))
                .setKey19(transformSearchKey(originalRecord.getKey19(), hashSearchKeys, true))
                .setKey20(transformSearchKey(originalRecord.getKey20(), hashSearchKeys, true));
        resultRecord.setAttachments(null);
        return resultRecord;
    }

    private String transformSearchKey(String value, boolean hashSearchKeys, boolean validateLength) throws StorageClientException {
        if (hashSearchKeys) {
            return hashUtils.getSha256Hash(value);
        }
        boolean isInvalidLength = validateLength && value != null && value.length() > MAX_STRING_KEY_LENGTH;
        HELPER.check(StorageClientException.class, isInvalidLength, MSG_ERR_INVALID_LENGTH);
        return value;
    }

    private SecretsData getSecretsData() throws StorageClientException {
        SecretsData result = null;
        if (keyAccessor != null) {
            try {
                result = keyAccessor.getSecretsData();
            } catch (StorageClientException sce) {
                throw sce;
            } catch (Exception ex) {
                throw new StorageClientException(MSG_ERR_UNEXPECTED, ex);
            }
            if (result == null) {
                throw new StorageClientException(MSG_ERR_NULL_SECRET);
            }
        }
        return result;
    }

    public Record getRecord(TransferRecord transferRecord) throws StorageServerException, StorageClientException, StorageCryptoException {
        if (transferRecord == null) {
            LOG.debug(MSG_DEBUG_NULL_RECORD);
            return null;
        }
        validateTransferRecord(transferRecord);
        int recordVersion = transferRecord.getVersion() != null ? transferRecord.getVersion() : DEFAULT_RECORD_VERSION;
        SecretsData secretsData = getSecretsData();

        String complexBodyJson = cryptoProvider.decrypt(transferRecord.getBody(), secretsData, recordVersion);
        String precommitBody = null;
        if (!isNullOrEmpty(transferRecord.getPrecommitBody())) {
            precommitBody = cryptoProvider.decrypt(transferRecord.getPrecommitBody(), secretsData, recordVersion);
        }
        ComplexBody complexBody = gson.fromJson(complexBodyJson, ComplexBody.class);
        String recordKey = complexBody.meta.getRecordKey() != null ? complexBody.meta.getRecordKey() : complexBody.meta.getKey();
        transferRecord.setVersion(recordVersion);
        //readonly and number fields are copied
        return transferRecord.copy()
                .setRecordKey(recordKey)
                .setBody(complexBody.payload)
                .setPrecommitBody(precommitBody)
                .setProfileKey(complexBody.meta.getProfileKey())
                .setKey1(complexBody.meta.getKey1())
                .setKey2(complexBody.meta.getKey2())
                .setKey3(complexBody.meta.getKey3())
                .setKey4(complexBody.meta.getKey4())
                .setKey5(complexBody.meta.getKey5())
                .setKey6(complexBody.meta.getKey6())
                .setKey7(complexBody.meta.getKey7())
                .setKey8(complexBody.meta.getKey8())
                .setKey9(complexBody.meta.getKey9())
                .setKey10(complexBody.meta.getKey10())
                .setKey11(complexBody.meta.getKey11())
                .setKey12(complexBody.meta.getKey12())
                .setKey13(complexBody.meta.getKey13())
                .setKey14(complexBody.meta.getKey14())
                .setKey15(complexBody.meta.getKey15())
                .setKey16(complexBody.meta.getKey16())
                .setKey17(complexBody.meta.getKey17())
                .setKey18(complexBody.meta.getKey18())
                .setKey19(complexBody.meta.getKey19())
                .setKey20(complexBody.meta.getKey20())
                .setParentKey(complexBody.meta.getParentKey())
                .setServiceKey1(complexBody.meta.getServiceKey1())
                .setServiceKey2(complexBody.meta.getServiceKey2())
                .setServiceKey3(complexBody.meta.getServiceKey3())
                .setServiceKey4(complexBody.meta.getServiceKey4())
                .setServiceKey5(complexBody.meta.getServiceKey5());
    }

    private static void validateTransferRecord(TransferRecord transferRecord) throws StorageServerException {
        HELPER.check(StorageServerException.class, isNullOrEmpty(transferRecord.getRecordKey()), MSG_ERR_NULL_RECORD_KEY);
        HELPER.check(StorageServerException.class, isNullOrEmpty(transferRecord.getBody()), MSG_ERR_NULL_BODY);
    }

    public List<TransferRecord> getTransferRecordList(List<Record> recordList) throws StorageClientException, StorageCryptoException {
        List<TransferRecord> resultList = new ArrayList<>();
        for (Record currentRecord : recordList) {
            resultList.add(getTransferRecord(currentRecord));
        }
        return resultList;
    }

    public FindResult getFindResult(TransferFindResult findResult) throws StorageServerException {
        validateTransferFindResult(findResult);
        List<Record> records = new ArrayList<>();
        List<RecordException> recordExceptions = new ArrayList<>();
        if (findResult.getMeta().getCount() != 0) {
            for (TransferRecord transferRecord : findResult.getData()) {
                if (transferRecord.getVersion() == null) {
                    transferRecord.setVersion(DEFAULT_RECORD_VERSION);
                }
                try {
                    Record resultRecord = getRecord(transferRecord);
                    records.add(resultRecord);
                } catch (Exception ex) {
                    LOG.warn(MSG_ERR_RECORD_PARSE, ex);
                    recordExceptions.add(new RecordException(MSG_ERR_RECORD_PARSE, gson.toJson(transferRecord), ex));
                }
            }
        }
        return new FindResult(records, findResult.getMeta().getCount(), findResult.getMeta().getLimit(), findResult.getMeta().getOffset(),
                findResult.getMeta().getTotal(), recordExceptions);
    }

    private void validateTransferFindResult(TransferFindResult findResult) throws StorageServerException {
        TransferFindResult.FindMeta meta = findResult.getMeta();
        List<TransferRecord> data = findResult.getData();
        if (meta == null) {
            throw new StorageServerException(MSG_ERR_NULL_META);
        }
        boolean negativeNumber = findResult.getMeta().getCount() < 0 || meta.getLimit() < 0 || meta.getOffset() < 0 || meta.getTotal() < 0;
        boolean positiveMetaEmptyData = meta.getCount() > 0 && (data == null || data.isEmpty() || data.size() != meta.getCount());
        boolean zeroMetaNonEmptyData = meta.getCount() == 0 && data != null && !data.isEmpty();
        if (negativeNumber) {
            throw new StorageServerException(MSG_ERR_NEGATIVE_META);
        } else if (positiveMetaEmptyData || zeroMetaNonEmptyData) {
            throw new StorageServerException(MSG_ERR_INCORRECT_COUNT);
        } else if (meta.getCount() > meta.getTotal()) {
            throw new StorageServerException(MSG_ERR_INCORRECT_TOTAL);
        }
    }

    public TransferFilterContainer getTransferFilterContainer(FindFilter filter) throws StorageClientException {
        if (filter == null) {
            return new TransferFilterContainer(new HashMap<>(), FindFilter.MAX_LIMIT, FindFilter.DEFAULT_OFFSET, new ArrayList<>());
        }
        Map<String, Object> transformedFilters = new HashMap<>();
        for (Map.Entry<NumberField, AbstractFilter> entry : filter.getNumberFilters().entrySet()) {
            transformedFilters.put(entry.getKey().toString().toLowerCase(), entry.getValue().toTransferObject());
        }

        for (Map.Entry<StringField, AbstractFilter> entry : filter.getStringFilters().entrySet()) {
            AbstractFilter oneFilter = entry.getValue();
            if (oneFilter instanceof StringFilter) {
                StringFilter stringFilter = (StringFilter) oneFilter;
                boolean needHash = hashSearchKeys || !FindFilter.nonHashedKeysListContains(entry.getKey());
                List<String> transformedStrings = new ArrayList<>();
                for (String one : stringFilter.getValues()) {
                    transformedStrings.add(transformSearchKey(one, needHash, false));
                }
                StringFilter transformedFilter = new StringFilter(transformedStrings.toArray(new String[0]), stringFilter.isNotCondition());
                transformedFilters.put(entry.getKey().toString().toLowerCase(), transformedFilter.toTransferObject());
            } else {
                transformedFilters.put(entry.getKey().toString().toLowerCase(), entry.getValue().toTransferObject());
            }
        }
        for (Map.Entry<DateField, AbstractFilter> entry : filter.getDateFilters().entrySet()) {
            transformedFilters.put(entry.getKey().toString().toLowerCase(), entry.getValue().toTransferObject());
        }
        if (filter.getSearchKeys() != null) {
            transformedFilters.put(SEARCH_KEYS, filter.getSearchKeys());
        }
        List<Map<String, Object>> transferSortList = new ArrayList<>();
        List<SortingParam> sorting = filter.getSortingList();
        if (!sorting.isEmpty()) {
            for (SortingParam oneSortParam : sorting) {
                Map<String, Object> transferSortParam = new HashMap<>();
                transferSortParam.put(oneSortParam.getField().toString().toLowerCase(),
                        oneSortParam.getOrder().toString().toLowerCase());
                transferSortList.add(transferSortParam);
            }
        }
        return new TransferFilterContainer(transformedFilters, filter.getLimit(), filter.getOffset(), transferSortList);
    }

    public List<Record> getRecordList(TransferRecordList transferRecordList) throws StorageServerException, StorageClientException, StorageCryptoException {
        List<Record> resultRecordList = new ArrayList<>();
        if (transferRecordList != null && transferRecordList.getRecords() != null) {
            for (TransferRecord transferRecord : transferRecordList.getRecords()) {
                resultRecordList.add(getRecord(transferRecord));
            }
        }
        return resultRecordList;
    }

    static class ComplexBody {
        TransferRecord meta;
        String payload;

        ComplexBody(TransferRecord meta, String payload) {
            this.meta = meta;
            this.payload = payload;
        }
    }
}
