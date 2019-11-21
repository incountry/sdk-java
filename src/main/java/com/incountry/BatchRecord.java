package com.incountry;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.incountry.crypto.impl.Crypto;
import lombok.Getter;

import java.security.GeneralSecurityException;

@Getter
public class BatchRecord {
    int count;
    int limit;
    int offset;
    int total;
    Record[] records;

    public BatchRecord(Record[] records, int count, int limit, int offset, int total) {
        this.count = count;
        this.limit = limit;
        this.offset = offset;
        this.total = total;
        this.records = records;
    }

    public static BatchRecord fromString(String responseString, Crypto mCrypto) throws GeneralSecurityException {

        JsonObject responseObject = new Gson().fromJson(responseString, JsonObject.class);

        JsonObject meta = (JsonObject) responseObject.get("meta");
        int count = meta.get("count").getAsInt();
        int limit = meta.get("limit").getAsInt();
        int offset = meta.get("offset").getAsInt();
        int total = meta.get("total").getAsInt();

        Record[] records = new Record[count];
        if (count == 0) return new BatchRecord(records, count, limit, offset, total);

        JsonArray data = responseObject.getAsJsonArray("data");
        for (int i = 0; i < data.size(); i++)
        {
            records[i] = Record.fromString(data.get(i).toString(), mCrypto);
        }

        return new BatchRecord(records, count, limit, offset, total);
    }
}
