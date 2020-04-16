package com.incountry.residence.sdk;

import com.incountry.residence.sdk.dto.BatchRecord;
import com.incountry.residence.sdk.dto.MigrateResult;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.dto.search.FindFilterBuilder;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;

import java.util.List;

/**
 * Access point of SDK
 */
public interface Storage {

    /**
     * Write data to remote storage
     *
     * @param country country identifier
     * @param record  object which encapsulate data which must be written in storage
     * @return recorded record
     * @throws StorageServerException if server connection failed or server response error
     * @throws StorageCryptoException if encryption failed
     */
    Record write(String country, Record record) throws StorageServerException, StorageCryptoException;

    /**
     * Write multiple records at once in remote storage
     *
     * @param country country identifier
     * @param records record list
     * @return BatchRecord object which contains list of recorded records
     * @throws StorageServerException if server connection failed or server response error
     * @throws StorageCryptoException if record encryption failed
     */
    BatchRecord batchWrite(String country, List<Record> records) throws StorageServerException, StorageCryptoException;

    /**
     * Read data from remote storage
     *
     * @param country   country identifier
     * @param recordKey record unique identifier
     * @return Record object which contains required data
     * @throws StorageServerException if server connection failed or server response error
     * @throws StorageCryptoException if decryption failed
     */
    Record read(String country, String recordKey) throws StorageServerException, StorageCryptoException;

    /**
     * Delete record from remote storage
     *
     * @param country   country identifier
     * @param recordKey record unique identifier
     * @return TRUE when record was deleted
     * @throws StorageServerException if server connection failed
     */
    boolean delete(String country, String recordKey) throws StorageServerException;

    /**
     * Find records in remote storage according to filters
     *
     * @param country country identifier
     * @param builder object representing find filters
     * @return BatchRecord object which contains required records
     * @throws StorageServerException if server connection failed or server response error
     * @throws StorageCryptoException if decryption failed
     */
    BatchRecord find(String country, FindFilterBuilder builder) throws StorageServerException, StorageCryptoException;

    /**
     * Find only one first record in remote storage according to filters
     *
     * @param country country identifier
     * @param builder object representing find filters
     * @return founded record or null
     * @throws StorageServerException if server connection failed or server response error
     * @throws StorageCryptoException if decryption failed
     */
    Record findOne(String country, FindFilterBuilder builder) throws StorageServerException, StorageCryptoException;

    /**
     * Make batched key-rotation-migration of records
     *
     * @param country country identifier
     * @param limit   batch-limit parameter
     * @return MigrateResult object which contain total records left to migrate and total amount of migrated records
     * @throws StorageException if encryption is off/failed, if server connection failed or server response error
     */
    MigrateResult migrate(String country, int limit) throws StorageException;
}
