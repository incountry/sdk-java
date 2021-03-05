package com.incountry.residence.sdk;

import com.incountry.residence.sdk.dto.FindResult;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.tools.exceptions.RecordException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FindResultTest {
    @Test
    void toStringPositive() {
        FindResult findResult = new FindResult(null, 1, 2, 3, 4, null);
        assertEquals("FindResult{count=1, limit=2, offset=3, total=4, records=[], errors=null}",
                findResult.toString());
        FindResult findResult1 = new FindResult(new ArrayList<>(), 6, 7, 8, 9, null);
        assertEquals("FindResult{count=6, limit=7, offset=8, total=9, records=[], errors=null}",
                findResult1.toString());
        FindResult findResult2 = new FindResult(Collections.singletonList(new Record("recKey")), 10, 11, 12, 13, null);
        assertEquals("FindResult{count=10, limit=11, offset=12, total=13, records=[Record{recordKey='recKey', hash=109746541}], errors=null}",
                findResult2.toString());
    }

    @Test
    void getFindResultFieldsPositive() {
        FindResult findResult = new FindResult(Collections.singletonList(new Record("key")),
                1, 2, 3, 4, Collections.singletonList(new RecordException("data")));
        assertEquals(1, findResult.getCount());
        assertEquals(2, findResult.getLimit());
        assertEquals(3, findResult.getOffset());
        assertEquals(4, findResult.getTotal());
        assertEquals(1, findResult.getErrors().size());
        assertEquals(1, findResult.getRecords().size());
    }
}
