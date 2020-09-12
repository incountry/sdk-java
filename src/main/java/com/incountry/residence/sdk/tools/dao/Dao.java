package com.incountry.residence.sdk.tools.dao;

import com.incountry.residence.sdk.dto.AttachedFile;
import com.incountry.residence.sdk.dto.AttachmentMeta;
import com.incountry.residence.sdk.dto.BatchRecord;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.dto.search.FindFilterBuilder;
import com.incountry.residence.sdk.tools.crypto.CryptoManager;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;

import java.io.InputStream;
import java.util.List;

public interface Dao {
    void createRecord(String country, Record record, CryptoManager cryptoManager) throws StorageClientException, StorageServerException, StorageCryptoException;

    void createBatch(List<Record> records, String country, CryptoManager cryptoManager) throws StorageClientException, StorageServerException, StorageCryptoException;

    Record read(String country, String key, CryptoManager cryptoManager) throws StorageClientException, StorageServerException, StorageCryptoException;

    void delete(String country, String key, CryptoManager cryptoManager) throws StorageServerException, StorageClientException;

    BatchRecord find(String country, FindFilterBuilder builder, CryptoManager cryptoManager) throws StorageClientException, StorageServerException;

    String addAttachment(String country, String recordKey, InputStream fileInputStream, boolean upsert) throws StorageClientException, StorageServerException;

    void deleteAttachment(String country, String recordKey, String fileId) throws StorageClientException, StorageServerException;

    AttachedFile getAttachmentFile(String country, String recordKey, String fileId) throws StorageClientException, StorageServerException;

    void updateAttachmentMeta(String country, String recordKey, String fileId, String fileName, String mimeType) throws StorageClientException, StorageServerException;

    AttachmentMeta getAttachmentMeta(String country, String recordKey, String fileId) throws StorageClientException, StorageServerException;
}
