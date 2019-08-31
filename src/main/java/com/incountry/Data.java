package com.incountry;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class Data {
    String country;
    String key;
    String body;
    String profile_key;
    String range_key;
    String key2;
    String key3;

    public Data(String country, String key, String body, String profile_key, String range_key, String key2, String key3) {
        this.country = country;
        this.key = key;
        this.body = body;
        this.profile_key = profile_key;
        this.range_key = range_key;
        this.key2 = key2;
        this.key3 = key3;
    }

    private static String extractKey(JsonNode o, String k){
        if (o.has(k)){
            JsonNode v = o.get(k);
            if (!v.isNull()){
                return v.asText();
            }
        }
        return null;
    }

    public static Data fromString(String s) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode o = mapper.readTree(s);
        String country = extractKey(o, "country");
        String key = extractKey(o, "key");
        String body = extractKey(o, "body");
        String profile_key = extractKey(o, "profile_key");
        String range_key = extractKey(o, "range_key");
        String key2 = extractKey(o, "key2");
        String key3 = extractKey(o, "key3");
        return new Data(country, key, body, profile_key, range_key, key2, key3);
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getProfile_key() {
        return profile_key;
    }

    public void setProfile_key(String profile_key) {
        this.profile_key = profile_key;
    }

    public String getRange_key() {
        return range_key;
    }

    public void setRange_key(String range_key) {
        this.range_key = range_key;
    }

    public String getKey2() {
        return key2;
    }

    public void setKey2(String key2) {
        this.key2 = key2;
    }

    public String getKey3() {
        return key3;
    }

    public void setKey3(String key3) {
        this.key3 = key3;
    }

    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try { return mapper.writeValueAsString(this); }
        catch (Exception x) {
            x.printStackTrace();
            return null;
        }
    }
}
