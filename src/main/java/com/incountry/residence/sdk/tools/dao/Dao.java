package com.incountry.residence.sdk.tools.dao;

import com.incountry.residence.sdk.dto.BatchRecord;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.dto.search.FindFilterBuilder;
import com.incountry.residence.sdk.tools.crypto.Crypto;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;

import java.util.List;

public interface Dao {
    void createRecord(String country, Record record, Crypto crypto) throws StorageServerException, StorageCryptoException;

    void createBatch(List<Record> records, String country, Crypto crypto) throws StorageServerException, StorageCryptoException;

    Record read(String country, String recordKey, Crypto crypto) throws StorageServerException, StorageCryptoException;

    void delete(String country, String recordKey, Crypto crypto) throws StorageServerException;

    BatchRecord find(String country, FindFilterBuilder builder, Crypto crypto) throws StorageServerException;
}
