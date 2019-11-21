package com.incountry;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.incountry.crypto.Impl.Crypto;
import lombok.Getter;

import java.io.IOException;
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

        JsonObject meta1 = (JsonObject) responseObject.get("meta");
        int count = meta1.get("count").getAsInt();
        int limit = meta1.get("limit").getAsInt();
        int offset = meta1.get("offset").getAsInt();
        int total = meta1.get("total").getAsInt();

        // TODO change on ArrayList
        Record[] records = new Record[count];
        if (count == 0) return new BatchRecord(records, count, limit, offset, total);

        JsonArray data = responseObject.getAsJsonArray("data");
        // TODO change on foreach
        for (int i = 0; i < data.size(); i++)
        {
            records[i] = Record.fromString(data.get(i).toString(), mCrypto);
        }

        return new BatchRecord(records, count, limit, offset, total);
    }
}
