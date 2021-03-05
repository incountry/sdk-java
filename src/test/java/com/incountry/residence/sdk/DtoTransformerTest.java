package com.incountry.residence.sdk;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.incountry.residence.sdk.crypto.SecretKeyAccessor;
import com.incountry.residence.sdk.crypto.SecretsDataGenerator;
import com.incountry.residence.sdk.dto.search.FindFilter;
import com.incountry.residence.sdk.dto.search.SortField;
import com.incountry.residence.sdk.dto.search.SortOrder;
import com.incountry.residence.sdk.tools.DtoTransformer;
import com.incountry.residence.sdk.tools.crypto.CryptoProvider;
import com.incountry.residence.sdk.tools.crypto.HashUtils;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.transfer.TransferFindResult;
import com.incountry.residence.sdk.tools.transfer.TransferRecord;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DtoTransformerTest {
    @Test
    void getSecretsNegative() throws StorageClientException {
        SecretKeyAccessor accessor1 = () -> null;
        CryptoProvider provider = new CryptoProvider(null);
        HashUtils hashUtils = new HashUtils("salt", false);
        TransferRecord transferRecord = new TransferRecord("key");
        transferRecord.setBody("body");
        DtoTransformer transformer1 = new DtoTransformer(provider, hashUtils, true, accessor1);
        StorageClientException ex = assertThrows(StorageClientException.class, () -> transformer1.getRecord(transferRecord));
        assertEquals("Secret accessor returns null secret", ex.getMessage());

        String exceptionMessage = "message";
        SecretKeyAccessor accessor2 = () -> {
            throw new StorageClientException(exceptionMessage);
        };
        DtoTransformer transformer2 = new DtoTransformer(provider, hashUtils, true, accessor2);
        ex = assertThrows(StorageClientException.class, () -> transformer2.getRecord(transferRecord));
        assertEquals(exceptionMessage, ex.getMessage());

        SecretKeyAccessor accessor3 = () -> {
            throw new NullPointerException(exceptionMessage);
        };
        DtoTransformer transformer3 = new DtoTransformer(provider, hashUtils, true, accessor3);
        ex = assertThrows(StorageClientException.class, () -> transformer3.getRecord(transferRecord));
        assertEquals("Unexpected error", ex.getMessage());
        assertEquals(exceptionMessage, ex.getCause().getMessage());
    }

    @Test
    void validateTransferFindResultNegative() throws StorageClientException {
        SecretKeyAccessor accessor = () -> SecretsDataGenerator.fromPassword("password");
        CryptoProvider provider = new CryptoProvider(null);
        HashUtils hashUtils = new HashUtils("salt", false);
        DtoTransformer transformer = new DtoTransformer(provider, hashUtils, true, accessor);

        TransferFindResult result = new TransferFindResult();
        TransferFindResult.FindMeta meta = new TransferFindResult.FindMeta();
        meta.setCount(-1);
        result.setMeta(meta);
        StorageServerException ex = assertThrows(StorageServerException.class, () -> transformer.getFindResult(result));
        assertEquals("Response error: negative values in batch metadata", ex.getMessage());

        meta.setCount(0);
        meta.setLimit(-1);
        ex = assertThrows(StorageServerException.class, () -> transformer.getFindResult(result));
        assertEquals("Response error: negative values in batch metadata", ex.getMessage());

        meta.setLimit(0);
        meta.setOffset(-1);
        ex = assertThrows(StorageServerException.class, () -> transformer.getFindResult(result));
        assertEquals("Response error: negative values in batch metadata", ex.getMessage());

        meta.setOffset(0);
        meta.setTotal(-1);
        ex = assertThrows(StorageServerException.class, () -> transformer.getFindResult(result));
        assertEquals("Response error: negative values in batch metadata", ex.getMessage());

        meta.setTotal(0);
        meta.setCount(1);
        ex = assertThrows(StorageServerException.class, () -> transformer.getFindResult(result));
        assertEquals("Response error: count in batch metadata differs from data size", ex.getMessage());

        result.setData(new ArrayList<>());
        ex = assertThrows(StorageServerException.class, () -> transformer.getFindResult(result));
        assertEquals("Response error: count in batch metadata differs from data size", ex.getMessage());

        result.getData().add(new TransferRecord("1"));
        result.getData().add(new TransferRecord("2"));
        ex = assertThrows(StorageServerException.class, () -> transformer.getFindResult(result));
        assertEquals("Response error: count in batch metadata differs from data size", ex.getMessage());

        meta.setCount(0);
        ex = assertThrows(StorageServerException.class, () -> transformer.getFindResult(result));
        assertEquals("Response error: count in batch metadata differs from data size", ex.getMessage());

        meta.setCount(2);
        meta.setTotal(1);
        ex = assertThrows(StorageServerException.class, () -> transformer.getFindResult(result));
        assertEquals("Response error: incorrect total in batch metadata, less then received", ex.getMessage());
    }

    @Test
    void searchKeyAndSortingFindFilterPositive() throws StorageClientException {
        FindFilter filter = new FindFilter()
                .searchKeysLike("keyword")
                .sortBy(SortField.CREATED_AT, SortOrder.DESC);
        SecretKeyAccessor accessor = () -> SecretsDataGenerator.fromPassword("password");
        CryptoProvider provider = new CryptoProvider(null);
        HashUtils hashUtils = new HashUtils("salt", false);
        DtoTransformer transformer = new DtoTransformer(provider, hashUtils, true, accessor);
        Gson gson = new GsonBuilder().serializeNulls().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        String json = gson.toJson(transformer.getTransferFilterContainer(filter));
        assertEquals("{\"filter\":{\"search_keys\":\"keyword\"},\"options\":{\"offset\":0,\"limit\":100,\"sort\":[{\"created_at\":\"desc\"}]}}", json);
    }
}
