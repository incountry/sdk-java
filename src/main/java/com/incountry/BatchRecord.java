package com.incountry;

import com.incountry.crypto.Crypto;
import com.incountry.exceptions.RecordException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BatchRecord {
    int count;
    int limit;
    int offset;
    int total;
    Record[] records;
    List<RecordException> errors;

    public BatchRecord(Record[] records, int count, int limit, int offset, int total, List<RecordException> errors) {
        this.count = count;
        this.limit = limit;
        this.offset = offset;
        this.total = total;
        this.records = records;
        this.errors = errors;
    }

    public static BatchRecord fromString(String s, Crypto mCrypto) {
        List<Record> parsedRecords = new ArrayList<>();
        List<RecordException> errors = new ArrayList<>();

        JSONObject obj = new JSONObject(s);
        JSONObject meta = obj.getJSONObject("meta");
        int count = meta.getInt("count");
        int limit = meta.getInt("limit");
        int offset = meta.getInt("offset");
        int total = meta.getInt("total");

        if (count == 0) return new BatchRecord(new Record[0], count, limit, offset, total, errors);

        JSONArray data = obj.getJSONArray("data");

        for (int i = 0; i < data.length(); i++)
        {
            String recordString = data.getJSONObject(i).toString();
            try {
                parsedRecords.add(Record.fromString(recordString, mCrypto));
            } catch (Exception e) {
                errors.add(new RecordException("Record Parse Exception", recordString, e));
            }
        }

        Record[] records = new Record[parsedRecords.size()];
        records = parsedRecords.toArray(records);

        return new BatchRecord(records, count, limit, offset, total, errors);
    }

    public Record[] getRecords() {
        return records;
    }

    public List<RecordException> getErrors() {
        return errors;
    }

    public int getCount() {
        return count;
    }

    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }

    public int getTotal() {
        return total;
    }

}
