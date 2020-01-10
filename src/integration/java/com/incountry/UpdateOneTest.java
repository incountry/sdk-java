/*
package com.incountry;

import com.incountry.exceptions.StorageException;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.security.GeneralSecurityException;

@TestMethodOrder(MethodOrderer.Alphanumeric.class)
public class UpdateOneTest extends BaseTest {

    private String country = "US";

    @DisplayName("Update record by key")
    @ParameterizedTest(name = "Update record by key" + testNameEnc)
    @ValueSource(booleans = {false, true})
    public void updateRecordByKeyTest(boolean encryption)
            throws GeneralSecurityException, StorageException, IOException, FindOptions.FindOptionsException {

        Record newRecord = createFullRecord(country);
        writeRecord(encryption, newRecord);

        FindFilter filter = new FindFilter();
        filter.setKeyParam(new FilterStringParam(newRecord.key));

        Record record = new Record();
        record.setCountry(newRecord.getCountry());
        record.setKey("UpdatedKey_" + newRecord.getKey());
        record.setKey2("UpdatedKey2_" + newRecord.getKey2());
        record.setKey3("UpdatedKey3_" + newRecord.getKey3());
        record.setProfileKey("UpdatedProfKey_" + newRecord.getProfileKey());
        record.setRangeKey(RandomUtils.nextInt(1, 99));
        record.setBody("UpdatedBody_" + newRecord.getBody());

        Record updatedRecord = storage.updateOne(country, filter, record);
//        Assertions.assertAll(validateRecord(record, updatedRecord));

        Record actualRecord = storage.read(country, record.getKey());
//        Assertions.assertAll(validateRecord(record, actualRecord));
    }

    @DisplayName("Update record by profile key")
    @ParameterizedTest(name = "Update record by profile key" + testNameEnc)
    @ValueSource(booleans = {false, true})
    public void updateRecordByProfileKeyTest(boolean encryption, String country)
            throws GeneralSecurityException, StorageException, IOException, FindOptions.FindOptionsException {

        Record newRecord = createFullRecord(country);
        writeRecord(encryption, newRecord);

        FindFilter filter = new FindFilter();
        filter.setProfileKeyParam(new FilterStringParam(newRecord.profileKey));

        Record record = new Record();
        record.setCountry(newRecord.getCountry());
        record.setKey("UpdatedKey_" + newRecord.getKey());
        record.setKey2("UpdatedKey2_" + newRecord.getKey2());
        record.setKey3("UpdatedKey3_" + newRecord.getKey3());
        record.setProfileKey("UpdatedProfKey_" + newRecord.getProfileKey());
        record.setRangeKey(RandomUtils.nextInt(1, 99));
        record.setBody("UpdatedBody_" + newRecord.getBody());

        Record updatedRecord = storage.updateOne(country, filter, record);
//        Assertions.assertAll(validateRecord(record, updatedRecord));

        Record actualRecord = storage.read(country, record.getKey());
//        Assertions.assertAll(validateRecord(record, actualRecord));
    }

    @DisplayName("Update record without override")
    @ParameterizedTest(name = "Update record without override" + testNameEnc)
    @ValueSource(booleans = {false, true})
    public void updateRecordWithoutOverrideTest(boolean encryption)
            throws GeneralSecurityException, StorageException, IOException, FindOptions.FindOptionsException {

        Record newRecord = createSimpleRecord(country);
        writeRecord(encryption, newRecord);

        FindFilter filter = new FindFilter();
        filter.setProfileKeyParam(new FilterStringParam(newRecord.profileKey));

        Record record = new Record();
        record.setKey2("UpdatedKey2_" + newRecord.getKey2());
        record.setProfileKey("UpdatedProfKey_" + newRecord.getProfileKey());
        record.setRangeKey(RandomUtils.nextInt(1, 99));
        record.setBody("UpdatedBody_" + newRecord.getBody());

        Record updatedRecord = storage.updateOne(country, filter, record);
//        Assertions.assertAll(validateRecord(record, updatedRecord));

        Record actualRecord = storage.read(country, record.getKey());
//        Assertions.assertAll(validateRecord(record, actualRecord));
    }
}
*/
