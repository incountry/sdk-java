package com.incountry.key_accessor.impl;

import com.incountry.key_accessor.ISecretKeyAccessor;

public class SecretKeyAccessor implements ISecretKeyAccessor {
    private String secret;

    public SecretKeyAccessor(String secret) {
        this.secret = secret;
    }

    @Override
    public String getKey() {
        return this.secret;
    }
}
