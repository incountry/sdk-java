package com.incountry.residence.sdk;

import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.tools.JsonUtils;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecordTest {

    @Test
    void createTestTrivial() {
        Record record = new Record();
        record.setRecordKey("<RecordKey>");
        record.setBody("<testBody>");
        assertEquals("<RecordKey>", record.getRecordKey());
        assertEquals("<testBody>", record.getBody());
    }

    @Test
    void createFromJsonWithServiceFields() throws StorageServerException, ParseException {
        String json = "{ " +
                "\"record_key\": \"123321\"," +
                "\"version\": 7," +
                "\"body\": \"someBody\"," +
                "\"created_at\": \"2020-12-21T00:00:00.000+0300\"," +
                "\"updated_at\": \"2020-12-22T00:00:00.000+0300\"" +
                "}";
        Record record = (Record) JsonUtils.jsonStringToObject(json, Record.class);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String dateInString1 = "2020-12-21T00:00:00";
        String dateInString2 = "2020-12-22T00:00:00";
        Date date1 = sdf.parse(dateInString1);
        Date date2 = sdf.parse(dateInString2);
        assertEquals(date1.toString(), record.getCreatedAt().toString());
        assertEquals(date2.toString(), record.getUpdatedAt().toString());
        assertEquals(7, record.getVersion());
        assertEquals("123321", record.getRecordKey());
        assertEquals("someBody", record.getBody());
    }
}
