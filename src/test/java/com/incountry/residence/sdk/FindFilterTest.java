package com.incountry.residence.sdk;

import com.google.gson.JsonObject;
import com.incountry.residence.sdk.dto.search.FilterStringParam;
import com.incountry.residence.sdk.dto.search.FindFilter;
import com.incountry.residence.sdk.tools.JsonUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FindFilterTest {

    private int version = 0;
    public String key = "key1";
    public String profileKey = "profileKey";

    @Test
    public void testToJsonObject() {

        FilterStringParam versionFilterParam = new FilterStringParam(Integer.toString(version), true);
        FilterStringParam keyFilterParam = new FilterStringParam(key, true);
        FilterStringParam profileKeyFilterParam = new FilterStringParam(profileKey);

        FindFilter findFilter = new FindFilter();
        findFilter.setVersionFilter(versionFilterParam);
        findFilter.setKeyFilter(keyFilterParam);
        findFilter.setProfileKeyFilter(profileKeyFilterParam);
        JsonObject jsonObject = JsonUtils.toJson(findFilter, null);

        assertEquals(String.format("{\"$not\":[%d]}", version), jsonObject.get("version").toString());
        assertEquals(String.format("{\"$not\":[\"%s\"]}", key), jsonObject.get("key").toString());
        assertEquals(String.format("[\"%s\"]", profileKey), jsonObject.get("profile_key").toString());
    }


    @Test
    public void testErrorArgs() {
        FindFilter findFilter = new FindFilter();
        assertThrows(IllegalArgumentException.class, () -> findFilter.setLimit(0));
        assertThrows(IllegalArgumentException.class, () -> findFilter.setLimit(Integer.MAX_VALUE));
        assertThrows(IllegalArgumentException.class, () -> findFilter.setOffset(-1));
    }
}
