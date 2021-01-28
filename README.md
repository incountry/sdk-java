InCountry Storage SDK
===========
[![Build Status](https://travis-ci.com/incountry/sdk-java.svg?branch=master)](https://travis-ci.com/incountry/sdk-java)
[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=incountry_sdk-java&metric=alert_status)](https://sonarcloud.io/dashboard?id=incountry_sdk-java)
[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=incountry_sdk-java&metric=coverage)](https://sonarcloud.io/dashboard?id=incountry_sdk-java)
[![Known Vulnerabilities](https://snyk.io/test/github/incountry/sdk-java/badge.svg?targetFile=build.gradle)](https://snyk.io/test/github/incountry/sdk-java?targetFile=build.gradle)

## Installation
Incountry Storage SDK requires Java Developer Kit 1.8 or higher, recommended language level 8.

For Maven users please add this section to your dependencies list
```xml
<dependency>
  <groupId>com.incountry</groupId>
  <artifactId>incountry-java-client</artifactId>
  <version>3.1.0</version>
</dependency>
```

For Gradle users please add this line to your dependencies list
```groovy
compile "com.incountry:incountry-java-client:3.0.0"
```

## Countries List
For a full list of supported countries and their codes please [follow this link](countries.md).


## Quickstart guide
To access your data in InCountry Platform by using Java SDK, you need to create an instance of the `Storage` class using the `getInstance` method and pass `StorageConfig` object to it. You can retrieve the `CLIENT_ID`, `CLIENT_SECRET` and `ENV_ID` variables from your dashboard on InCountry Portal.
```java
SecretsData secretsData = SecretsDataGenerator.fromPassword("<encryption_secret>");
StorageConfig config = new StorageConfig()
        .setEnvId("<environment_id>")
        .setClientId("client_id")
        .setClientSecret("<client_secret>")
        .setSecretKeyAccessor(() -> secretsData);
Storage storage = StorageImpl.getInstance(config);
```

## Storage Configuration

Below you can find a full list of possible configuration options for creating a Storage instance.
```java
public class StorageImpl implements Storage {
  /**
   * creating Storage instance
   *
   * @param config A container with configuration for Storage initialization
   * @return instance of Storage
   * @throws StorageClientException if configuration validation finished with errors   
   */
  public static Storage getInstance(StorageConfig config)
                                    throws StorageClientException, StorageServerException  {...}
//...
}
```
StorageConfig provides the following parameters:
```java
/**
 * container with Storage configuration, using pattern 'builder'
 */
public class StorageConfig {
   //...
   /** Required to be passed in, or as environment variable INC_API_KEY */
   private String envId;
   /** Required when using oAuth authorization, can be also set via INC_CLIENT_ID */
   private String clientId;
   /** Required when using oAuth authorization, can be also set via INC_CLIENT_SECRET */
   private String clientSecret;
   /** Required when using API key authorization, or as environment variable */
   private String apiKey;
   /** Optional. Defines custom API URL, can also be set via INC_ENDPOINT */
   private String endPoint;
   /** Instance of SecretKeyAccessor class. Used to fetch encryption secret */
   private SecretKeyAccessor secretKeyAccessor;
   /** Optional. List of custom encryption configurations */
   private List<Crypto> customEncryptionConfigsList;
   /** Optional. If true - all keys will be stored as lower cased. default is false */
   private boolean normalizeKeys;
   /** Optional. Parameter endpointMask is used for switching from `default` InCountry host
    *  family (-mt-01.api.incountry.io) to a different one.  */
   private String endpointMask;
   /** Optional. Set custom endpoint for loading countries list */
   private String countriesEndpoint;
   /** Optional. Set HTTP requests timeout. Parameter is optional. Should be greater than 0.
    * Default value is 30 seconds. */
   private Integer httpTimeout;
   /** Set custom endpoints regional map to use for fetching oAuth tokens
    * Can be used only with {@link #defaultAuthEndpoint}
    * Format: key = region, value = authorization server URL for region */
   private Map<String, String> authEndpoints;
   /** Set custom oAuth authorization server URL, will be used as default one.
    * Can't be null when {@link #authEndpoints} is used */
   private String defaultAuthEndpoint;
   /** Optional. Set HTTP connections pool size. Expected value - null or positive integer.
    * Defaults to 20. */
   private Integer maxHttpPoolSize;
   /** Optional. Set maximum count of HTTP connections per route. 
    * Expected value - null or positive integer.
    * Default value == {@link #maxHttpPoolSize}. */
   private Integer maxHttpConnectionsPerRoute;
   /** Optional. If false - key1-key10 will be not hashed. Default is true */
   private boolean hashSearchKeys = true;
   //...
```

---
**WARNING**

API Key authorization is being deprecated. The backward compatibility is preserved for the API keys, but you no longer can access API keys (neither old nor new) from your dashboard.

Below you can find API Key authorization usage example:
```java
SecretsData secretsData = SecretsDataGenerator.fromPassword("<password>");
StorageConfig config = new StorageConfig()
    .setEnvId("<env_id>")
    .setApiKey("<api_key>")
    .setSecretKeyAccessor(() -> secretsData);
Storage storage=StorageImpl.getInstance(config);
```
---

#### oAuth options configuration

The SDK allows to precisely configure oAuth authorization endpoints (if needed). Use this option only if your plan configuration requires so.

Below you can find the example of how to create a storage instance with custom oAuth endpoints:
```java
Map<String, String> authEndpointsMap = new HashMap<>();
authEndpointsMap.put("emea", "https://auth-server-emea.com");
authEndpointsMap.put("apac", "https://auth-server-apac.com");
authEndpointsMap.put("amer", "https://auth-server-amer.com");

StorageConfig config = new StorageConfig()
   .setClientId(CLIENT_ID)
   .setClientSecret(SECRET)
   .setAuthEndpoints(authEndpointsMap)
   .setDefaultAuthEndpoint("https://auth-server-default.com")
   .setEndpointMask(ENDPOINT_MASK)
   .setEnvId(ENV_ID)
Storage storage = StorageImpl.getInstance(config);
```

Note: parameter endpointMask is used for switching from default InCountry host family (api.incountry.io) to a different one. For example setting `endpointMask`==`-private.incountry.io` will make all further requests to be sent to `https://{COUNTRY_CODE}-private.incountry.io`
If your PoPAPI configuration relies on a custom PoPAPI server (rather than the default one) use `countriesEndpoint` option to specify the endpoint responsible for fetching supported countries list.
```java
StorageConfig config = new StorageConfig()
   .setCountriesEndpoint(countriesEndpoint)
   //...
Storage storage = StorageImpl.getInstance(config);
```

### Encryption key/secret

SDK provides `SecretKeyAccessor` interface which allows you to pass your own secrets/keys to the SDK.
```java
/**
 * Secrets accessor. Method {@link SecretKeyAccessor#getSecretsData()} is invoked 
 * on each encryption/decryption.
 */
public interface SecretKeyAccessor {

    /**
     * get your container with secrets
     *
     * @return SecretsData
     * @throws StorageClientException when something goes wrong during getting secrets
     */
    SecretsData getSecretsData() throws StorageClientException;
}


public class SecretsData {
    /**
     * creates a container with secrets
     *
     * @param secrets non-empty list of secrets. One of the secrets must have
     *        same version as currentVersion in SecretsData
     * @param currentVersion Should be a non-negative integer
     * @throws StorageClientException when parameter validation fails
     */
     public SecretsData(List<SecretKey> secrets, int currentVersion)
                throws StorageClientException {...}
    //...
}


public class SecretKey {
    /**
    * Creates a secret key
    *
    * @param secret  secret/key as byte array
    * @param version secret version, should be a non-negative integer
    * @param isKey   should be True only for user-defined encryption keys
    * @throws StorageClientException when parameter validation fails
    */
    public SecretKey(byte[] secret, int version, boolean isKey)
              throws StorageClientException {...}
    //...
}
```

You can implement `SecretKeyAccessor` interface and pass secrets/keys in multiple ways:

1. As a constant SecretsData object
    ```java
    SecretsData secretsData = new SecretsData(secretsList, currentVersion);
    SecretKeyAccessor accessor = () -> secretsData;
    ```

2. As a function that dynamically fetches secrets
    ```java
    SecretKeyAccessor accessor = () -> loadSecretsData();

    private SecretsData loadSecretsData()  {
       String url = "<your_secret_url>";
       String responseJson = loadFromUrl(url).asJson();
       return SecretsDataGenerator.fromJson(responseJson);
    }
    ```

You can also use `SecretsDataGenerator` class for creating `SecretsData` instances:

1. from a String password
    ```java
    SecretsData secretsData = SecretsDataGenerator.fromPassword("<password>");
    ```

2. from a JSON string representing SecretsData object
    ```java
    SecretsData secretsData = SecretsDataGenerator.fromJson(jsonString);
    ```

```javascript
{
    "secrets": [
        {
            "secret": "secret0",
            "version": 0,
            "isKey": false
        },
        {
            "secret": "secret1",
            "version": 1,
            "isKey": false
        }
    ],
    "currentVersion": 1
}
```

`SecretsData` allows you to specify multiple keys/secrets which SDK will use for decryption based on the version of the key or secret used for encryption.

Meanwhile SDK will encrypt only using key/secret that matches `currentVersion` provided in `SecretsData` object. This enables the flexibility required to support Key Rotation policies when secrets/keys need to be changed with time.

SDK will encrypt data using current secret/key while maintaining the ability to decrypt records encrypted with old keys/secrets.
SDK also provides a method for data migration which allows to re-encrypt data with the newest key/secret. For details please see [migrate](#Data-Migration-and-Key-Rotation-support) method.

SDK allows you to use custom encryption keys, instead of secrets. Please note that user-defined encryption key should be a 32-bytes-long key as it's required by AES-256 cryptographic algorithm (base64 encoded when secrets data is loaded from JSON).

Note: even though SDK uses PBKDF2 to generate a cryptographically strong encryption key, you must make sure you provide a secret/password which follows modern security best practices and standards.

### Writing data to Storage

Use `write` method in order to create a record.
```java
public interface Storage {
    /**
     * Write data to remote storage
     *
     * @param country country identifier
     * @param record  object which encapsulate data which must be written in storage
     * @return recorded record
     * @throws StorageClientException if validation finished with errors
     * @throws StorageServerException if server connection failed or server response error
     * @throws StorageCryptoException if encryption failed
     */
    Record write(String country, Record record)
          throws StorageClientException, StorageServerException, StorageCryptoException;
    //...
}
```

Here is how you initialize a record object:
```java
public class Record {
   /**
    * Minimalistic constructor
    *
    * @param recordKey record key
    */
    public Record(String recordKey) {...};

   /**
    * Overloaded constructor
    *
    * @param recordKey record key
    * @param body      data to be stored and encrypted
    */
    public Record(String recordKey, String body) {...}
    //...
}
```

Below is the example of how you may use `write` method:
```java
Record record = new Record("some_key")
    .setBody("some PII data")
    .setProfileKey("customer")
    .setKey1("hatchback")
    .setKey2("english")
    .setKey3("insurance")
    .setRangeKey1(10_000L)
storage.write("us", record);
```

#### List of available record fields
v3.0.0 release introduced a series of new fields available for storage. Below is an exhaustive list of fields available for storage in InCountry along with their types and  storage methods - each field is either encrypted, hashed or stored as is:
```java
public class Record {
    //String fields, hashed
    private String recordKey;
    private String parentKey;
    private String profileKey;
    private String serviceKey1;
    private String serviceKey2;
   //String fields, hashed or original
    private String key1;
    private String key2;
    private String key3;
    private String key4;
    private String key5;
    private String key6;
    private String key7;
    private String key8;
    private String key9;
    private String key10;
    private String key11;
    private String key12;
    private String key13;
    private String key14;
    private String key15;
    private String key16;
    private String key17;
    private String key18;
    private String key19;
    private String key20;
    //String fields, encrypted
    private String body;
    private String precommitBody;
    //Long fields, plain
    private Long rangeKey1;
    private Long rangeKey2;
    private Long rangeKey3;
    private Long rangeKey4;
    private Long rangeKey5;
    private Long rangeKey6;
    private Long rangeKey7;
    private Long rangeKey8;
    private Long rangeKey9;
    private Long rangeKey10;
    //Readonly service fields, date
    protected Date createdAt;
    protected Date updatedAt;
```
You can access all the properties using appropriate getters and setters, for example:

```java
String key2 = record.getKey2();
String body = record.getBody();

record.setProfileKey("customer")
    .setRangeKey1(1_000L)
    .setKey3("grey");
```

#### Date fields
Use `createdAt` and `updatedAt` fields to access date-related information about records. `createdAt` indicates date when the record was initially created in the target country. `updatedAt` shows the date of the latest write operation for the given `recordKey`

### Batches

Use the `batchWrite` method to write multiple records to the storage in a single request.
```java
public interface Storage {
     /**
      * Write multiple records at once in remote storage
      *
      * @param country country identifier
      * @param records record list
      * @return BatchRecord object which contains list of recorded records
      * @throws StorageClientException if validation finished with errors
      * @throws StorageServerException if server connection failed or server response error
      * @throws StorageCryptoException if record encryption failed
      */
     BatchRecord batchWrite(String country, List<Record> records)
          throws StorageClientException, StorageServerException, StorageCryptoException;
     //...
}
```

 Below is the example of how you may use `batchWrite` method
```java
List<Record> list = new ArrayList<>();
list.add(new Record("some_record_key","some PII data"));
list.add(new Record("another_record_key","another PII data"));
storage.batchWrite("us", list);
```

### Reading stored data

Stored record can be read by `recordKey` using `read` method.
```java
public interface Storage {
   /**
    * Read data from remote storage
    *
    * @param country   country identifier
    * @param recordKey record unique identifier
    * @return Record object which contains required data
    * @throws StorageClientException if validation finished with errors
    * @throws StorageServerException if server connection failed or server response error
    * @throws StorageCryptoException if decryption failed
    */
    Record read(String country, String recordKey)
        throws StorageClientException, StorageServerException, StorageCryptoException;
    //...
}
```

Below is the example of how you may use `read` method:
 ```java
String recordKey = "user_1";
Record record = storage.read("us", recordKey);
String decryptedBody = record.getBody();
 ```

### Find records

It is possible to search by random keys using `find` method.
```java
public interface Storage {
   /**
    * Find records in remote storage according to filters
    *
    * @param country country identifier
    * @param builder object representing find filters and search options
    * @return BatchRecord object which contains required records
    * @throws StorageClientException if validation finished with errors
    * @throws StorageServerException if server connection failed or server response error
    * @throws StorageCryptoException if decryption failed
    */
    BatchRecord find(String country, FindFilterBuilder builder)
         throws StorageClientException, StorageServerException, StorageCryptoException;
    //...
}
```

Use `FindFilterBuilder` class to refine your find request.

Below is the example how to use `find` method along with `FindFilterBuilder`:
```java
FindFilterBuilder builder = FindFilterBuilder.create()
                  .keyEq(StringField.KEY2, "someKey")
                  .keyEq(StringField.KEY3, "firstValue", "secondValue")
                  .keyEq(NumberField.RANGE_KEY1, 123L, 456L);

BatchRecord findResult = storage.find("us", builder);
if (findResult.getCount() > 0) {
    Record record = findResult.getRecords().get(0);
    //...
}
```

The request will return records, filtered according to the following pseudo-sql
```sql
key2 = 'someKey' AND key3 in ('firstValue' , 'secondValue') AND (123 < = `rangeKey1` < = 456)
```

All conditions added via `FindFilterBuilder` are joined using logical `AND`. You may not add multiple conditions for the same key - if you do only the last one will be used.

SDK returns 100 records at most. Use `limit` and `offset` to iterate through the records.
```java
FindFilterBuilder builder = FindFilterBuilder.create()
                  //...
                  .limitAndOffset(20, 80);
BatchRecord records = storage.find("us", builder);
```

---
**NOTE**

Sorting find results is currently available for InCountry dedicated instances only. Please check your subscription plan for details. This may require specifying your dedicated instance endpoint when configuring Java SDK Storage.

---

By default, data at a find result is not sorted. To sort the returned records by one or multiple keys use method `sortBy` of `FindFilterBuilder` .
```java
FindFilterBuilder builder = FindFilterBuilder.create()
                  //...
                  .sortBy(SortFields.CREATED_AT, SortOrder.ASC)
                  .sortBy(SortFields.RANGE_KEY1, SortOrder.DESC)
BatchRecord records = storage.find("us", builder);
```
The request will return records, sorted according to the following pseudo-sql
```sql
SELECT * FROM record WHERE ...  ORDER BY created_at asc, range_key1 desc
```

##### Fields that records can be sorted by:
```java
public enum SortFields {
   KEY1,
   KEY2,
   KEY3,
   KEY4,
   KEY5,
   KEY6,
   KEY7,
   KEY8,
   KEY9,
   KEY10,
   KEY11,
   KEY12,
   KEY13,
   KEY14,
   KEY15,
   KEY16,
   KEY17,
   KEY18,
   KEY19,
   KEY20,
   RANGE_KEY1,
   RANGE_KEY2,
   RANGE_KEY3,
   RANGE_KEY4,
   RANGE_KEY5,
   RANGE_KEY6,
   RANGE_KEY7,
   RANGE_KEY8,
   RANGE_KEY9,
   RANGE_KEY10,
   CREATED_AT,
   UPDATED_AT
}
```

Next predicate types are available for each string key field of class `Record` via individual methods of `FindFilterBuilder`:
```java
EQUALS              (FindFilterBuilder::keyEq)
NOT_EQUALS          (FindFilterBuilder::keyNotEq)
IS_NULL/IS_NOT_NULL (FindFilterBuilder::nullable)

```

You can use the following builder methods for filtering by numerical fields:
```java
EQUALS              (FindFilterBuilder::keyEq)
IS_NULL/IS_NOT_NULL (FindFilterBuilder::nullable)
IN                  (FindFilterBuilder::keyIn)
GREATER             (FindFilterBuilder::keyGT)
GREATER OR EQUALS   (FindFilterBuilder::keyGTE)
LESS                (FindFilterBuilder::keyLT)
LESS OR EQUALS      (FindFilterBuilder::keyLTE)
BETWEEN             (FindFilterBuilder::keyBetween)
```

Method `find` returns `BatchRecord` object which contains a list of `Record` and some metadata:
```java
class BatchRecord {
    private int count;
    private int limit;
    private int offset;
    private int total;
    private List<Record> records;
    //...
}
```

These fields can be accessed using getters, for example:

```java
int limit = records.getTotal();
```

`BatchRecord.getErrors()` allows you to get a List of `RecordException` objects which contains detailed information about records that failed to be processed correctly during `find` request.

### Find one record matching filter

If you need to find the first record matching filter, you can use `findOne` method:
```java
public interface Storage {
   /**
    * Find only one first record in remote storage according to filters
    *
    * @param country country identifier
    * @param builder object representing find filters
    * @return founded record or null
    * @throws StorageClientException if validation finished with errors
    * @throws StorageServerException if server connection failed or server response error
    * @throws StorageCryptoException if decryption failed
    */
    Record findOne(String country, FindFilterBuilder builder)
           throws StorageClientException, StorageServerException, StorageCryptoException;
    //...
}
```

It works the same way as `find` but returns the first record or `null` if no records found.

Here is the example of how `findOne` method can be used:
```java
FindFilterBuilder builder = FindFilterBuilder.create()
                  .keyEq(StringField.KEY2, "someKey")
                  .keyEq(StringField.KEY3, "firstValue", "secondValue")
                  .keyEq(NumberField.RANGE_KEY1, 123L, 456L);

Record record = storage.findOne("us", builder);
//...
```

### Delete records

Use `delete` method in order to delete a record from InCountry storage. It is only possible using `key` field.
```java
public interface Storage {
    /**
    * Delete record from remote storage
    *
    * @param country   country code of the record
    * @param recordKey the record's key
    * @return true when record was deleted
    * @throws StorageClientException if validation finished with errors
    * @throws StorageServerException if server connection failed
    */
    boolean delete(String country, String recordKey)
            throws StorageClientException, StorageServerException;
    //...
}
```


Below is the example of how you may use `delete` method:
 ```java
String recordKey = "user_1";
storage.delete("us", recordKey);
 ```

## Attaching files to a record

---
**NOTE**

Attachments are currently available for InCountry dedicated instances only. Please check your subscription plan for details. This may require specifying your dedicated instance endpoint when configuring InCountry Java SDK Storage.

---

InCountry Storage allows you to attach files to the previously created records. Attachments' meta information is available through the `attachments` field of `Record` object.
```java
public class Record {
    /** ... other fields ...  */
    private List<AttachmentMeta> attachments;
}

public class AttachmentMeta {
    private Date createdAt;
    private Date updatedAt;
    private String downloadLink;
    private String fileId;
    private String filename;
    private String hash;
    private String mimeType;
    private int size;
    //...
}
```

### Adding attachments

The `addAttachment` method of `Storage` instance allows you to add or replace attachments. File data can be provided as `InputStream`.
```java
public interface Storage {
    /**
     * Add attached file to existing record
     *
     * @param country         country identifier
     * @param recordKey       the record's recordKey
     * @param fileInputStream input data stream
     * @param fileName        file name
     * @param upsert          if true will overwrite existing file with the same name.
     *                        Otherwise will throw exception
     * @param mimeType        mime type for attached file
     * @return AttachmentMeta attachment meta information: fileId, mimeType, size, etc.
     * @throws StorageClientException if validation finished with errors
     * @throws StorageServerException if server connection failed or server response error
     */
    AttachmentMeta addAttachment(String country, String recordKey, InputStream fileInputStream, 
                                 String fileName, boolean upsert, String mimeType)
             throws StorageClientException, StorageServerException;
    //...
}
```

Example of usage:
```java
File initialFile = new File("example.txt");
InputStream stream = new FileInputStream(initialFile);
storage.addAttachment(COUNTRY, RECORD_KEY, stream, "example.txt", false, MIME_TYPE);
```

### Deleting attachments

The `deleteAttachment` method of `Storage` instance allows you to delete attachment using its `fileId`.
```java
public interface Storage {
    /**
     * Delete attached file of existing record
     *
     * @param country   country identifier
     * @param recordKey the record's recordKey
     * @param fileId    file identifier
     * @return true when file was deleted
     * @throws StorageClientException if validation finished with errors
     * @throws StorageServerException if server connection failed or server response error
     */
    boolean deleteAttachment(String country, String recordKey, String fileId) 
            throws StorageClientException, StorageServerException;
    //...
}
```

Example of usage:
```java
storage.deleteAttachment(COUNTRY, RECORD_KEY, fileId);
```

### Downloading attachments

The `getAttachmentFile` method of `Storage` instance allows you to download attachment contents. It returns an `AttachedFile` class instance with a readable stream and filename.
```java
public interface Storage {
    /**
     * Get attached file of existing record
     *
     * @param country   country identifier
     * @param recordKey the record's recordKey
     * @param fileId    file identifier
     * @return AttachedFile object which contains required file
     * @throws StorageClientException if validation finished with errors
     * @throws StorageServerException if server connection failed or server response error
     */
    AttachedFile getAttachmentFile(String country, String recordKey, String fileId) 
            throws StorageClientException, StorageServerException;
    //...
}

public class AttachedFile {
   private final InputStream fileContent;
   private final String fileName;
   //...
}
```

Example of usage:
```java
AttachedFile attachement = storageForAttachment.getAttachmentFile(COUNTRY, RECORD_KEY, fileId);
File file = new File(attachement.getFileName());
FileUtils.copyInputStreamToFile(attachement.getFileContent(), file);
```

### Working with attachment meta info

The `getAttachmentMeta` method of `Storage` instance allows you to retrieve attachment's metadata using its `fileId`.
```java
public interface Storage {
    /**
     * Get attached file meta information
     *
     * @param country   country identifier
     * @param recordKey the record's recordKey
     * @param fileId    file identifier
     * @return AttachmentMeta object which contains required meta information fileId,
     * mimeType, size, filename, downloadLink, updatedAt and createdAt
     * @throws StorageClientException if validation finished with errors
     * @throws StorageServerException if server connection failed or server response error
     */
    AttachmentMeta getAttachmentMeta(String country, String recordKey, String fileId) 
            throws StorageClientException, StorageServerException;
    //...
}
```

Example of usage:
```java
AttachmentMeta meta = storageForAttachment.getAttachmentMeta(COUNTRY, RECORD_KEY, fileId);
```

The `updateAttachmentMeta` method of `Storage` allows you to update attachment's metadata (MIME type and file name).
```java
public interface Storage {
    /**
     * Update attached file meta information
     *
     * @param country   country identifier
     * @param recordKey the record's recordKey
     * @param fileId    file identifier
     * @param fileName  file name (optional if mimeType provided)
     * @param mimeType  file MIME type (optional if fileName provided)
     * @return AttachmentMeta object which contains updated fields
     * @throws StorageClientException if validation finished with errors
     * @throws StorageServerException if server connection failed or server response error
     */
    AttachmentMeta updateAttachmentMeta(String country, String recordKey, String fileId, 
                                        String fileName, String mimeType) 
            throws StorageClientException, StorageServerException;
    //...
}
```

Example of usage:
```java
AttachmentMeta meta = storage
        .updateAttachmentMeta(COUNTRY, RECORD_KEY, fileId, NEW_FILE_NAME, NEW_MIME_TYPE);
```

Data Migration and Key Rotation support
-----

Using `SecretKeyAccessor` that provides `SecretsData` object enables key rotation and data migration support.

SDK introduces method `migrate`
```java
public interface Storage {
   /**
    * Make batched key-rotation-migration of records
    *
    * @param country country identifier
    * @param limit   batch-limit parameter
    * @return MigrateResult object which contain total records
    *         left to migrate and total amount of migrated records
    * @throws StorageClientException if validation finished with errors
    * @throws StorageServerException if server connection failed or server response error
    * @throws StorageCryptoException if decryption failed
    */
    MigrateResult migrate(String country, int limit)
           throws StorageClientException, StorageServerException, StorageCryptoException;
    //...
}
```

It allows you to re-encrypt data encrypted with old versions of the secret. You should specify `country` you want to conduct migration in
and `limit` for precise amount of records to migrate. `migrate` returns a `MigrateResult` object which contains some information about the migration - the
amount of records migrated (`migrated`) and the amount of records left to migrate (`totalLeft`) (which basically means the amount of records with
version different from `currentVersion` provided by `SecretKeyAccessor`)

```java
public class MigrateResult {
    private int migrated;
    private int totalLeft;
    //...
}
```

For detailed example of a migration usage please [follow this link](/src/integration/java/com/incountry/residence/sdk/FullMigrationExample.java).

## Error Handling

InCountry Java SDK throws following Exceptions:
- **StorageClientException** - used for various input validation errors
- **StorageServerException** - thrown if SDK failed to communicate with InCountry servers or if server response validation failed.
- **StorageCryptoException** - thrown during encryption/decryption procedures (both default and custom). This may be a sign of malformed/corrupt data or a wrong encryption key provided to the SDK.
- **StorageException** - general exception. Inherited by all other exceptions

We suggest gracefully handling all the possible exceptions:

```java
public void test() {
    try {
        // use InCountry Storage instance here
    } catch (StorageClientException e) {
        // some input validation error
    } catch (StorageServerException e) {
        // some server error
    } catch (StorageCryptoException e) {
        // some encryption error
    } catch (StorageException e) {
        // general error
    } catch (Exception e) {
        // something else happened not related to InCountry SDK
    }
}
```

## Custom Encryption Support

SDK supports the ability to provide custom encryption/decryption methods if you decide to use your own algorithm instead of the default one.

Use method `setCustomEncryptionConfigsList` of `StorageConfig` for passing a list of custom encryption implementations:

```java
public class StorageConfig {
  //...
  /**
   * for custom encryption
   *
   * @param customEncryptionConfigsList List with custom encryption functions
   * @return StorageConfig
   */
  public StorageConfig setCustomEncryptionConfigsList(List<Crypto> customEncryptionConfigsList) {
      this.customEncryptionConfigsList = customEncryptionConfigsList;
      return this;
  }
  //...
}
```

For using of custom encryption you need to implement the following interface:
```java
public interface Crypto {
    /**
     * encrypts data with secret
     *
     * @param text      data for encryption
     * @param secretKey secret
     * @return encrypted data as String
     * @throws StorageClientException when parameters validation fails
     * @throws StorageCryptoException when decryption fails
     */
    String encrypt(String text, SecretKey secretKey)
            throws StorageClientException, StorageCryptoException;

    /**
     * decrypts data with Secret
     *
     * @param cipherText encrypted data
     * @param secretKey  secret
     * @return decrypted data as String
     * @throws StorageClientException when parameters validation fails
     * @throws StorageCryptoException when decryption fails
     */
    String decrypt(String cipherText, SecretKey secretKey)
            throws StorageClientException, StorageCryptoException;

    /**
     * version of encryption algorithm as String
     *
     * @return version
     */
    String getVersion();

    /**
     * only one CustomCrypto can be current. This parameter
     * used only during {@link com.incountry.residence.sdk.Storage}
     * initialisation. Changing this parameter will be ignored after initialization
     *
     * @return is current or not
     */
    boolean isCurrent();
}
```

---
**NOTE**

You should provide a specific `SecretKey` via `SecretsData` passed to `SecretKeyAccessor`. This secret should have flag `isForCustomEncryption` set to `true` and flag `isKey` set to `false`:
```java
public class SecretKey {
    /**
     * @param secret secret/key as byte array
     * @param version secret version, should be a non-negative integer
     * @param isKey should be True only for user-defined encryption keys
     * @param isForCustomEncryption should be True for using this key in custom encryption
     *                              implementations. Either ({@link #isKey} or
     *                              {@link #isForCustomEncryption}) can be True at the same
     *                              moment, not both
     * @throws StorageClientException when parameter validation fails
     */
    public SecretKey(byte[] secret, int version, boolean isKey, boolean isForCustomEncryption)
              throws StorageClientException {...}
    //...
}
```

You can set `isForCustomEncryption` using `SecretsData` JSON format as well:
```javascript
secrets_data = {
  "secrets": [{
     "secret": "MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwQUI=", //base64-encoded key (32 byte key)
     "version": 1,
     "isForCustomEncryption": true,
    }
  }],
  "currentVersion": 1,
}
```
---

`version` attribute is used to differ one custom encryption from another and from the default encryption as well.
This way SDK will be able to successfully decrypt any old data if encryption changes with time.

`isCurrent` attribute allows to specify one of the custom encryption implementations that will be used for encryption. Only one implementation can be set as `isCurrent() == true`.

If none of the configurations have `isCurrent() == true` then the SDK will use default encryption to encrypt stored data. At the same time it will keep the ability to decrypt old data, encrypted with custom encryption (if any).

Here's an example of how you can set up SDK to use custom encryption (using Fernet encryption from https://github.com/l0s/fernet-java8 )

```java
/**
 * Example of custom implementation of {@link Crypto} using Fernet algorithm
 */
public class FernetCrypto implements Crypto {
    private static final String VERSION = "fernet custom encryption";
    private boolean current;
    private Validator<String> validator;

    public FernetCrypto(boolean current) {
        this.current = current;
        this.validator = new StringValidator() {
        };
    }

    @Override
    public String encrypt(String text, SecretKey secretKey)
            throws StorageCryptoException {
        try {
            Key key = new Key(secretKey.getSecret());
            Token result = Token.generate(key, text);
            return result.serialise();
        } catch (IllegalStateException ex) {
            throw new StorageCryptoException("Encryption error", ex);
        }
    }

    @Override
    public String decrypt(String cipherText, SecretKey secretKey)
            throws StorageCryptoException {
        try {
            Key key = new Key(secretKey.getSecret());
            Token result = Token.fromString(cipherText);
            return result.validateAndDecrypt(key, validator);
        } catch (PayloadValidationException ex) {
            throw new StorageCryptoException("Decryption error", ex);
        }
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public boolean isCurrent() {
        return current;
    }
}
```

## Project dependencies

The following is a list of compile dependencies for this project. These dependencies are required to compile and run the application:

| **GroupId**               | **ArtifactId**       | **Version** | **Type** |
| :---:                     | :---:                | :---:       | :---:    |
| javax.xml.bind            | jaxb-api             | 2.3.1       | jar      |
| javax.activation          | javax.activation-api | 1.2.0       | jar      |
| commons-codec             | commons-codec        | 1.14        | jar      |
| commons-logging           | commons-logging      | 1.2         | jar      |
| org.apache.logging.log4j  | log4j-api            | 2.13.3      | jar      |
| org.apache.logging.log4j  | log4j-core           | 2.13.3      | jar      |
| org.apache.logging.log4j  | log4j-core-jcl       | 2.13.3      | jar      |
| org.apache.httpcomponents | httpclient           | 4.5.12      | jar      |
| org.apache.httpcomponents | httpcore             | 4.4.13      | jar      |
| com.google.code.gson      | gson                 | 2.8.6       | jar      |

#### Dependency Tree
```
compileClasspath
+--- javax.xml.bind:jaxb-api:2.3.1
|    \--- javax.activation:javax.activation-api:1.2.0
+--- commons-codec:commons-codec:1.14
+--- com.google.code.gson:gson:2.8.6
+--- org.apache.logging.log4j:log4j-api:2.13.3
+--- org.apache.logging.log4j:log4j-core:2.13.3
|    \--- org.apache.logging.log4j:log4j-api:2.13.3
+--- org.apache.logging.log4j:log4j-jcl:2.13.3
|    +--- commons-logging:commons-logging:1.2
|    \--- org.apache.logging.log4j:log4j-api:2.13.3
\--- org.apache.httpcomponents:httpclient:4.5.12
     +--- org.apache.httpcomponents:httpcore:4.4.13
     +--- commons-logging:commons-logging:1.2
     \--- commons-codec:commons-codec:1.11 -> 1.14
```

### Minimal JVM memory options
```
-Xms8m
-Xmx16m
-XX:MaxMetaspaceSize=32m
```
