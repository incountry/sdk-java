package com.incountry.keyaccessor.key;

import lombok.Data;

@Data
public class SecretKey {
    private String secret;
    private int version;
    private Boolean isKey;
}
