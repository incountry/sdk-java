package com.incountry.residence.sdk.example;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.services.kms.model.DecryptResult;
import com.incountry.residence.sdk.Storage;
import com.incountry.residence.sdk.StorageConfig;
import com.incountry.residence.sdk.StorageImpl;
import com.incountry.residence.sdk.crypto.EncryptionKey;
import com.incountry.residence.sdk.crypto.SecretsData;
import com.incountry.residence.sdk.crypto.Secret;
import com.incountry.residence.sdk.dto.Record;
import com.incountry.residence.sdk.tools.exceptions.StorageException;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Collections;

public class AwsKmsExample {

    private static final String ENVIRONMENT_ID = "<environment_id>";
    private static final String CLIENT_ID = "<client_id>";
    private static final String CLIENT_SECRET = "<client_secret>";

    /* For the details about AWS KMS CMK see
    https://docs.aws.amazon.com/kms/latest/developerguide/create-keys.html#create-symmetric-cmk */

    private static final String AWS_ACCESS_KEY_ID = "AWS_ACCESS_KEY_ID";
    private static final String AWS_SECRET_ACCESS_KEY = "AWS_SECRET_ACCESS_KEY";
    private static final String AWS_KMS_REGION = "us-east-1";
    private static final String AWS_KMS_MASTER_KEY_ID_ARN = "AWS_KMS_MASTER_KEY_ID_ARN";
    private static final String AWS_KMS_ENCRYPTED_KEY_BASE64 = "AWS_KMS_ENCRYPTED_KEY_BASE64";

    public void run() throws StorageException {
        AWSCredentials awsCredentials = new BasicAWSCredentials(AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY);
        AWSCredentialsProvider awsCredProvider = new AWSStaticCredentialsProvider(awsCredentials);
        AWSKMS kmsClient = AWSKMSClientBuilder.standard()
                .withCredentials(awsCredProvider)
                .withRegion(AWS_KMS_REGION)
                .build();
        DecryptRequest decryptRequest = new DecryptRequest();
        decryptRequest.setKeyId(AWS_KMS_MASTER_KEY_ID_ARN);
        decryptRequest.setCiphertextBlob(ByteBuffer.wrap(Base64.getDecoder().decode(AWS_KMS_ENCRYPTED_KEY_BASE64)));
        DecryptResult decryptedKey = kmsClient.decrypt(decryptRequest);
        ByteBuffer keyByteBuffer = decryptedKey.getPlaintext();

        Secret secret = new EncryptionKey(1, keyByteBuffer.array());
        SecretsData secretsData = new SecretsData(Collections.singletonList(secret), secret);
        StorageConfig config = new StorageConfig()
                .setEnvironmentId(ENVIRONMENT_ID)
                .setClientId(CLIENT_ID)
                .setClientSecret(CLIENT_SECRET)
                .setSecretKeyAccessor(() -> secretsData);
        Storage storage = StorageImpl.newStorage(config);
        Record record = new Record("recordKey-testAWSKMSKeys", "Test AWS KMS keys in Java SDK")
                .setKey1("<key1>")
                .setKey2("<key2>")
                .setKey3("<key3>")
                .setKey10("<key10>")
                .setProfileKey("<profile_key>")
                .setRangeKey1(125L);
        String country = "US";
        storage.write(country, record);
        storage.delete(country, record.getRecordKey());
    }
}
