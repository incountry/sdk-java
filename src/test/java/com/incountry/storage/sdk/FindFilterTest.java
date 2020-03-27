package com.incountry.storage.sdk;

import com.incountry.storage.sdk.dto.FilterStringParam;
import com.incountry.storage.sdk.dto.FindFilter;
import com.incountry.storage.sdk.tools.JsonUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FindFilterTest {

    private int version = 0;
    public String key = "key1";
    public String profileKey = "profileKey";

    @Test
    public void testToJSONObject() {

        FilterStringParam versionFilterParam = new FilterStringParam(Integer.toString(version), true);
        FilterStringParam keyFilterParam = new FilterStringParam(key, true);
        FilterStringParam profileKeyFilterParam = new FilterStringParam(profileKey);

        FindFilter findFilter = new FindFilter();
        findFilter.setVersionParam(versionFilterParam);
        findFilter.setKeyParam(keyFilterParam);
        findFilter.setProfileKeyParam(profileKeyFilterParam);
        JSONObject jsonObject = JsonUtils.toJson(findFilter,null);

        assertEquals(String.format("{\"$not\":[%d]}", version), jsonObject.get("version").toString());
        assertEquals(String.format("{\"$not\":[\"%s\"]}", key), jsonObject.get("key").toString());
        assertEquals(String.format("[\"%s\"]", profileKey), jsonObject.get("profile_key").toString());

    }
}
