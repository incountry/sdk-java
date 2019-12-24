package com.incountry.find;

import com.incountry.BaseTest;

public abstract class FindBaseTest extends BaseTest {

    /*protected FindOptions defaultFindOptions;

    {
        try {
            defaultFindOptions = new FindOptions(10, 0);
        } catch (FindOptions.FindOptionsException e) {
            e.printStackTrace();
        }
    }

    public void findRecords(String country, Record expectedRecord, FindFilter filter, FindOptions findOptions)
            throws StorageException, IOException, GeneralSecurityException {
        BatchRecord batch = storage.find(country, filter, findOptions);
        Record[] records = batch.getRecords();

        Assertions.assertAll("Validate batch fields",
                validateBatchFields(findOptions, records.length, batch));

        Assertions.assertAll("Validate record",
                validateRecord(expectedRecord, records[0]));
    }*/

}
