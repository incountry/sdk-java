package com.incountry.residence.sdk.search;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.incountry.residence.sdk.dto.search.SortOrder;
import com.incountry.residence.sdk.dto.search.fields.NumberField;
import com.incountry.residence.sdk.dto.search.fields.SortField;
import com.incountry.residence.sdk.dto.search.fields.StringField;
import com.incountry.residence.sdk.dto.search.filters.FindFilter;
import com.incountry.residence.sdk.dto.search.filters.NumberFilter;
import com.incountry.residence.sdk.dto.search.filters.StringFilter;
import com.incountry.residence.sdk.tools.crypto.CryptoProvider;
import com.incountry.residence.sdk.tools.crypto.HashUtils;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.transfer.DtoTransformer;
import com.incountry.residence.sdk.tools.transfer.TransferFilterContainer;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("java:S5786")
public class FindFilterTest {

    private static final String JSON_FILTER_REGULAR =
            "{\"filter\":{\"record_key\":[\"28bcf5fdec84347637d05a34239faea8fe28d3cf412e78fa24ad59843f00ed45\"]," +
                    "\"key1\":[\"0d171da9e4e93d5c3e75137dd81259fc5847c43e7e34dda41c9fdbc353363971\"]," +
                    "\"key2\":[\"6f3d2975a1e42bb2dfd4c60a9dcdb260b40bf70c06658502eb799ab01f66c642\"]," +
                    "\"key3\":[\"9638bf918ab771140c9be319fe295821502ad73f4fcbedf3563326e0cec605de\"]," +
                    "\"key4\":[\"41056868b44e3628587e90acf95162f02dd8fd890c49b91ee1a2a82bea3a3ff9\"]," +
                    "\"key5\":[\"89d1565e2aa4f680433bb2a96c102c22fc3f8275848ca4f6e068530077ee9ae1\"]," +
                    "\"key6\":[\"f7209842cff546ef871fe22ffd02ba8bf85f5b751799fabc3bea205dcad4ed58\"]," +
                    "\"key7\":[\"607f75660789ca7083d9c03446075606b7124f7cf4f3c547b10b9d0d3df670a7\"]," +
                    "\"key8\":[\"339493f151822b2409ec0aa8d4c211829116d5a451a3a5573f3dad3fa830f15b\"]," +
                    "\"key9\":[\"2e900eb1042301446010d9636c31a24ef7b57636448c630bc1dbc9740cc16668\"]," +
                    "\"key10\":[\"7a223f3fb53f860f70101ed330f5f1c6a3a75239b26f50c635918c5d3118d8a6\"]," +
                    "\"profile_key\":[\"ac94d0a56e81139510a0bcd77e5d7c9d9a7083bfa969b7da5b4bdc70f374cb0d\"]," +
                    "\"service_key1\":[\"4df8e03ea556562e21a486c4c3fc5256896cdec44691609f735614f6ee456f7b\"]," +
                    "\"service_key2\":[\"567176b1bbf5fd47fbf51a28312c63a208ce99975d24020668efc3df5e4816b1\"]," +
                    "\"range_key1\":[1],\"range_key2\":[2],\"range_key3\":[3],\"range_key4\":[4],\"range_key5\":[5]," +
                    "\"range_key6\":[6],\"range_key7\":[7],\"range_key8\":[8],\"range_key9\":[9],\"range_key10\":[10]," +
                    "\"version\":[123]}," +
                    "\"options\":{\"limit\":100,\"offset\":0}}";

    private static final String JSON_FILTER_NORMALIZE_KEYS =
            "{\"filter\":{\"record_key\":[\"308738c53b83aac87b1a833deb86a530565c02bccdb6665f8659c28b709f89b4\"]," +
                    "\"key1\":[\"8b03e45d4e198ab1e031db1b2e4d33987be55d1a518b394ee14b86149320869a\"]," +
                    "\"key2\":[\"c2d857f05ccb1b760dedaffabb06f42f0f205a56be952a8117330da55abfc8c7\"]," +
                    "\"key3\":[\"ec72da32b1bfcba24a7d0c1daf1ad0a63d3c7cf4048bbf5d7e7cb35141b58fb0\"]," +
                    "\"key4\":[\"a05f09cfb7c0a82d7830712a08ba6461a9ccb3af5baa085f71308ecfd999e2e8\"]," +
                    "\"key5\":[\"b7db7b89b89c0bf5400985a4ea9fd8de98e97b47f740c695152b2f7502373248\"]," +
                    "\"key6\":[\"1aa5d91aec81cbec7ada3b275eb08e35068cd4516ba9cafd94370db3ad35f716\"]," +
                    "\"key7\":[\"1517b7839b289836ecaa06f6068d1ae4058902894cffedf6c8b7b9e3589cc56f\"]," +
                    "\"key8\":[\"60ce17f7bc9f4155739b0a9eda0a9d398a477fb0e06356964371fd3f0fdbfdb4\"]," +
                    "\"key9\":[\"73e7e18443398f8db1c0aea1af1bd880a17bc44bc9812032bc3712f9de594a91\"]," +
                    "\"key10\":[\"8581fd41d1ff56fcb0e2a1574caceccce62b9a72fc22ed0cf5abe6fb082a77bb\"]," +
                    "\"profile_key\":[\"429ce8cac38098fc1aebe969ab6c596b78000823478c7ec3fd6f6c5e27ab1939\"]," +
                    "\"service_key1\":[\"fb6937942e47bf097f3d7f9cbbc3994c62a7aadc203bcf2740c9a7fa809ad272\"]," +
                    "\"service_key2\":[\"9bdf483dd5474e9009c98193d101edba6264f283802589e660ff684df6c140bc\"]," +
                    "\"range_key1\":[1],\"range_key2\":[2],\"range_key3\":[3],\"range_key4\":[4],\"range_key5\":[5]," +
                    "\"range_key6\":[6],\"range_key7\":[7],\"range_key8\":[8],\"range_key9\":[9],\"range_key10\":[10]," +
                    "\"version\":[123]}," +
                    "\"options\":{\"limit\":100,\"offset\":0}}";

    private static final String JSON_FILTER_NON_HASHING =
            "{\"filter\":{\"record_key\":[\"28bcf5fdec84347637d05a34239faea8fe28d3cf412e78fa24ad59843f00ed45\"]," +
                    "\"key1\":[\"_key1\"],\"key2\":[\"_key2\"],\"key3\":[\"_key3\"],\"key4\":[\"_key4\"],\"key5\":[\"_key5\"]," +
                    "\"key6\":[\"_key6\"],\"key7\":[\"_key7\"],\"key8\":[\"_key8\"],\"key9\":[\"_key9\"],\"key10\":[\"_key10\"]," +
                    "\"profile_key\":[\"ac94d0a56e81139510a0bcd77e5d7c9d9a7083bfa969b7da5b4bdc70f374cb0d\"]," +
                    "\"service_key1\":[\"4df8e03ea556562e21a486c4c3fc5256896cdec44691609f735614f6ee456f7b\"]," +
                    "\"service_key2\":[\"567176b1bbf5fd47fbf51a28312c63a208ce99975d24020668efc3df5e4816b1\"]," +
                    "\"range_key1\":[1],\"range_key2\":[2],\"range_key3\":[3],\"range_key4\":[4],\"range_key5\":[5]," +
                    "\"range_key6\":[6],\"range_key7\":[7],\"range_key8\":[8],\"range_key9\":[9],\"range_key10\":[10]," +
                    "\"version\":[123]}," +
                    "\"options\":{\"limit\":100,\"offset\":0}}";

    private static final String JSON_FILTER_SORTING_ASC =
            "{\"filter\":{\"range_key1\":[1]},\"options\":{\"limit\":100,\"offset\":0," +
                    "\"sort\":[{\"key1\":\"asc\"},{\"key2\":\"asc\"},{\"key3\":\"asc\"},{\"key4\":\"asc\"},{\"key5\":\"asc\"}," +
                    "{\"key6\":\"asc\"},{\"key7\":\"asc\"},{\"key8\":\"asc\"},{\"key9\":\"asc\"},{\"key10\":\"asc\"}," +
                    "{\"range_key1\":\"asc\"},{\"range_key2\":\"asc\"},{\"range_key3\":\"asc\"},{\"range_key4\":\"asc\"},{\"range_key5\":\"asc\"}," +
                    "{\"range_key6\":\"asc\"},{\"range_key7\":\"asc\"},{\"range_key8\":\"asc\"},{\"range_key9\":\"asc\"},{\"range_key10\":\"asc\"}," +
                    "{\"created_at\":\"asc\"},{\"updated_at\":\"asc\"}]}}";

    private static final String JSON_FILTER_SORTING_DESC =
            "{\"filter\":{\"range_key1\":[2]},\"options\":{\"limit\":100,\"offset\":0," +
                    "\"sort\":[{\"key1\":\"desc\"},{\"key2\":\"desc\"},{\"key3\":\"desc\"},{\"key4\":\"desc\"},{\"key5\":\"desc\"}," +
                    "{\"key6\":\"desc\"},{\"key7\":\"desc\"},{\"key8\":\"desc\"},{\"key9\":\"desc\"},{\"key10\":\"desc\"}," +
                    "{\"range_key1\":\"desc\"},{\"range_key2\":\"desc\"},{\"range_key3\":\"desc\"},{\"range_key4\":\"desc\"},{\"range_key5\":\"desc\"}," +
                    "{\"range_key6\":\"desc\"},{\"range_key7\":\"desc\"},{\"range_key8\":\"desc\"},{\"range_key9\":\"desc\"},{\"range_key10\":\"desc\"}," +
                    "{\"created_at\":\"desc\"},{\"updated_at\":\"desc\"}]}}";

    private static final String JSON_FILTER_NULL =
            "{\"filter\":{\"range_key1\":null,\"version\":null,\"range_key2\":null,\"range_key3\":null,\"range_key4\":null,\"range_key5\":null," +
                    "\"range_key6\":null,\"range_key7\":null,\"range_key8\":null,\"range_key9\":null,\"range_key10\":null," +
                    "\"record_key\":null," +
                    "\"key1\":null,\"key2\":null,\"key3\":null,\"key4\":null,\"key5\":null,\"key6\":null,\"key7\":null,\"key8\":null,\"key9\":null,\"key10\":null," +
                    "\"profile_key\":null,\"service_key1\":null,\"service_key2\":null},\"options\":{\"limit\":100,\"offset\":0,\"sort\":null}}";

    private static final String JSON_FILTER_NOT_NULL =
            "{\"filter\":{\"range_key1\":{\"$not\":null},\"version\":{\"$not\":null},\"range_key2\":{\"$not\":null}," +
                    "\"range_key3\":{\"$not\":null},\"range_key4\":{\"$not\":null},\"range_key5\":{\"$not\":null},\"range_key6\":{\"$not\":null}," +
                    "\"range_key7\":{\"$not\":null},\"range_key8\":{\"$not\":null},\"range_key9\":{\"$not\":null},\"range_key10\":{\"$not\":null}," +
                    "\"record_key\":{\"$not\":null},\"key1\":{\"$not\":null},\"key2\":{\"$not\":null}," +
                    "\"key3\":{\"$not\":null},\"key4\":{\"$not\":null},\"key5\":{\"$not\":null},\"key6\":{\"$not\":null},\"key7\":{\"$not\":null}," +
                    "\"key8\":{\"$not\":null},\"key9\":{\"$not\":null},\"key10\":{\"$not\":null},\"profile_key\":{\"$not\":null},\"service_key1\":{\"$not\":null}," +
                    "\"service_key2\":{\"$not\":null}},\"options\":{\"limit\":100,\"offset\":0,\"sort\":null}}";



    public static Gson getGson4Records() {
        return new GsonBuilder()
                .setFieldNamingStrategy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
    }

    public static boolean jsonEquals(String json1, String json2) {
        return getGson4Records().fromJson(json1, JsonObject.class)
                .equals(getGson4Records().fromJson(json2, JsonObject.class));
    }

    @Test
    void clearTest() throws StorageClientException {
        FindFilter filter = FindFilter.create()
                .keyEq(StringField.KEY1, "1st key")
                .keyEq(NumberField.VERSION, 3l)
                .limitAndOffset(50, 10);
        assertEquals(50, filter.getLimit());
        assertEquals(10, filter.getOffset());
        assertTrue(filter.getStringFilters().keySet().contains(StringField.KEY1));
        assertEquals("1st key", ((StringFilter) filter.getStringFilters().values().iterator().next()).getValues().get(0));
        assertTrue(filter.getNumberFilters().keySet().contains(NumberField.VERSION));
        assertEquals(3, ((NumberFilter) filter.getNumberFilters().values().iterator().next()).getValues().get(0));
        filter.clear();
        assertEquals(100, filter.getLimit());
        assertEquals(0, filter.getOffset());
        assertTrue(filter.getStringFilters().isEmpty());
        assertTrue(filter.getNumberFilters().isEmpty());
        assertEquals("{\"filter\":{},\"options\":{\"limit\":0,\"offset\":0}}",
                getGson4Records().toJson(new TransferFilterContainer(new HashMap<>(), 0, 0, null)));
    }

    @Test
    void keyBetweenTest() throws StorageClientException {
        CryptoProvider provider = new CryptoProvider();
        DtoTransformer transformer = new DtoTransformer(provider, new HashUtils("envId", false));
        FindFilter filter = FindFilter.create()
                .keyBetween(NumberField.RANGE_KEY1, 1L, 2l)
                .keyBetween(NumberField.RANGE_KEY2, 3L, 4l, true, false)
                .keyBetween(NumberField.RANGE_KEY3, 5L, 6l, false, false)
                .keyBetween(NumberField.RANGE_KEY4, 7L, 8l, false);
        assertEquals(
                "{\"filter\":{\"range_key4\":{\"$gt\":7,\"$lte\":8},\"range_key3\":{\"$gt\":5,\"$lt\":6}," +
                        "\"range_key2\":{\"$gte\":3,\"$lt\":4},\"range_key1\":{\"$gte\":1,\"$lte\":2}}," +
                        "\"options\":{\"limit\":100,\"offset\":0}}",
                getGson4Records().toJson(transformer.transformFilter(filter)));
    }

    @Test
    void stringKeyNotEqTest() throws StorageClientException {
        CryptoProvider provider = new CryptoProvider();
        DtoTransformer transformer = new DtoTransformer(provider, new HashUtils("envId", false));
        FindFilter filter = FindFilter.create()
                .keyNotEq(StringField.KEY1, "value1", "value2");

        assertEquals(
                "{\"filter\":{\"key1\":{\"$not\":[\"327c84457a2ff2c6da36314dc0ffb3216a283570a5c4654d5f51947e74742cf0\"," +
                        "\"04a29359b09bb94b4d6d923fc169c125b3b5253aac37c405dc5f5241535627b4\"]}}," +
                        "\"options\":{\"limit\":100,\"offset\":0}}",
                getGson4Records().toJson(transformer.transformFilter(filter)));
    }

    @Test
    void searchKeysTest() throws StorageClientException {
        CryptoProvider provider = new CryptoProvider();
        DtoTransformer transformer = new DtoTransformer(provider, new HashUtils("envId", false));
        FindFilter filter = FindFilter.create()
                .keyEq(StringField.RECORD_KEY, "some record key")
                .searchKeysLike("some regex value")
                .keyEq(NumberField.RANGE_KEY1, 1L)
                .keyNotEq(StringField.PROFILE_KEY, "some profile key");
        assertTrue(jsonEquals(
                "{\"filter\":{\"record_key\":[\"2ab632ee5ebf3af90be1ae6accea46d99843fdc4676bb29a376919fa9c530364\"]," +
                        "\"profile_key\":{\"$not\":[\"3075bffaf3d67f04c2ab25a53ae970d25350bf7abe63f977aeaa29366baf38fa\"]}," +
                        "\"search_keys\":\"some regex value\",\"range_key1\":[1]}," +
                        "\"options\":{\"limit\":100,\"offset\":0}}",
                getGson4Records().toJson(transformer.transformFilter(filter))));
    }

    @Test
    void searchKeysNegativeTest() throws StorageClientException {
        FindFilter filter = FindFilter.create()
                .keyEq(StringField.KEY1, "some record key");
        StorageClientException ex = assertThrows(StorageClientException.class, () -> filter
                .searchKeysLike("some regex value"));
        assertEquals("SEARCH_KEYS cannot be used in conjunction with regular KEY1...KEY10 lookup", ex.getMessage());

        FindFilter filter1 = FindFilter.create()
                .searchKeysLike("some regex value");

        ex = assertThrows(StorageClientException.class, () -> filter1
                .keyEq(StringField.KEY1, "some record key"));
        assertEquals("SEARCH_KEYS cannot be used in conjunction with regular KEY1...KEY10 lookup", ex.getMessage());

        ex = assertThrows(StorageClientException.class, () -> FindFilter.create()
                .searchKeysLike("12"));
        assertEquals("SEARCH_KEYS should contain at least 3 characters and be not longer than 200", ex.getMessage());

        ex = assertThrows(StorageClientException.class, () -> FindFilter.create()
                .searchKeysLike("78923641892340823482348239237482342384237849-37834-2748923-17348912-378934892347839190283019283" +
                        "78923641892340823482348239237482342384237849-37834-2748923-17348912-378934892347839190283019283" +
                        "78923641892340823482348239237482342384237849-37834-2748923-17348912-378934892347839190283019283"));
        assertEquals("SEARCH_KEYS should contain at least 3 characters and be not longer than 200", ex.getMessage());
    }

    @Test
    void allFieldsFilterTest() throws StorageClientException {
        FindFilter filter = FindFilter.create()
                .keyEq(StringField.RECORD_KEY, "rec_key")
                .keyEq(StringField.PROFILE_KEY, "par_key")
                .keyEq(StringField.PROFILE_KEY, "prof_key")
                .keyEq(StringField.SERVICE_KEY1, "serv1_key")
                .keyEq(StringField.SERVICE_KEY2, "serv2_key")
                .keyEq(StringField.KEY1, "_key1")
                .keyEq(StringField.KEY2, "_key2")
                .keyEq(StringField.KEY3, "_key3")
                .keyEq(StringField.KEY4, "_key4")
                .keyEq(StringField.KEY5, "_key5")
                .keyEq(StringField.KEY6, "_key6")
                .keyEq(StringField.KEY7, "_key7")
                .keyEq(StringField.KEY8, "_key8")
                .keyEq(StringField.KEY9, "_key9")
                .keyEq(StringField.KEY10, "_key10")
                .keyEq(NumberField.VERSION, 123L)
                .keyEq(NumberField.RANGE_KEY1, 1L)
                .keyEq(NumberField.RANGE_KEY2, 2L)
                .keyEq(NumberField.RANGE_KEY3, 3L)
                .keyEq(NumberField.RANGE_KEY4, 4L)
                .keyEq(NumberField.RANGE_KEY5, 5L)
                .keyEq(NumberField.RANGE_KEY6, 6L)
                .keyEq(NumberField.RANGE_KEY7, 7L)
                .keyEq(NumberField.RANGE_KEY8, 8L)
                .keyEq(NumberField.RANGE_KEY9, 9L)
                .keyEq(NumberField.RANGE_KEY10, 10L);

        CryptoProvider provider = new CryptoProvider();
        DtoTransformer transformer = new DtoTransformer(provider, new HashUtils("envId", false));
        assertTrue(jsonEquals(JSON_FILTER_REGULAR,
                getGson4Records().toJson(transformer.transformFilter(filter))));

        transformer = new DtoTransformer(provider, new HashUtils("envId", true));
        assertTrue(jsonEquals(JSON_FILTER_NORMALIZE_KEYS,
                getGson4Records().toJson(transformer.transformFilter(filter))));

        transformer = new DtoTransformer(provider, new HashUtils("envId", false), false);
        assertTrue(jsonEquals(JSON_FILTER_NON_HASHING,
                getGson4Records().toJson(transformer.transformFilter(filter))));
    }

    @Test
    void sortingTest() throws StorageClientException {
        FindFilter filter1 = FindFilter.create().keyEq(NumberField.RANGE_KEY1, 1l);
        FindFilter filter2 = FindFilter.create().keyEq(NumberField.RANGE_KEY1, 2l);

        for (SortField field : SortField.values()) {
            filter1.sortBy(field, SortOrder.ASC);
            filter2.sortBy(field, SortOrder.DESC);
        }

        CryptoProvider provider = new CryptoProvider();
        DtoTransformer transformer = new DtoTransformer(provider, new HashUtils("envId", false));
        assertTrue(jsonEquals(JSON_FILTER_SORTING_ASC,
                getGson4Records().toJson(transformer.transformFilter(filter1))));
        assertTrue(jsonEquals(JSON_FILTER_SORTING_DESC,
                getGson4Records().toJson(transformer.transformFilter(filter2))));
    }

    @Test
    void sortingNegativeTest() throws StorageClientException {
        FindFilter filter = FindFilter.create().keyEq(NumberField.RANGE_KEY1, 1l);
        filter.sortBy(SortField.KEY1, SortOrder.ASC);
        StorageClientException ex = assertThrows(StorageClientException.class, () -> filter.sortBy(SortField.KEY1, SortOrder.ASC));
        assertEquals("Field KEY1 is already in sorting list", ex.getMessage());
    }

    @Test
    void nullFilterTest() throws StorageClientException {
        FindFilter filter1 = FindFilter.create();
        FindFilter filter2 = FindFilter.create();
        for (StringField field: StringField.values()) {
            filter1.nullable(field);
            filter2.nullable(field, false);
        }
        for (NumberField field: NumberField.values()) {
            filter1.nullable(field);
            filter2.nullable(field, false);
        }

        CryptoProvider provider = new CryptoProvider();
        DtoTransformer transformer = new DtoTransformer(provider, new HashUtils("envId", false));

        Gson gson = new GsonBuilder()
                .serializeNulls()
                .setFieldNamingStrategy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
        assertEquals(gson.fromJson(JSON_FILTER_NULL, JsonObject.class), gson.fromJson(gson.toJson(transformer.transformFilter(filter1)), JsonObject.class));
        assertEquals(gson.fromJson(JSON_FILTER_NOT_NULL, JsonObject.class), gson.fromJson(gson.toJson(transformer.transformFilter(filter2)), JsonObject.class));
    }
}
