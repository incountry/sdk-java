package com.incountry.residence.sdk.search;

import com.incountry.residence.sdk.dto.search.FilterStringParam;
import com.incountry.residence.sdk.dto.search.FindFilter;
import com.incountry.residence.sdk.dto.search.StringField;
import com.incountry.residence.sdk.tools.JsonUtils;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FindFilterTest {

    private String version = "0";
    public String recordKey = "recordKey";
    public String profileKey = "profileKey";

    @Test
    void testToJsonObject() throws StorageClientException {
        FilterStringParam versionFilterParam = new FilterStringParam(new String[]{version}, true);
        FilterStringParam recordKeyFilterParam = new FilterStringParam(new String[]{recordKey}, true);
        FilterStringParam profileKeyFilterParam = new FilterStringParam(new String[]{profileKey}, true);

        FindFilter findFilter = new FindFilter();
        findFilter.setStringFilter(StringField.VERSION, versionFilterParam);
        findFilter.setStringFilter(StringField.RECORD_KEY, recordKeyFilterParam);
        findFilter.setStringFilter(StringField.PROFILE_KEY, profileKeyFilterParam);
        String jsonString = JsonUtils.toJsonString(findFilter, null);

        assertTrue(jsonString.contains("\"record_key\":{\"$not\":[\"" + recordKey + "\"]}"));
        assertTrue(jsonString.contains("\"profile_key\":{\"$not\":[\"" + profileKey + "\"]}"));
        assertTrue(jsonString.contains("\"version\":{\"$not\":[" + version + "]}"));
        assertTrue(jsonString.contains("\"options\":{\"limit\":100,\"offset\":0}"));
    }


    @Test
    void testErrorArgs() {
        FindFilter findFilter = new FindFilter();
        StorageClientException ex1 = assertThrows(StorageClientException.class, () -> findFilter.setLimit(0));
        assertEquals("Limit must be more than 1", ex1.getMessage());
        StorageClientException ex2 = assertThrows(StorageClientException.class, () -> findFilter.setLimit(Integer.MAX_VALUE));
        assertEquals("Max limit is 100. Use offset to populate more", ex2.getMessage());
        StorageClientException ex3 = assertThrows(StorageClientException.class, () -> findFilter.setOffset(-1));
        assertEquals("Offset must be more than 0", ex3.getMessage());
    }
}
