package com.incountry.residence.sdk.tools.dao;

import com.incountry.residence.sdk.dto.AttachedFile;
import com.incountry.residence.sdk.dto.AttachmentMeta;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.transfer.TransferFilterContainer;
import com.incountry.residence.sdk.tools.transfer.TransferFindResult;
import com.incountry.residence.sdk.tools.transfer.TransferRecord;
import com.incountry.residence.sdk.tools.transfer.TransferRecordList;

import java.io.InputStream;
import java.util.List;

public interface Dao {
    TransferRecord createRecord(String country, TransferRecord transferRecord) throws StorageClientException, StorageServerException;

    TransferRecordList createBatch(String country, List<TransferRecord> records) throws StorageClientException, StorageServerException;

    TransferRecord read(String country, String recordKey) throws StorageClientException, StorageServerException;

    void delete(String country, String recordKey) throws StorageServerException, StorageClientException;

    void batchDelete(String country, TransferFilterContainer filters) throws StorageServerException, StorageClientException;

    TransferFindResult find(String country, TransferFilterContainer filters) throws StorageClientException, StorageServerException;

    AttachmentMeta addAttachment(String country, String recordKey, InputStream fileInputStream, String fileName, boolean upsert, String mimeType) throws StorageClientException, StorageServerException;

    void deleteAttachment(String country, String recordKey, String fileId) throws StorageClientException, StorageServerException;

    AttachedFile getAttachmentFile(String country, String recordKey, String fileId) throws StorageClientException, StorageServerException;

    AttachmentMeta updateAttachmentMeta(String country, String recordKey, String fileId, AttachmentMeta updatedMeta) throws StorageClientException, StorageServerException;

    AttachmentMeta getAttachmentMeta(String country, String recordKey, String fileId) throws StorageClientException, StorageServerException;

    boolean healthCheck(String country) throws StorageServerException, StorageClientException;
}
