package com.incountry.residence.sdk.search;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.incountry.residence.sdk.dto.search.DateField;
import com.incountry.residence.sdk.dto.search.FindFilter;
import com.incountry.residence.sdk.dto.search.NumberField;
import com.incountry.residence.sdk.dto.search.SortField;
import com.incountry.residence.sdk.dto.search.SortOrder;
import com.incountry.residence.sdk.dto.search.StringField;
import com.incountry.residence.sdk.tools.DtoTransformer;
import com.incountry.residence.sdk.tools.crypto.CryptoProvider;
import com.incountry.residence.sdk.tools.crypto.HashUtils;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FindFilterTest {

    private static final String ENV_ID = "envId";
    private static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .serializeNulls()
            .create();

    @Test
    void stringKeyEqPositive() throws StorageClientException {
        FindFilter filter = new FindFilter();
        for (StringField field : StringField.values()) {
            filter.keyEq(field, field.toString());
        }

        DtoTransformer defaultTransformer = new DtoTransformer(new CryptoProvider(null), new HashUtils(ENV_ID, false), true, null);
        String filterJson = GSON.toJson(defaultTransformer.getTransferFilterContainer(filter));
        assertEquals(GSON.fromJson(DEFAULT_STRING_FILTER, JsonObject.class),
                GSON.fromJson(filterJson, JsonObject.class));

        DtoTransformer transformerNonHashing = new DtoTransformer(new CryptoProvider(null), new HashUtils(ENV_ID, false), false, null);
        String filterJson2 = GSON.toJson(transformerNonHashing.getTransferFilterContainer(filter));
        assertEquals(GSON.fromJson(NON_HASHING_STRING_FILTER, JsonObject.class),
                GSON.fromJson(filterJson2, JsonObject.class));

        DtoTransformer transformerWithNormalizing = new DtoTransformer(new CryptoProvider(null), new HashUtils(ENV_ID, true), true, null);
        String filterJson3 = GSON.toJson(transformerWithNormalizing.getTransferFilterContainer(filter));

        assertEquals(GSON.fromJson(NORMALIZED_STRING_FILTER, JsonObject.class),
                GSON.fromJson(filterJson3, JsonObject.class));

        assertNotEquals(filterJson, filterJson2);
        assertNotEquals(filterJson, filterJson3);
        assertNotEquals(filterJson2, filterJson3);
    }

    @Test
    void numberKeyEqPositive() throws StorageClientException {
        FindFilter filter = new FindFilter();
        NumberField[] numberFields = NumberField.values();
        for (int i = 0; i < numberFields.length; i++) {
            filter.keyEq(numberFields[i], Long.valueOf(i));
        }
        DtoTransformer transformer = new DtoTransformer(new CryptoProvider(null), new HashUtils(ENV_ID, false), true, null);
        String filterJson = GSON.toJson(transformer.getTransferFilterContainer(filter));
        assertEquals("{\"filter\":{\"range_key8\":7,\"range_key7\":6,\"range_key6\":5,\"range_key5\":4," +
                "\"range_key9\":8,\"range_key4\":3,\"range_key3\":2,\"range_key10\":9,\"version\":10," +
                "\"range_key2\":1,\"range_key1\":0},\"options\":{\"offset\":0,\"limit\":100}}", filterJson);
    }

    @Test
    void dateFiltersPositive() throws StorageClientException {
        Calendar calendar = Calendar.getInstance();
        calendar.set(1970, Calendar.JANUARY, 1, 0, 0, 0);
        Date date = calendar.getTime();
        FindFilter filter = new FindFilter().keyEq(DateField.EXPIRES_AT, date);
        DtoTransformer transformer = new DtoTransformer(new CryptoProvider(null), new HashUtils(ENV_ID, false), true, null);
        String filterJson = GSON.toJson(transformer.getTransferFilterContainer(filter));
        assertEquals("{\"filter\":{\"expires_at\":\"1970-01-01T00:00:00\"},\"options\":{\"offset\":0,\"limit\":100}}",
                filterJson);
        filter.clear().keyIsNull(DateField.EXPIRES_AT);
        filterJson = GSON.toJson(transformer.getTransferFilterContainer(filter));
        assertEquals("{\"filter\":{\"expires_at\":null},\"options\":{\"offset\":0,\"limit\":100}}",
                filterJson);

        filter.clear().keyNotEq(DateField.EXPIRES_AT, date);
        filterJson = GSON.toJson(transformer.getTransferFilterContainer(filter));
        assertEquals("{\"filter\":{\"expires_at\":{\"$not\":\"1970-01-01T00:00:00\"}},\"options\":{\"offset\":0,\"limit\":100}}",
                filterJson);

        filter.clear().keyIsNotNull(DateField.EXPIRES_AT);
        filterJson = GSON.toJson(transformer.getTransferFilterContainer(filter));
        assertEquals("{\"filter\":{\"expires_at\":{\"$not\":null}},\"options\":{\"offset\":0,\"limit\":100}}",
                filterJson);

        filter = filter.clear().keyLess(DateField.CREATED_AT, date)
                .keyLess(DateField.UPDATED_AT, date, true);
        filterJson = GSON.toJson(transformer.getTransferFilterContainer(filter));
        assertEquals("{\"filter\":{\"updated_at\":{\"$lte\":\"1970-01-01T00:00:00\"}," +
                        "\"created_at\":{\"$lt\":\"1970-01-01T00:00:00\"}},\"options\":{\"offset\":0,\"limit\":100}}",
                filterJson);

        filter = filter.clear().keyGreater(DateField.CREATED_AT, date)
                .keyGreater(DateField.UPDATED_AT, date, true);
        filterJson = GSON.toJson(transformer.getTransferFilterContainer(filter));
        assertEquals("{\"filter\":{\"updated_at\":{\"$gte\":\"1970-01-01T00:00:00\"}," +
                        "\"created_at\":{\"$gt\":\"1970-01-01T00:00:00\"}},\"options\":{\"offset\":0,\"limit\":100}}",
                filterJson);
        calendar.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        Date date2 = calendar.getTime();
        filter = filter.clear().keyBetween(DateField.CREATED_AT, date, date2)
                .keyBetween(DateField.UPDATED_AT, date, false, date2, false);
        filterJson = GSON.toJson(transformer.getTransferFilterContainer(filter));
        assertEquals("{\"filter\":{\"updated_at\":{\"$gt\":\"1970-01-01T00:00:00\",\"$lt\":\"2020-01-01T00:00:00\"}," +
                        "\"created_at\":{\"$gte\":\"1970-01-01T00:00:00\",\"$lte\":\"2020-01-01T00:00:00\"}}," +
                        "\"options\":{\"offset\":0,\"limit\":100}}",
                filterJson);

        filter = filter.clear().keyBetween(DateField.CREATED_AT, date, true, date2, false)
                .keyBetween(DateField.UPDATED_AT, date, false, date2, true);
        filterJson = GSON.toJson(transformer.getTransferFilterContainer(filter));
        assertEquals("{\"filter\":{\"updated_at\":{\"$gt\":\"1970-01-01T00:00:00\",\"$lte\":\"2020-01-01T00:00:00\"}," +
                        "\"created_at\":{\"$gte\":\"1970-01-01T00:00:00\",\"$lt\":\"2020-01-01T00:00:00\"}}," +
                        "\"options\":{\"offset\":0,\"limit\":100}}",
                filterJson);
    }

    @Test
    void dateKeyEqNegative() {
        FindFilter filter = new FindFilter();
        StorageClientException ex = assertThrows(StorageClientException.class, () ->
                filter.keyEq(DateField.EXPIRES_AT, null));
        assertEquals("Date filter can't be null", ex.getMessage());
        ex = assertThrows(StorageClientException.class, () ->
                filter.keyIsNull(DateField.CREATED_AT));
        assertEquals("This operation is available only for field 'ExpiresAt'", ex.getMessage());
        ex = assertThrows(StorageClientException.class, () ->
                filter.keyIsNotNull(DateField.UPDATED_AT));
        assertEquals("This operation is available only for field 'ExpiresAt'", ex.getMessage());
    }

    @Test
    void advancedFilterPositive() throws StorageClientException {
        FindFilter filter = new FindFilter()
                .keyIsNotNull(StringField.KEY1)
                .keyIsNotNull(NumberField.RANGE_KEY1)
                .keyIsNull(StringField.KEY2)
                .keyIsNull(NumberField.RANGE_KEY2)
                .keyEq(StringField.KEY3, "one", "two")
                .keyEq(NumberField.RANGE_KEY3, 1L, 2L)
                .keyNotEq(StringField.KEY4, "three", "four")
                .keyNotEq(StringField.KEY5, "5ive")
                .keyNotEq(NumberField.RANGE_KEY5, 3L, 4L)
                .keyGreater(NumberField.RANGE_KEY6, 5L)
                .keyGreater(NumberField.RANGE_KEY7, 6L, true)
                .keyLess(NumberField.RANGE_KEY8, 7L)
                .keyLess(NumberField.RANGE_KEY9, 8L, true)
                .keyBetween(NumberField.RANGE_KEY10, 1, 2)
                .keyBetween(NumberField.VERSION, 3, false, 5, false);
        Calendar calendar = Calendar.getInstance();
        calendar.set(1970, Calendar.JANUARY, 1, 0, 0, 0);
        filter.keyEq(DateField.EXPIRES_AT, calendar.getTime());

        DtoTransformer transformer = new DtoTransformer(new CryptoProvider(null), new HashUtils(ENV_ID, false), true, null);
        String filterJson = GSON.toJson(transformer.getTransferFilterContainer(filter));
        assertEquals("{\"filter\":{\"key1\":{\"$not\":null},\"key2\":null,\"key5\":{\"$not\":\"f098f46da8052bfd032c4fe462503ca996cbeaa2ca6644b866166ab6c13fd73b\"}," +
                        "\"key3\":[\"2850267d7ddfe1cd8116d22607200eb386817af3fa2114acef1483376353ac17\",\"7f28b8ad3e4a2924f08b030d95e320653441b6e91425c678291ba87c9adfbbd7\"]," +
                        "\"key4\":{\"$not\":[\"95b47bb565136599af99e6510d2676d0d8d9f211818df1dd6944a232021eec30\",\"75904d1bc23a6caa24d6f4c2c092de2465a9cf6af95e06d230d59dd4bd1e846b\"]}," +
                        "\"range_key10\":{\"$gte\":1,\"$lte\":2},\"version\":{\"$gt\":3,\"$lt\":5},\"range_key8\":{\"$lt\":7}," +
                        "\"range_key7\":{\"$gte\":6},\"range_key6\":{\"$gt\":5},\"range_key5\":{\"$not\":[3,4]},\"expires_at\":\"1970-01-01T00:00:00\"," +
                        "\"range_key9\":{\"$lte\":8},\"range_key3\":[1,2],\"range_key2\":null,\"range_key1\":{\"$not\":null}}," +
                        "\"options\":{\"offset\":0,\"limit\":100}}",
                filterJson);
    }

    @Test
    void limitAndOffsetNegative() {
        FindFilter findFilter = new FindFilter();
        StorageClientException ex1 = assertThrows(StorageClientException.class, () -> findFilter.limitAndOffset(0, 0));
        assertEquals("Limit must be more than 1", ex1.getMessage());
        StorageClientException ex2 = assertThrows(StorageClientException.class, () -> findFilter.limitAndOffset(Integer.MAX_VALUE, 0));
        assertEquals("Max limit is 100. Use offset to populate more", ex2.getMessage());
        StorageClientException ex3 = assertThrows(StorageClientException.class, () -> findFilter.limitAndOffset(1, -1));
        assertEquals("Offset must be more than 0", ex3.getMessage());
    }

    @Test
    void sortNegative() throws StorageClientException {
        FindFilter findFilter = new FindFilter();
        StorageClientException ex = assertThrows(StorageClientException.class, () -> findFilter.sortBy(null, null));
        assertEquals("Sorting field is null", ex.getMessage());

        ex = assertThrows(StorageClientException.class, () -> findFilter.sortBy(SortField.KEY4, null));
        assertEquals("Sorting order is null", ex.getMessage());

        findFilter.sortBy(SortField.KEY1, SortOrder.ASC)
                .sortBy(SortField.KEY2, SortOrder.DESC);

        ex = assertThrows(StorageClientException.class, () -> findFilter.sortBy(SortField.KEY1, SortOrder.DESC));
        assertEquals("Field KEY1 is already in sorting list", ex.getMessage());
    }

    @Test
    void searchKeysLikeNegative() throws StorageClientException {
        FindFilter findFilter = new FindFilter()
                .keyEq(StringField.SERVICE_KEY1, "service")
                .searchKeysLike("value")
                .keyEq(StringField.PROFILE_KEY, "profile_key");
        StorageClientException ex = assertThrows(StorageClientException.class, () -> findFilter.keyEq(StringField.KEY1, "value1"));
        assertEquals("SEARCH_KEYS cannot be used in conjunction with regular KEY1...KEY20 lookup", ex.getMessage());

        findFilter.clear().keyEq(StringField.KEY1, "value1");
        ex = assertThrows(StorageClientException.class, () -> findFilter.searchKeysLike("value2"));
        assertEquals("SEARCH_KEYS cannot be used in conjunction with regular KEY1...KEY20 lookup", ex.getMessage());
    }

    private static final String NORMALIZED_STRING_FILTER = "{\"filter\":{" +
            "\"key1\":\"8f5f9f8326177381ab141af7572f148797c6aa6333d3251824e7b2869dafeb41\"," +
            "\"key2\":\"cb678f4e13a4dd15f91d04797e593c6bda5f2c73a202b6f93d7ed6ee825e582a\"," +
            "\"key5\":\"4ebd2369ed6232cf6c03ce105009ef47b51103b0e3064031e803da408c804296\"," +
            "\"key6\":\"a4a216186a8903c21457e80be921f75a719da9a92643c8a689d4310c9fea2428\"," +
            "\"key3\":\"6d97988ae0d2d982b925da0b8459084d6927b7e916d4c18b06b2a45b93605e14\"," +
            "\"key4\":\"54729dc1e4cb1c9f9e568b0001dd35eca9453a0c4629149064ceafc6ef106e0e\"," +
            "\"key9\":\"e4b47e4030d22c8ea28129fec33c160d36351df9ed15b1b67a23dd378b23fe6c\"," +
            "\"key7\":\"00a73ecc4712401fd511a525ca8f4f053ff1befa009da1742243d3de49681639\"," +
            "\"key8\":\"0972f5583162a8b614e2fa7c622493af27bcda066db88995ea99c80d73abfd3e\"," +
            "\"key20\":\"6ad8da091338df58dc3eeaafe79cbe0fb0d479241ad4a54249bb1c14e9cee3a8\"," +
            "\"service_key1\":\"cdc3ece33af693b31d2cbb3d686109b175464f843097a2f5f068c4acf1c0b0b4\"," +
            "\"service_key2\":\"7db1e25b4dfe835c1dc265c1cbeeb7dee738599addffbdf39f303f0e43e65d90\"," +
            "\"service_key3\":\"638f738f999ac9f918fe6d058cf4731d1db5fd3c4f9da150368be10d2b7fb72c\"," +
            "\"service_key4\":\"520a2e6a545721cc64af52a455b6517b46cd729c80a114d792ee297fb186ee8e\"," +
            "\"service_key5\":\"1dff5e61195c5383466066d90635fd3fb8067168e328cb88701f282dda94bd5e\"," +
            "\"key15\":\"411d087eb03729e55267afbcce385025df44f4c5ce0dfc4801437a5fa3573e52\"," +
            "\"key14\":\"9334955bd1f425b49e73d6102173d3d99647e966600f0c45e47c8bed12361904\"," +
            "\"key13\":\"2af29cc93072a020a4bae22b0f9932e3225d7271ce65fbb71fc84ab13e7372ac\"," +
            "\"key12\":\"5812afbcf703113845a641ea75a3224854d174a6245494d7f464e28a24649843\"," +
            "\"key19\":\"0e7507e9c7aea28677f5245e734bcf4059ba1881ef37dc66dcb69ae8323793d9\"," +
            "\"key18\":\"b720c0cdd610194235926765975a54cf468f4ebfccffe238545eca81eff223e2\"," +
            "\"profile_key\":\"fb47dd038482e4bc96836d96ee3da2c4f673bb559b25f6c6f674cabcd3ea4fb0\"," +
            "\"key17\":\"7643afe1c176a3342230e4bbfb538450b95da8beea6e982c96d0f9d5219c8e3a\"," +
            "\"key16\":\"46eebef9a0cb2387eeaba9058bf2df2a6681c7789bcb54232d6c382768d50517\"," +
            "\"record_key\":\"8b4da40f7797b2bfec916ae654bd00cc2e1c731fcae39cf46290fee1835bd3f7\"," +
            "\"parent_key\":\"4d0575681ce8cfaeb9d4cd3171294931f148772cfbe30c67477d378d8940690e\"," +
            "\"key11\":\"2679240475e8518e4242ea3c6da545fb4a1d6a4e1b35f81892ddf46c8ae53f57\"," +
            "\"key10\":\"736cb364a63ce81339fbe4c44ea6c6127f26bab1e6d8475ce19aad2a08a87076\"}," +
            "\"options\":{\"offset\":0,\"limit\":100}}";

    private static final String NON_HASHING_STRING_FILTER = "{\"filter\":{" +
            "\"key1\":\"KEY1\",\"key2\":\"KEY2\",\"key5\":\"KEY5\",\"key6\":\"KEY6\",\"key3\":\"KEY3\"," +
            "\"key4\":\"KEY4\",\"key9\":\"KEY9\",\"key7\":\"KEY7\",\"key8\":\"KEY8\",\"key20\":\"KEY20\"," +
            "\"service_key1\":\"5294a16f78f7d9edab7b2d3da23a551bc05d4a60e8ec5ca46784a21254e9b656\"," +
            "\"service_key2\":\"e164d7eac1d2d39f346580e298b16704fd48bef64bc2cf61536ad22d2e1d1f30\"," +
            "\"service_key3\":\"51d21f341ce5593d1df0c36c3799342be66495a745aeb188bc0cc57619edb3f9\"," +
            "\"service_key4\":\"d3bb2b75ca9a3faa2cdb73a1222ad5ded992e1a16b20e13023ef2177821b2c81\"," +
            "\"service_key5\":\"22eba5726e689c21b66b2bad5ee4eb64cd2f76ec8d99ce3a795741035caceb41\"," +
            "\"key15\":\"KEY15\",\"key14\":\"KEY14\",\"key13\":\"KEY13\",\"key12\":\"KEY12\"," +
            "\"key19\":\"KEY19\",\"key18\":\"KEY18\"," +
            "\"profile_key\":\"fa4364783409c6b2ac20f712418befa28b7875c917004f12d64fba203a1cafbb\"," +
            "\"key17\":\"KEY17\",\"key16\":\"KEY16\"," +
            "\"record_key\":\"1399855535af4cb650f5dbe3a19d270e203f93f60c2b53cd851e51c51d250e50\"," +
            "\"parent_key\":\"bddd6f669b1de4630836ebc397c60efd6b5cb4776812bd686e23870a4de14ca4\"," +
            "\"key11\":\"KEY11\",\"key10\":\"KEY10\"},\"options\":{\"offset\":0,\"limit\":100}}";

    private static final String DEFAULT_STRING_FILTER = "{\"filter\":{" +
            "\"key1\":\"aab4146f077e4631463e59af26ab1fa87aea715d95058510ef3954ed68b89e43\"," +
            "\"key2\":\"360c0662f3c29d9087e4fc517fb273d1b7b4088b1809bbee4590773153682b82\"," +
            "\"key5\":\"efa36f7ba5a186fd6e178fc56e02b72e22c9e2ce44a1da819cf852cae57e4cf0\"," +
            "\"key6\":\"beba48fa60189a260fb7398bae3268210bad2508ff4ff6b007457d6d1098a51f\"," +
            "\"key3\":\"5cfa31d84e653874b48d9e7de14e53a0bbb8c3c38ba781856a4995faf9dcce93\"," +
            "\"key4\":\"6bf173c45c145498dbe5381c8f29f836f905dd5d24b77a6249d1777c54c60974\"," +
            "\"key9\":\"af8a7f556b61d53caf37c20d08727b94a205d75eeceff79791c51ff94b027a06\"," +
            "\"key7\":\"0fafc89c02ed4aa8c56ee00ebed5a0b88cf91f2240d787fa76d1c84490bb2aea\"," +
            "\"key8\":\"26db4311a425adcf4d5c0b60c57c75b610d2dde2ada791f3af2ed7dca2c04cac\"," +
            "\"key20\":\"b920f493cca851e2fa931bfcc206cbdc2ed551c837814170ec91445341df2b1b\"," +
            "\"service_key1\":\"5294a16f78f7d9edab7b2d3da23a551bc05d4a60e8ec5ca46784a21254e9b656\"," +
            "\"service_key2\":\"e164d7eac1d2d39f346580e298b16704fd48bef64bc2cf61536ad22d2e1d1f30\"," +
            "\"service_key3\":\"51d21f341ce5593d1df0c36c3799342be66495a745aeb188bc0cc57619edb3f9\"," +
            "\"service_key4\":\"d3bb2b75ca9a3faa2cdb73a1222ad5ded992e1a16b20e13023ef2177821b2c81\"," +
            "\"service_key5\":\"22eba5726e689c21b66b2bad5ee4eb64cd2f76ec8d99ce3a795741035caceb41\"," +
            "\"key15\":\"27ba0ee9c4f1bc147f61aa765e41d2661995d9317d706b67cdd9bfea62d9a885\"," +
            "\"key14\":\"dedf8616cab4ba110a075f27dc54ca73aa498976654ee77b18bf4bbc6027d3f8\"," +
            "\"key13\":\"c9aa113cc2900f0b0a4252aee14e37d62c9e4593a9a950c420bc70b1a2e69fef\"," +
            "\"key12\":\"d24ac97d1b90c2c7fe442f5537bad776de3dfd3cda27f81672e072ef1bb05859\"," +
            "\"key19\":\"91e91002c02df761f729e0d9b5e607681c654cfa8539dd6ca5ccf77399df88a1\"," +
            "\"key18\":\"77abd80955c676e7f8102ed0d46d2fc08a56e1a148fafe186c1ef3c17e600204\"," +
            "\"profile_key\":\"fa4364783409c6b2ac20f712418befa28b7875c917004f12d64fba203a1cafbb\"," +
            "\"key17\":\"b5cf7a3d443e230e5008b4e9019c9eafc10e0e7a8ed446b4d363c5475f865120\"," +
            "\"key16\":\"486c9615485b87a1484897b694b2857856c7575f36d9cb418c0d004ffb400e41\"," +
            "\"record_key\":\"1399855535af4cb650f5dbe3a19d270e203f93f60c2b53cd851e51c51d250e50\"," +
            "\"parent_key\":\"bddd6f669b1de4630836ebc397c60efd6b5cb4776812bd686e23870a4de14ca4\"," +
            "\"key11\":\"408a99d113d021c02352533a32b268c13b1bb81fd8955a42b850587ee2c71d6a\"," +
            "\"key10\":\"f36d6ca201feb267ab0c2a527224c2672c5d9c3d61712de94f351f6a4a7a7c97\"}," +
            "\"options\":{\"offset\":0,\"limit\":100}}";
}
