package com.incountry.residence.sdk.search;

import com.incountry.residence.sdk.dto.search.FilterStringParam;
import com.incountry.residence.sdk.dto.search.FindFilter;
import com.incountry.residence.sdk.tools.JsonUtils;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FindFilterTest {

    private String version = "0";
    public String key = "key1";
    public String profileKey = "profileKey";

    @Test
    void testToJsonObject() throws StorageClientException {
        FilterStringParam versionFilterParam = new FilterStringParam(new String[]{version}, true);
        FilterStringParam keyFilterParam = new FilterStringParam(new String[]{key}, true);
        FilterStringParam profileKeyFilterParam = new FilterStringParam(new String[]{profileKey}, true);

        FindFilter findFilter = new FindFilter();
        findFilter.setVersionFilter(versionFilterParam);
        findFilter.setKeyFilter(keyFilterParam);
        findFilter.setProfileKeyFilter(profileKeyFilterParam);
        String jsonString = JsonUtils.toJsonString(findFilter, null);

        assertEquals("{\"filter\":" +
                        "{\"key\":{\"$not\":[\"" + key + "\"]}," +
                        "\"profile_key\":{\"$not\":[\"" + profileKey + "\"]}," +
                        "\"version\":{\"$not\":[" + version + "]}}," +
                        "\"options\":{\"limit\":100,\"offset\":0}}",
                jsonString);
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
