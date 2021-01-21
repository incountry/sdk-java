package com.incountry.residence.sdk.search;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.incountry.residence.sdk.dto.search.internal.FilterStringParam;
import com.incountry.residence.sdk.tools.JsonUtils;
import com.incountry.residence.sdk.tools.crypto.CryptoManager;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilterStringParamTest {

    @Test
    void toJSONStringTestWithCrypto() throws StorageClientException {
        String filterValue = "filterValue";
        CryptoManager crypto = new CryptoManager(null, "envId", null, false, true);
        FilterStringParam filterStringParam = new FilterStringParam(new String[]{filterValue});
        JsonArray jsonArray = JsonUtils.toJsonArray(filterStringParam, "", crypto);
        assertEquals(crypto.createKeyHash(filterValue), jsonArray.get(0).getAsString());
    }

    @Test
    void toJSONIntTest() throws StorageClientException {
        int filterValue = 1;
        FilterStringParam filterStringParam = new FilterStringParam(new String[]{Integer.toString(filterValue)});
        JsonArray jsonArray = JsonUtils.toJsonInt(filterStringParam);
        assertEquals(filterValue, jsonArray.get(0).getAsInt());
    }

    @Test
    void fromJsonEmptyFieldsStringTest() {
        String stringFilterJson = "{}";
        FilterStringParam stringParam = new Gson().fromJson(stringFilterJson, FilterStringParam.class);
        List<String> values = stringParam.getValues();
        assertTrue(values.isEmpty());
    }

    @Test
    void filterStringParamCopyTest() throws StorageClientException {
        String filterValue = "filterValue";
        FilterStringParam stringParam = new FilterStringParam(new String[]{filterValue}, true);
        FilterStringParam stringParamCopy = stringParam.copy();
        assertEquals(stringParam.getValues(), stringParamCopy.getValues());
        assertEquals(stringParam.isNotCondition(), stringParamCopy.isNotCondition());
    }
}
