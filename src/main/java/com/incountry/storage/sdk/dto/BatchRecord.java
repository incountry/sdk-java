package com.incountry.storage.sdk.dto;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.incountry.storage.sdk.tools.crypto.Crypto;
import com.incountry.storage.sdk.tools.exceptions.RecordException;
import com.incountry.storage.sdk.tools.exceptions.StorageCryptoException;

import java.util.ArrayList;
import java.util.List;

public class BatchRecord {
    private int count;
    private int limit;
    private int offset;
    private int total;
    private List<Record> records;
    private List<RecordException> errors;

    public BatchRecord(List<Record> records, int count, int limit, int offset, int total, List<RecordException> errors) {
        this.count = count;
        this.limit = limit;
        this.offset = offset;
        this.total = total;
        this.records = records;
        this.errors = errors;
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

    public List<Record> getRecords() {
        return records;
    }

    public List<RecordException> getErrors() {
        return errors;
    }

    public static BatchRecord fromString(String responseString, Crypto mCrypto) throws StorageCryptoException {
        List<RecordException> errors = new ArrayList<>();

        JsonObject responseObject = new Gson().fromJson(responseString, JsonObject.class);

        JsonObject meta = (JsonObject) responseObject.get("meta");
        int count = meta.get("count").getAsInt();
        int limit = meta.get("limit").getAsInt();
        int offset = meta.get("offset").getAsInt();
        int total = meta.get("total").getAsInt();

        List<Record> records = new ArrayList<>();

        if (count == 0) return new BatchRecord(records, count, limit, offset, total, errors);

        JsonArray data = responseObject.getAsJsonArray("data");

        for (JsonElement item : data) {
            try {
                records.add(Record.fromString(item.toString(), mCrypto));
            } catch (Exception e) {
                errors.add(new RecordException("Record Parse Exception", item.toString(), e));
            }
        }

        return new BatchRecord(records, count, limit, offset, total, errors);
    }
}
