//todo
//package com.incountry.residence.sdk.search;
//
//import com.google.gson.Gson;
//import com.google.gson.JsonArray;
//import com.incountry.residence.sdk.dto.search.internal.StringFilter;
//import com.incountry.residence.sdk.tools.JsonUtils;
//import com.incountry.residence.sdk.tools.crypto.CryptoManager;
//import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
//import org.junit.jupiter.api.Test;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//class StringFilterTest {
//
//    @Test
//    void toJSONStringTestWithCrypto() throws StorageClientException {
//        String filterValue = "filterValue";
//        CryptoManager crypto = new CryptoManager(null, "envId", null, false, true);
//        StringFilter stringFilter = new StringFilter(new String[]{filterValue});
//        JsonArray jsonArray = JsonUtils.toJsonArray(stringFilter, "", crypto);
//        assertEquals(crypto.createKeyHash(filterValue), jsonArray.get(0).getAsString());
//    }
//
//    @Test
//    void toJSONIntTest() throws StorageClientException {
//        int filterValue = 1;
//        StringFilter stringFilter = new StringFilter(new String[]{Integer.toString(filterValue)});
//        JsonArray jsonArray = JsonUtils.toJsonInt(stringFilter);
//        assertEquals(filterValue, jsonArray.get(0).getAsInt());
//    }
//
//    @Test
//    void fromJsonEmptyFieldsStringTest() {
//        String stringFilterJson = "{}";
//        StringFilter stringParam = new Gson().fromJson(stringFilterJson, StringFilter.class);
//        List<String> values = stringParam.getValues();
//        assertTrue(values.isEmpty());
//    }
//}
