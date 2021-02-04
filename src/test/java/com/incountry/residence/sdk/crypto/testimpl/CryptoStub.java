//package com.incountry.residence.sdk.crypto.testimpl;
//
//import com.incountry.residence.sdk.Deprecated.Crypto.Crypto;
//import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;
//
//import java.nio.charset.StandardCharsets;
//
//public class CryptoStub implements Crypto {
//
//    private boolean current;
//
//    public CryptoStub(boolean current) {
//        this.current = current;
//    }
//
//    @Override
//    public String encrypt(String text, SecretKey secretKey) {
//        String key = new String(secretKey.getSecret(), StandardCharsets.UTF_8);
//        return text != null ? text + ":" + key : key;
//    }
//
//    @Override
//    public String decrypt(String cipherText, SecretKey secretKey) {
//        String key = new String(secretKey.getSecret(), StandardCharsets.UTF_8);
//        return cipherText.equals(key) ? null : cipherText.substring(0, cipherText.lastIndexOf(":" + key));
//    }
//
//    @Override
//    public String getVersion() {
//        return CryptoStub.class.getSimpleName();
//    }
//
//    @Override
//    public boolean isCurrent() {
//        return current;
//    }
//}
