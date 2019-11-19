package com.incountry;

import com.incountry.crypto.Impl.Crypto;
import org.json.JSONArray;

public class FilterStringParam {
    String[] value;

    public FilterStringParam(String[] value) {
        this.value = value;
    }

    public FilterStringParam(String value) {
        if (value != null) this.value = new String[] {value};
    }

    private String[] hashValue(Crypto mCrypto){
        String[] result = new String[value.length];
        for (int i = 0; i < value.length; i++){
            result[i] = mCrypto.createKeyHash(value[i]);
        }
        return result;
    }

    public JSONArray toJSON(){
        return toJSON(null);
    }

    public JSONArray toJSON(Crypto mCrypto){
        if (value == null) return null;
        if (mCrypto == null) return new JSONArray(value);

        return new JSONArray(hashValue(mCrypto));
    }
}
