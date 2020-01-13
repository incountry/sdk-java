package com.incountry;

import com.incountry.exceptions.StorageException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

@RunWith(Parameterized.class)
public class UpdateOneTest extends BaseTest {

    private Record newRecord;
    private Record updatedRecord;

    @Before
    public void setUp() throws GeneralSecurityException, StorageException, IOException {
        newRecord = writeRecord(encryption, country);
        System.out.println(newRecord.getKey());
    }

    @After
    public void tearDown() throws Exception {
        storage.delete(country, updatedRecord.getKey());
    }

    @Test
    public void updateRecordByKeyTest()
            throws GeneralSecurityException, StorageException, IOException, FindOptions.FindOptionsException {

        FindFilter filter = new FindFilter();
        filter.setKeyParam(new FilterStringParam(newRecord.key));

        Record record = new Record();
        record.setCountry(newRecord.getCountry());
        record.setKey(newRecord.getKey());
        record.setKey2("UpdatedKey2_" + newRecord.getKey2());
        record.setKey3("UpdatedKey3_" + newRecord.getKey3());
        record.setProfileKey("UpdatedProfKey_" + newRecord.getProfileKey());
        record.setRangeKey(randomInt());
        record.setBody("UpdatedBody_" + newRecord.getBody());

        updatedRecord = storage.updateOne(country, filter, record);

        assertReflectionEquals("Validate Update response", record, updatedRecord);

        validateRecord(record);
    }

    @Test
    public void updateRecordByProfileKeyTest()
            throws GeneralSecurityException, StorageException, IOException, FindOptions.FindOptionsException {

        FindFilter filter = new FindFilter();
        filter.setProfileKeyParam(new FilterStringParam(newRecord.profileKey));

        Record record = new Record();
        record.setCountry(newRecord.getCountry());
        record.setKey(newRecord.getKey());
        record.setKey2("UpdatedKey2_" + newRecord.getKey2());
        record.setKey3("UpdatedKey3_" + newRecord.getKey3());
        record.setProfileKey("UpdatedProfKey_" + newRecord.getProfileKey());
        record.setRangeKey(randomInt());
        record.setBody("UpdatedBody_" + newRecord.getBody());

        updatedRecord = storage.updateOne(country, filter, record);
        assertReflectionEquals("Validate Update response", record, updatedRecord);

        validateRecord(record);
    }

    @Test
    public void updateRecordWithoutOverrideTest()
            throws GeneralSecurityException, StorageException, IOException, FindOptions.FindOptionsException {

        FindFilter filter = new FindFilter();
        filter.setProfileKeyParam(new FilterStringParam(newRecord.profileKey));

        Record record = new Record();
        record.setCountry(newRecord.getCountry());
        record.setKey2("UpdatedKey2_" + newRecord.getKey2());
        record.setProfileKey("UpdatedProfKey_" + newRecord.getProfileKey());
        record.setRangeKey(randomInt());
        record.setBody("UpdatedBody_" + newRecord.getBody());

        updatedRecord = storage.updateOne(country, filter, record);
        validateRecord(updatedRecord);
    }
}
