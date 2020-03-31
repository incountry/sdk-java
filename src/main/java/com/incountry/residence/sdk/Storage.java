package com.incountry.residence.sdk;

import com.incountry.residence.sdk.dto.BatchRecord;
import com.incountry.residence.sdk.dto.MigrateResult;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.dto.search.FindFilter;
import com.incountry.residence.sdk.dto.search.FindOptions;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.http.HttpAgent;

import java.util.List;

/**
 * Access point of SDK
 */
public interface Storage {

    Record create(Record record) throws StorageServerException, StorageCryptoException;

    BatchRecord createBatch(String country, List<Record> records) throws StorageServerException, StorageCryptoException;

    Record read(String country, String recordKey) throws StorageServerException, StorageCryptoException;

    Record update(String country, FindFilter filter, Record record) throws StorageServerException, StorageCryptoException;

    boolean delete(String country, String recordKey) throws StorageServerException;

    BatchRecord find(String country, FindFilter filter, FindOptions options) throws StorageServerException, StorageCryptoException;

    Record findOne(String country, FindFilter filter, FindOptions options) throws StorageServerException, StorageCryptoException;

    MigrateResult migrate(String country, int limit) throws StorageException;

    void setHttpAgent(HttpAgent agent);
}
