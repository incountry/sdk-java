package com.incountry.storage.sdk;

import com.incountry.storage.sdk.tools.JsonUtils;
import com.incountry.storage.sdk.tools.crypto.Crypto;
import com.incountry.storage.sdk.tools.crypto.impl.CryptoImpl;
import com.incountry.storage.sdk.dto.search.FilterStringParam;
import org.json.JSONArray;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class FilterStringParamTest {


    @Test
    public void toJSONStringTestWithCrypto() {
        String filterValue = "filterValue";
        Crypto crypto = new CryptoImpl("envId");
        FilterStringParam filterStringParam = new FilterStringParam(filterValue);
        JSONArray jsonArray = JsonUtils.toJsonString(filterStringParam, crypto);
        assertEquals(crypto.createKeyHash(filterValue), jsonArray.get(0));
    }

    @Test
    public void toJSONStringWithCryptoNullTest() {
        String filterValue = "filterValue";
        FilterStringParam filterStringParam = new FilterStringParam(filterValue);
        JSONArray jsonArray = JsonUtils.toJsonString(filterStringParam, null);
        assertEquals(filterValue, jsonArray.get(0));
    }

    @Test
    public void toJSONStringTestWithFilterStringParamValueNullTest() {
        Crypto crypto = new CryptoImpl("envId");
        List<String> filterValue = null;
        FilterStringParam filterStringParam = new FilterStringParam(filterValue);
        assertNull(JsonUtils.toJsonString(filterStringParam, crypto));
    }

    @Test
    public void toJSONIntTest() {
        int filterValue = 1;
        FilterStringParam filterStringParam = new FilterStringParam(Integer.toString(filterValue));
        JSONArray jsonArray = JsonUtils.toJsonInt(filterStringParam);;
        assertEquals(filterValue, jsonArray.get(0));
    }

    @Test
    public void toJSONIntTestWithFilterStringParamValueNull() {
        List<String> filterValue = null;
        FilterStringParam filterStringParam = new FilterStringParam(filterValue);
        assertNull(JsonUtils.toJsonInt(filterStringParam));
    }
}
