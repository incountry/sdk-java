package com.incountry.residence.sdk;

import com.google.gson.JsonArray;
import com.incountry.residence.sdk.dto.search.FilterStringParam;
import com.incountry.residence.sdk.tools.JsonUtils;
import com.incountry.residence.sdk.tools.crypto.Crypto;
import com.incountry.residence.sdk.tools.crypto.impl.CryptoImpl;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class FilterStringParamTest {


    @Test
    public void toJSONStringTestWithCrypto() {
        String filterValue = "filterValue";
        Crypto crypto = new CryptoImpl("envId");
        try {
            FilterStringParam filterStringParam = new FilterStringParam(filterValue);
            JsonArray jsonArray = JsonUtils.toJsonArray(filterStringParam, crypto);
            assertEquals(crypto.createKeyHash(filterValue), jsonArray.get(0).getAsString());
        } catch (StorageClientException e) {
            assertNull(e);
        }
    }

    @Test
    public void toJSONStringWithCryptoNullTest() {
        String filterValue = "filterValue";
        try {
            FilterStringParam filterStringParam = new FilterStringParam(filterValue);
            JsonArray jsonArray = JsonUtils.toJsonArray(filterStringParam, null);
            assertEquals(filterValue, jsonArray.get(0).getAsString());
        } catch (StorageClientException e) {
            assertNull(e);
        }
    }

    @Test
    public void toJSONIntTest() {
        int filterValue = 1;
        try {
            FilterStringParam filterStringParam = new FilterStringParam(Integer.toString(filterValue));
            JsonArray jsonArray = JsonUtils.toJsonInt(filterStringParam);
            assertEquals(filterValue, jsonArray.get(0).getAsInt());
        } catch (StorageClientException e) {
            assertNull(e);
        }
    }
}
