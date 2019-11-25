package com.incountry.keyaccessor.model;

import lombok.Data;

import java.util.List;

@Data
public class SecretKeysData {
    private List<SecretKey> secrets;
    private int currentVersion;
}
