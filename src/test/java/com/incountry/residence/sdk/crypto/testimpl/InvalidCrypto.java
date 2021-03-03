//todo
//package com.incountry.residence.sdk.crypto.testimpl;
//
//import com.incountry.residence.sdk.tools.crypto.Crypto;
//import com.incountry.residence.sdk.tools.keyaccessor.key.SecretKey;
//
//import java.util.UUID;
//
//public class InvalidCrypto implements Crypto {
//
//    private boolean current;
//
//    public InvalidCrypto(boolean current) {
//        this.current = current;
//    }
//
//    @Override
//    public String encrypt(String text, SecretKey secretKey) {
//        return UUID.randomUUID().toString();
//    }
//
//    @Override
//    public String decrypt(String cipherText, SecretKey secretKey) {
//        return UUID.randomUUID().toString();
//    }
//
//    @Override
//    public String getVersion() {
//        return InvalidCrypto.class.getSimpleName();
//    }
//
//    @Override
//    public boolean isCurrent() {
//        return current;
//    }
//}
