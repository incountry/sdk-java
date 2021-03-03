//todo
//package com.incountry.residence.sdk.search;
//
//import com.incountry.residence.sdk.dto.search.internal.StringFilter;
//import com.incountry.residence.sdk.dto.search.FindFilter;
//import com.incountry.residence.sdk.dto.search.StringField;
//import com.incountry.residence.sdk.tools.JsonUtils;
//import com.incountry.residence.sdk.tools.crypto.CryptoManager;
//import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
//import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;
//import com.incountry.residence.sdk.crypto.SecretsData;
//import org.apache.commons.codec.digest.DigestUtils;
//import org.junit.jupiter.api.Test;
//
//import java.nio.charset.StandardCharsets;
//import java.util.Collections;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//class FindFilterTest {
//
//    private String version = "0";
//    public String recordKey = "recordKey";
//    public String profileKey = "profileKey";
//
//    @Test
//    void testToJsonObject() throws StorageClientException {
//        StringFilter versionFilterParam = new StringFilter(new String[]{version}, true);
//        StringFilter recordKeyFilterParam = new StringFilter(new String[]{recordKey}, true);
//        StringFilter profileKeyFilterParam = new StringFilter(new String[]{profileKey}, true);
//
//        FindFilter findFilter = new FindFilter();
//        findFilter.setFilter(StringField.VERSION, versionFilterParam);
//        findFilter.setFilter(StringField.RECORD_KEY, recordKeyFilterParam);
//        findFilter.setFilter(StringField.PROFILE_KEY, profileKeyFilterParam);
//
//        String envId = null;
//        byte[] secret = "password".getBytes(StandardCharsets.UTF_8);
//        Integer keyVersion = 0;
//        SecretKey secretKey = new SecretKey(secret, keyVersion, false);
//        SecretsData secretsData = new SecretsData(Collections.singletonList(secretKey), keyVersion);
//        String jsonString = JsonUtils.toJsonString(findFilter,  new CryptoManager(() -> secretsData, envId, null, false, true));
//
//        assertTrue(jsonString.contains("\"record_key\":{\"$not\":[\"" + DigestUtils.sha256Hex((recordKey + ":" + envId).getBytes(StandardCharsets.UTF_8)) + "\"]}"));
//        assertTrue(jsonString.contains("\"profile_key\":{\"$not\":[\"" + DigestUtils.sha256Hex((profileKey + ":" + envId).getBytes(StandardCharsets.UTF_8)) + "\"]}"));
//        assertTrue(jsonString.contains("\"version\":{\"$not\":[" + version + "]}"));
//        assertTrue(jsonString.contains("\"options\":{\"limit\":100,\"offset\":0}"));
//    }
//
//
//    @Test
//    void testErrorArgs() {
//        FindFilter findFilter = new FindFilter();
//        StorageClientException ex1 = assertThrows(StorageClientException.class, () -> findFilter.setLimit(0));
//        assertEquals("Limit must be more than 1", ex1.getMessage());
//        StorageClientException ex2 = assertThrows(StorageClientException.class, () -> findFilter.setLimit(Integer.MAX_VALUE));
//        assertEquals("Max limit is 100. Use offset to populate more", ex2.getMessage());
//        StorageClientException ex3 = assertThrows(StorageClientException.class, () -> findFilter.setOffset(-1));
//        assertEquals("Offset must be more than 0", ex3.getMessage());
//    }
//}
