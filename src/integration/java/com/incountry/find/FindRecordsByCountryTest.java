package com.incountry.find;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;

@Disabled
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
public class FindRecordsByCountryTest extends FindBaseTest {

    /*@DisplayName("Find records by key")
    @ParameterizedTest(name = "Find records by key" + testName)
    @CsvSource({"false, US"})
    public void findByKeyTest(boolean encryption, String country)
            throws GeneralSecurityException, StorageException, IOException {

        Storage storage = createStorage(encryption);
        FindOptions options = new FindOptions(100, 0);

        FindFilter filter = new FindFilter();
        BatchRecord batch = storage.find(country, filter, options);
        Record[] records = batch.getRecords();

        System.out.println("Total = " + batch.getTotal());
    }*/
}
