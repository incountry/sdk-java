InCountry Storage SDK
============

Installation
-----
Incountry Storage SDK requires Java Developer Kit 1.8 or higher, recommended language level - 8.

For Maven users please add this section to your dependencies list
```xml
<dependency>
  <groupId>com.incountry</groupId>
  <artifactId>incountry-java-client</artifactId>
  <version>2.0.0</version>
</dependency>
```

For Gradle users plase add this line to your dependencies list
```groovy
compile "com.incountry:incountry-java-client:2.0.0"
```

Countries List
----
For a full list of supported countries and their codes please [follow this link](countries.md).

Usage
-----
To access your data in InCountry using Java SDK, you need to create an implementation of `Storage` interface. Use 'StorageImpl' for it 
```java
public class StorageImpl implements Storage {        
        public static Storage getInstance(String environmentID,                  // Required to be passed in, or as environment variable INC_API_KEY 
                                          String apiKey,                         // Required to be passed in, or as environment variable INC_ENVIRONMENT_ID
                                          String endpoint,                       // Optional. Defines API URL. Default endpoint will be used if this param is null
                                          SecretKeyAccessor secretKeyAccessor)   // Instance of SecretKeyAccessor class. Used to fetch encryption secret
            throws StorageServerException {...}                          
//...
}
```

Parameters `apiKey` and `environmentID` can be fetched from your dashboard on `Incountry` site.

`endpoint` defines API URL and is used to override default one.

You can turn off encryption (not recommended). For it use `null` value for parameter `secretKeyAccessor`.

Below is an example how to create a storage instance:
```java
public class StorageCreationExample {
    public Storage initStorage() throws StorageException {        
        SecretKeyAccessor secretKeyAccessor = SecretKeyAccessor.getAccessor("somePassword");
        String endPoint = "https://us.api.incountry.io";        
        String envId = "someEnvironmentId";
        String apiKey = "someApiKey";
        return StorageImpl.getInstance(envId, apiKey, endPoint, secretKeyAccessor);        
    }
}
```

### Encryption key/secret

The SDK has a `SecretKeyAccessor` interface which allows you to pass your own secrets/keys to the SDK.

`SecretKeyAccessor` allows you to pass a function that should return representing your secret in `SecretsData` class instance:
```java
public interface SecretKeyAccessor {    
    SecretsData getSecretsData();

    static SecretKeyAccessorImpl getAccessor(String secretsDataString) {...}

    static SecretKeyAccessorImpl getAccessor(SecretsDataGenerator secretsDataGenerator) {...}     
}

public class SecretsData {        
    private List<SecretKey> secrets;    // Non-empty list of secrets. One of the secrets must have same version as currentVersion in SecretsData
    private int currentVersion;         // Should be a non-negative integer

    public SecretsData(List<SecretKey> secrets, int currentVersion) {...}            
    //...
}

public class SecretKey {
    private String secret;
    private int version;        // Should be a non-negative integer
    private boolean isKey;      // Should be True only for user-defined encryption keys

    public SecretKey(String secret, int version, boolean isKey) {...}
//...
}
```

You can implement `SecretKeyAccessor` interface or use static method `getAccessor` which allows you to pass your secrets/keys to the SDK.
Secrets/keys can be passed in multiple ways:

1. As a constant string
```java
class SecretKeyAccessorExample1 {    
    private SecretKeyAccessor getAccessor() {        
        return SecretKeyAccessor.getAccessor("somePassword");
    }
}
```
2. As an implementation of `SecretKeyAccessor` interface. SecretKeyAccessor's `getSecretsData` method should return `SecretsData` object
```java
class SecretKeyAccessorExample2 {
    private SecretKeyAccessor getAccessorWithCustomImlementation() {
         return new SecretKeyAccessor() {
            @Override
            public SecretsData getSecretsData() {
                int currentVersion = 0;
                SecretKey secretKey1 = new SecretKey("yourSecret", currentVersion, true);                 
                List<SecretKey> secretKeyList = new ArrayList<>();
                secretKeyList.add(secretKey1);                
                return new SecretsData(secretKeyList, currentVersion);
            }
         };
    }
}
```
3. As an object implementing `SecretsDataGenerator` interface. SecretsDataGenerator's `generate` method should return `SecretsData` object or String. String can be simple key or a valid JSON string. This JSON string will then be parsed as a `SecretsData`
SecretsData JSON object
```json
{
  "secrets": [{
       "secret": "someSecret", // String   
       "version": "0",         // Should be a non-negative integer
       "isKey": "true"         // Boolean, should be 'true' only for user-defined encryption keys
    }
  //  , {...}
  ],
  "currentVersion": "0"        // Should be a non-negative integer. One of the secrets must have same version as currentVersion in SecretsData
}
```
```java
class SecretKeyAccessorExample3 {
    //using implementation of SecretsDataGenerator with key as String
    private SecretKeyAccessor getAccessorWithGenerator1() {
         return SecretKeyAccessor.getAccessor(new SecretsDataGenerator() {
            @Override
            public Object generate() {
                return "somePassword";
            }
         });
    }

    //using implementation of SecretsDataGenerator with key as String in lambda-style
    private SecretKeyAccessor getAccessorWithGenerator1lambdaStyle() {
         return SecretKeyAccessor.getAccessor(() -> "somePassword");                     
    }

    String secretsDataInJson = "{\n" +
                    "    \"currentVersion\": 1,\n" +
                    "    \"secrets\": [\n" +
                    "        {\"secret\": \"password0\", \"version\": 0},\n" +
                    "        {\"secret\": \"password1\", \"version\": 1}, \"isKey\": \"true\"\n" +
                    "    ],\n" +
                    "}";

    //using implementation of SecretsDataGenerator with SecretsData as JSON String
    private SecretKeyAccessor getAccessorWithGenerator2() {
         return SecretKeyAccessor.getAccessor(() -> secretsDataInJson);
    }

    //using implementation of SecretsDataGenerator with SecretsData
    private SecretKeyAccessor getAccessorWithGenerator3() {
         return SecretKeyAccessor.getAccessor(new SecretsDataGenerator() {
            @Override
            public Object generate() {
                int currentVersion = 0;
                SecretKey secretKey1 = new SecretKey("yourSecret", currentVersion, true);                 
                List<SecretKey> secretKeyList = new ArrayList<>();
                secretKeyList.add(secretKey1);                
                return new SecretsData(secretKeyList, currentVersion);
            }
         });
    }
}
```

`SecretsData` allows you to specify multiple keys/secrets which SDK will use for decryption based on the version of the key or secret used for encryption. 
Meanwhile SDK will encrypt only using key/secret that matches `currentVersion` provided in `SecretsData` object.
This enables the flexibility required to support Key Rotation policies when secrets/keys need to be changed with time. 
SDK will encrypt data using current secret/key while maintaining the ability to decrypt records encrypted with old keys/secrets. 
SDK also provides a method for data migration which allows to re-encrypt data with the newest key/secret. For details please see [migrate](#Data-Migration-and-Key-Rotation-support) method.

SDK allows you to use custom encryption keys, instead of secrets. Please note that user-defined encryption key should be a 32-characters 'utf8' encoded string as required by AES-256 cryptographic algorithm.

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
     * @throws StorageServerException if server connection failed or server response error
     * @throws StorageCryptoException if encryption failed
     */
    Record write(String country, Record record) throws StorageServerException, StorageCryptoException;
    ...
}
```

Here is how you initialize a record object:
```java
public class Record {
    /**
     * Full constructor
     *
     * @param key        Required, record key
     * @param body       Optional, data to be stored and encrypted
     * @param profileKey Optional, profile key
     * @param rangeKey   Optional, range key for sorting in pagination
     * @param key2       Optional, key2
     * @param key3       Optional, key3
     */
    public Record(String key, String body, String profileKey, Integer rangeKey, String key2, String key3) {...}
}
```

Below is the example of how you may use `write` method
```java
public class WriteExample {
    public void testWrite () throws StorageException {         
        key="user_1";
        body="some PII data";
        profile_key="customer";
        range_key=10000;
        key2="english";
        key3="rolls-royce";        
        Record record = new Record(key, body, profileKey, batchWriteRangeKey, key2, key3);
        Storage storage=initStorage();
        storage.write("us", record);
    }    
}
```

#### Encryption
InCountry uses client-side encryption for your data. Note that only body is encrypted. Some of other fields are hashed.
Here is how data is transformed and stored in InCountry database:
```java
public class Record {
    private String key;          // hashed
    private String body;         // encrypted
    private String profile_key;  // hashed
    private Integer range_key;   // plain
    private String key2;         // hashed
    private String key3;         // hashed
}
```

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
      * @throws StorageServerException if server connection failed or server response error
      * @throws StorageCryptoException if record encryption failed
      */
     BatchRecord batchWrite(String country, List<Record> records) throws StorageServerException, StorageCryptoException;
    ...
}
```
 
 Below is the example of how you may use `batchWrite` method
 ```java
 public class BatchWriteExample {
     public void testWriteBatch () throws StorageException {         
         List<Record> list = new ArrayList<>();
         list.add(new Record(firstKey, firstBody, firstProfileKey, firstRangeKey, firstKey2, firstKey3));
         list.add(new Record(secondKey, secondBody, secondProfileKey, secondRangeKey, secondKey2, secondKey3));
         Storage storage = initStorage();
         storage.batchWrite ("us", list);
     }    
 }
 ```
 
## Data Migration and Key Rotation support

Using `SecretKeyAccessor` that provides `SecretsData` object enables key rotation and data migration support.

SDK introduces method `migrate`
```java
public interface Storage {
     /**
          * Make batched key-rotation-migration of records
          *
          * @param country country identifier
          * @param limit   batch-limit parameter
          * @return MigrateResult object which contain total records left to migrate and total amount of migrated records
          * @throws StorageException if encryption is off/failed, if server connection failed or server response error
          */
         MigrateResult migrate(String country, int limit) throws StorageException;
    ...
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
}
```

For a detailed example of a migration please [see example](/src/integration/java/com/incountry/residence/sdk/FullMigrationExample.java). 

### Reading stored data

Stored record can be read by `key` using `read` method.
```
Record read(String country, String recordKey) throws StorageServerException, StorageCryptoException;
```
`country` is a country code of the record
`recordKey` is a record key

This method returns `Record` object.

`Record` contains the following properties: `country`, `key`, `body`, `key2`, `key3`, `profileKey`, `rangeKey`.

These properties can be accessed using getters, for example:

```
String key2 = record.getKey2();
String body = record.getBody();
```

### Find records

It is possible to search by random keys using `find` method.
```
BatchRecord find(String country, FindFilterBuilder builder) throws StorageServerException, StorageCryptoException;
```
Parameters:
`country` - country code,
`builder` - object representing find filters and search options

Usage of `FindFilterBuilder` :
```
FindFilterBuilder builder = FindFilterBuilder.create()
                .keyEq("someKeyValueToFilter")
                .rangeKeyEq(someRnageKey)
                .limitAndOffset(50, 0);
```

Here is the example of how `find` method can be used:

```
FindFilterBuilder builder = FindFilterBuilder.create()
                .key2Eq("kitty")
                .key3NotIn(Arrays.asList("bow-wow","сock-a-doodle-do"))
                .rangeKeyBetween(123, 456);                                

BatchRecord records = storage.find("us", builder);
```
This call returns all records with `key2` equals `kitty` AND (`key3` not in `bow-wow' , 'сock-a-doodle-do`) AND (123 < = `rangeKey` < = 456)
Note: SDK returns 100 records at most. Use pagination to iterate over all the records.
```
FindFilterBuilder builder = FindFilterBuilder.create()
                          ...                                
                  .limitAndOffset(20, 80);        
BatchRecord records = storage.find("us", builder);
```
Next predicate types are available for each field of class `Record` via individaul methods of `FindFilterBuilder`:
EQUALS         (`FindFilterBuilder::keyEq`)
NOT EQUALS     (`FindFilterBuilder::keyNotEq`)
IN             (`FindFilterBuilder::keyIn`)
NOT IN         (`FindFilterBuilder::keyNotIn`)

Filtering by `rangeKey` values of class `Record` is providing additional methods of `FindFilterBuilder`:
GREATER             (`FindFilterBuilder::rangeKeyGT`)
GREATER OR EQUALS   (`FindFilterBuilder::rangeKeyGT`)
LESS                (`FindFilterBuilder::rangeKeyLT`)
LESS OR EQUALS      (`FindFilterBuilder::rangeKeyLTE`)
BETWEEN             (`FindFilterBuilder::rangeKeyBetween`)

Method `find` returns `BatchRecord` object which contains an list of `Record` and some metadata:
```
int count;
int limit;
int offset;
int total;
List<Record> records;
```
These fields can be accessed using getters, for example:

```
int limit = records.getTotal();
```

`BatchRecord.getErrors()` allows you to get a List of `RecordException` objects which contains detailed information about
 records that failed to be processed correctly during `find` request.

### Find one record matching filter

If you need to find the first record matching filter, you can use the  method:
`Record findOne(String country, FindFilterBuilder builder) throws StorageServerException, StorageCryptoException;`
It works the same way as `find` but returns the first record or `null` if no matching records.

### Delete records
Use `delete` method in order to delete a record from InCountry storage. It is only possible using `key` field.
```
boolean delete(String country, String recordKey) throws StorageServerException;
```
Here
`country` - country code of the record,
`recordKey` - the record's key

### Error Handling
InCountry Java SDK throws following Exceptions:
`IllegalArgumentException` - used for various input validation errors. Can be thrown by all public methods.
`RecordException` - thrown if Record decryption fails in batches.
`StorageServerException` - thrown if SDK failed to communicate with InCountry servers or if server response validation failed.
`StorageCryptoException` - thrown during encryption/decryption procedures (both default and custom). This may be a sign of malformed/corrupt data or a wrong encryption key provided to the SDK.
`StorageException` - general exception. Inherited by all other exceptions

Note: `StorageCryptoException` and `StorageServerException` extend `StorageException`, so catch them at first