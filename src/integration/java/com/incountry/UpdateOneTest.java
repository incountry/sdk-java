package com.incountry;

import com.incountry.exceptions.StorageException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

@RunWith(Parameterized.class)
public class UpdateOneTest extends BaseTest {

    @Test
    public void updateRecordByKeyTest()
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
        record.setRangeKey(randomInt());
        record.setBody("UpdatedBody_" + newRecord.getBody());

        Record updatedRecord = storage.updateOne(country, filter, record);
        assertReflectionEquals("Validate Update response", record, updatedRecord);

        validateRecord(record);
    }

    @Test
    public void updateRecordByProfileKeyTest()
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
        record.setRangeKey(randomInt());
        record.setBody("UpdatedBody_" + newRecord.getBody());

        Record updatedRecord = storage.updateOne(country, filter, record);
        assertReflectionEquals("Validate Update response", record, updatedRecord);

        validateRecord(record);
    }

    @Test
    public void updateRecordWithoutOverrideTest()
            throws GeneralSecurityException, StorageException, IOException, FindOptions.FindOptionsException {

        Record newRecord = createFullRecord(country);
        writeRecord(encryption, newRecord);

        FindFilter filter = new FindFilter();
        filter.setProfileKeyParam(new FilterStringParam(newRecord.profileKey));

        Record record = new Record();
        record.setCountry(newRecord.getCountry());
        record.setKey2("UpdatedKey2_" + newRecord.getKey2());
        record.setProfileKey("UpdatedProfKey_" + newRecord.getProfileKey());
        record.setRangeKey(randomInt());
        record.setBody("UpdatedBody_" + newRecord.getBody());

        BatchRecord batch = storage.find(country, filter, new FindOptions());

        Record updatedRecord = storage.updateOne(country, filter, record);
        validateRecord(updatedRecord);
    }
}
