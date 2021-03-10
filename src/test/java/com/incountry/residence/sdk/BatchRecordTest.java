package com.incountry.residence.sdk;

import com.incountry.residence.sdk.dto.BatchRecord;
import com.incountry.residence.sdk.dto.Record;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BatchRecordTest {
    @Test
    void toStringTest() {
        BatchRecord batch1 = new BatchRecord(null, 1, 2, 3, 4, null);
        assertEquals("BatchRecord{count=1, limit=2, offset=3, total=4, records=[], errors=null}",
                batch1.toString());
        BatchRecord batch2 = new BatchRecord(new ArrayList<>(), 6, 7, 8, 9, null);
        assertEquals("BatchRecord{count=6, limit=7, offset=8, total=9, records=[], errors=null}",
                batch2.toString());
        BatchRecord batch3 = new BatchRecord(Collections.singletonList(new Record("recKey")), 10, 11, 12, 13, null);
        assertEquals("BatchRecord{count=10, limit=11, offset=12, total=13, records=[Record{recordKey='recKey', hash=1957511887}], errors=null}",
                batch3.toString());
    }
}
