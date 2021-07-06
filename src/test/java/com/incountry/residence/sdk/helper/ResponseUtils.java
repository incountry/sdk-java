package com.incountry.residence.sdk.helper;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.tools.DtoTransformer;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageCryptoException;

public class ResponseUtils {
    private static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    public static String getRecordStubResponse(Record record, DtoTransformer transformer) throws StorageClientException, StorageCryptoException {
        return GSON.toJson(transformer.getTransferRecord(record));
    }
}
