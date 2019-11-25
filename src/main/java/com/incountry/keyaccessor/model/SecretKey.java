package com.incountry.keyaccessor.model;

import lombok.Data;

@Data
public class SecretKey {

    private String secret;
    private int version;

}
