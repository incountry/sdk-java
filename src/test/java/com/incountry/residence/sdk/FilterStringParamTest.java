package com.incountry.residence.sdk;

import com.google.gson.JsonArray;
import com.incountry.residence.sdk.dto.search.FilterStringParam;
import com.incountry.residence.sdk.tools.JsonUtils;
import com.incountry.residence.sdk.tools.crypto.Crypto;
import com.incountry.residence.sdk.tools.crypto.impl.CryptoImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FilterStringParamTest {


    @Test
    public void toJSONStringTestWithCrypto() {
        String filterValue = "filterValue";
        Crypto crypto = new CryptoImpl("envId");
        FilterStringParam filterStringParam = new FilterStringParam(filterValue);
        JsonArray jsonArray = JsonUtils.toJsonString(filterStringParam, crypto);
        assertEquals(crypto.createKeyHash(filterValue), jsonArray.get(0).getAsString());
    }

    @Test
    public void toJSONStringWithCryptoNullTest() {
        String filterValue = "filterValue";
        FilterStringParam filterStringParam = new FilterStringParam(filterValue);
        JsonArray jsonArray = JsonUtils.toJsonString(filterStringParam, null);
        assertEquals(filterValue, jsonArray.get(0).getAsString());
    }

    @Test
    public void toJSONIntTest() {
        int filterValue = 1;
        FilterStringParam filterStringParam = new FilterStringParam(Integer.toString(filterValue));
        JsonArray jsonArray = JsonUtils.toJsonInt(filterStringParam);
        assertEquals(filterValue, jsonArray.get(0).getAsInt());
    }
}
