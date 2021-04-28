package com.incountry.residence.sdk.helper;

import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.tools.DtoTransformer;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;
import com.incountry.residence.sdk.tools.transfer.TransferRecord;

public class ResponseUtils {
    public static String getRecordStubResponse(Record record, DtoTransformer transformer) throws StorageClientException, StorageCryptoException {
        TransferRecord transferRecord = transformer.getTransferRecord(record);
        return "{'record_key':'1'," +
                "'body':'" + transferRecord.getBody() + "'," +
                "'version':" + transferRecord.getVersion() + "}";
    }
}
