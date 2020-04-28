package com.incountry.residence.sdk;

import com.google.gson.JsonArray;
import com.incountry.residence.sdk.dto.search.FilterStringParam;
import com.incountry.residence.sdk.tools.JsonUtils;
import com.incountry.residence.sdk.tools.crypto.CustomCrypto;
import com.incountry.residence.sdk.tools.crypto.impl.CryptoManager;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FilterStringParamTest {


    @Test
    public void toJSONStringTestWithCrypto() throws StorageClientException {
        String filterValue = "filterValue";
        CustomCrypto crypto = new CryptoManager("envId");
        FilterStringParam filterStringParam = new FilterStringParam(new String[]{filterValue});
        JsonArray jsonArray = JsonUtils.toJsonArray(filterStringParam, crypto);
        assertEquals(crypto.createKeyHash(filterValue), jsonArray.get(0).getAsString());
    }

    @Test
    public void toJSONStringWithCryptoNullTest() throws StorageClientException {
        String filterValue = "filterValue";
        FilterStringParam filterStringParam = new FilterStringParam(new String[]{filterValue});
        JsonArray jsonArray = JsonUtils.toJsonArray(filterStringParam, null);
        assertEquals(filterValue, jsonArray.get(0).getAsString());
    }

    @Test
    public void toJSONIntTest() throws StorageClientException {
        int filterValue = 1;
        FilterStringParam filterStringParam = new FilterStringParam(new String[]{Integer.toString(filterValue)});
        JsonArray jsonArray = JsonUtils.toJsonInt(filterStringParam);
        assertEquals(filterValue, jsonArray.get(0).getAsInt());
    }
}
