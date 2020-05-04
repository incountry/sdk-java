package com.incountry.residence.sdk.crypto.testimpl;

import com.incountry.residence.sdk.tools.crypto.DefaultCrypto;

import java.nio.charset.StandardCharsets;

public class PseudoCustomCrypto extends DefaultCrypto {
    private final boolean current;

    public PseudoCustomCrypto(boolean current) {
        super(StandardCharsets.UTF_8);
        this.current = current;
    }

    @Override
    public boolean isCurrent() {
        return current;
    }

    @Override
    public String getVersion() {
        return PseudoCustomCrypto.class.getSimpleName();
    }
}
