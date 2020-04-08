package com.incountry.residence.sdk.tools.dao;

import com.incountry.residence.sdk.dto.BatchRecord;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.dto.search.FindFilterBuilder;
import com.incountry.residence.sdk.tools.crypto.Crypto;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;

import java.util.List;
import java.util.Map;

public interface Dao {

    Map<String, POP> loadCounties() throws StorageServerException;

    void createRecord(String country, POP pop, Record record, Crypto crypto) throws StorageServerException, StorageCryptoException;

    void createBatch(List<Record> records, String country, POP pop, Crypto crypto) throws StorageServerException, StorageCryptoException;

    Record read(String country, POP pop, String recordKey, Crypto crypto) throws StorageServerException, StorageCryptoException;

    void delete(String country, POP pop, String recordKey, Crypto crypto) throws StorageServerException;

    BatchRecord find(String country, POP pop, FindFilterBuilder builder, Crypto crypto) throws StorageServerException;
}
