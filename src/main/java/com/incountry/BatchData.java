package com.incountry;

import com.incountry.crypto.Crypto;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.IOException;
import java.security.GeneralSecurityException;


public class BatchData {
    int count;
    int limit;
    int offset;
    int total;
    Data[] records;

    public BatchData(Data[] records, int count, int limit, int offset, int total) {
        this.count = count;
        this.limit = limit;
        this.offset = offset;
        this.total = total;
        this.records = records;
    }

    public static BatchData fromString(String s, Crypto mCrypto) throws IOException, GeneralSecurityException {
        JSONObject obj = new JSONObject(s);
        JSONObject meta = obj.getJSONObject("meta");
        int count = meta.getInt("count");
        int limit = meta.getInt("limit");
        int offset = meta.getInt("offset");
        int total = meta.getInt("total");

        Data[] records = new Data[count];
        if (count == 0) return new BatchData(records, count, limit, offset, total);

        JSONArray data = obj.getJSONArray("data");
        for (int i = 0; i < data.length(); i++)
        {
            records[i] = Data.fromString(data.getJSONObject(i).toString(), mCrypto);
        }

        return new BatchData(records, count, limit, offset, total);
    }

    public Data[] getRecords() {
        return records;
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
