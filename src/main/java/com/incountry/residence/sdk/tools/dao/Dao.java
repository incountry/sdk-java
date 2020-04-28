package com.incountry.residence.sdk.tools.dao;

import com.incountry.residence.sdk.dto.BatchRecord;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.dto.search.FindFilterBuilder;
import com.incountry.residence.sdk.tools.crypto.impl.CryptoManager;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;

import java.util.List;

public interface Dao {
    void createRecord(String country, Record record, CryptoManager cryptoManager) throws StorageClientException, StorageServerException, StorageCryptoException;

    void createBatch(List<Record> records, String country, CryptoManager cryptoManager) throws StorageClientException, StorageServerException, StorageCryptoException;

    Record read(String country, String recordKey, CryptoManager cryptoManager) throws StorageClientException, StorageServerException, StorageCryptoException;

    void delete(String country, String recordKey, CryptoManager cryptoManager) throws StorageClientException, StorageServerException;

    BatchRecord find(String country, FindFilterBuilder builder, CryptoManager cryptoManager) throws StorageClientException, StorageServerException;
}
