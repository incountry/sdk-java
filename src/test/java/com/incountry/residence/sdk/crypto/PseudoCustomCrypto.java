package com.incountry.residence.sdk.crypto;

import com.incountry.residence.sdk.tools.crypto.DefaultCrypto;

import java.nio.charset.Charset;

public class PseudoCustomCrypto extends DefaultCrypto {
    private final boolean current;

    public PseudoCustomCrypto(Charset charset, boolean current) {
        super(charset);
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
