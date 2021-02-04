//package com.incountry.residence.sdk.crypto.testimpl;
//
//import com.incountry.residence.sdk.Deprecated.Crypto.DefaultCrypto;
//
//import java.nio.charset.StandardCharsets;
//
//public class DefaultCryptoWithCustomVersion extends DefaultCrypto {
//
//    private final String version;
//    private boolean current;
//
//    public DefaultCryptoWithCustomVersion(String version) {
//        super(StandardCharsets.UTF_8);
//        this.version = version;
//    }
//
//    public DefaultCryptoWithCustomVersion(String version, boolean current) {
//        this(version);
//        this.current = current;
//    }
//
//    @Override
//    public String getVersion() {
//        return version;
//    }
//
//    @Override
//    public boolean isCurrent() {
//        return current;
//    }
//}
