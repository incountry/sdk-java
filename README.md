InCountry Storage SDK
============

Installation
-----
Incountry Storage SDK requires Java Developer Kit 1.8 or higher, recommended language level 8.

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
To access your data in InCountry using Java SDK, you need to create an implementation of `Storage` interface. 
Use `StorageImpl` for it 
```java
public class StorageImpl implements Storage {        
  /**
   * creating Storage instance
   *
   * @param environmentID     Required to be passed in, or as environment variable INC_API_KEY
   * @param apiKey            Required to be passed in, or as environment variable INC_ENVIRONMENT_ID
   * @param endpoint          Optional. Defines API URL. 
   *                          Default endpoint will be used if this param is null
   * @param secretKeyAccessor Instance of SecretKeyAccessor class. Used to fetch encryption secret
   * @return instance of Storage
   * @throws StorageClientException if configuration validation finished with errors
   * @throws StorageServerException if server connection failed or server response error
   */
  public static Storage getInstance(String environmentID, String apiKey, String endpoint,
                            SecretKeyAccessor secretKeyAccessor) throws StorageServerException {...} 
//...
}
```

Parameters `apiKey` and `environmentID` can be fetched from your dashboard on `Incountry` site.

`endpoint` defines API URL and is used to override default one.

You can turn off encryption (not recommended) by providing `null` value for parameter `secretKeyAccessor`.

Below is an example how to create a storage instance:
```java      
SecretKeyAccessor secretKeyAccessor = SecretKeyAccessor.getAccessor("somePassword");
String endPoint = "https://us.api.incountry.io";        
String envId = "someEnvironmentId";
String apiKey = "someApiKey";
Storage storage = StorageImpl.getInstance(envId, apiKey, endPoint, secretKeyAccessor);        
```

### Encryption key/secret

The SDK has a `SecretKeyAccessor` interface which allows you to pass your own secrets/keys to the SDK.

`SecretKeyAccessor` allows you to pass a function that should return representing your secret in `SecretsData` class instance:
```java
public interface SecretKeyAccessor {    
    SecretsData getSecretsData();

    /**
     * create SecretKeyAccessor from String
     *
     * @param secretsDataString simple password or SecretsData in JSON
     * @return SecretKeyAccessor
     * @throws StorageClientException when parameter validation fails
     */
    static SecretKeyAccessorImpl getAccessor(String secretsDataString) 
                          throws StorageClientException {...}

    /**
     * create SecretKeyAccessor with SecretsDataGenerator
     *
     * @param secretsDataGenerator non-empty generator of SecretsData
     * @return SecretKeyAccessor
     * @throws StorageClientException when parameter validation fails
     */
    static SecretKeyAccessorImpl getAccessor(SecretsDataGenerator secretsDataGenerator) 
                          throws StorageClientException {...}     
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
    * @param secret  secret/key
    * @param version secret version, should be a non-negative integer
    * @param isKey   should be True only for user-defined encryption keys
    * @throws StorageClientException when parameter validation fails
    */
    public SecretKey(String secret, int version, boolean isKey) 
              throws StorageClientException {...}
    //...
}
```

You can implement `SecretKeyAccessor` interface or use static method `getAccessor` which allows you to pass your secrets/keys to the SDK.
Secrets/keys can be passed in multiple ways:

1. As a constant string
```java     
SecretKeyAccessor accessor = SecretKeyAccessor.getAccessor("somePassword");
```

2. As an implementation of `SecretKeyAccessor` interface. SecretKeyAccessor's `getSecretsData` method should return `SecretsData` object
```java
int currentVersion = 0;
SecretKey secretKey1 = new SecretKey("yourSecret", currentVersion, true);                 
List<SecretKey> secretKeyList = new ArrayList<>();
secretKeyList.add(secretKey1);                
SecretsData secretsData = new SecretsData(secretKeyList, currentVersion);

SecretKeyAccessor accessor = () -> secretsData;        
```

3. As an object implementing `SecretsDataGenerator` interface. SecretsDataGenerator's `generate` method should return `SecretsData` object or String. String can be simple key or a valid JSON string. This JSON string will then be parsed as a `SecretsData`
SecretsData JSON object
```javascript
{
  "secrets": [{
       "secret": "someSecret",  // String   
       "version": 0,            // Should be a non-negative integer
       "isKey": true            // Boolean, should be 'true' only for user-defined encryption keys
    }
  ],
  "currentVersion": 0           // Should be a non-negative integer. One of the secrets
                                // must have same version as currentVersion in SecretsData
}
```
```java
//using implementation of SecretsDataGenerator with key as String
private SecretKeyAccessor getAccessorWithGenerator1() throws StorageClientException {
    return SecretKeyAccessor.getAccessor(new SecretsDataGenerator() {
        @Override
        public Object generate() {
            return "somePassword";
        }
    });
}

//using implementation of SecretsDataGenerator with key as String in lambda-style
private SecretKeyAccessor getAccessorWithGenerator1lambdaStyle() throws StorageClientException {
    return SecretKeyAccessor.getAccessor(() -> "somePassword");                     
}

private String secretsDataInJson = "{\n" +
                    "    \"currentVersion\": 1,\n" +
                    "    \"secrets\": [\n" +
                    "        {\"secret\": \"password0\", \"version\": 0},\n" +
                    "        {\"secret\": \"password1\", \"version\": 1}, \"isKey\": \"true\"\n" +
                    "    ],\n" +
                    "}";

//using implementation of SecretsDataGenerator with SecretsData as JSON String
private SecretKeyAccessor getAccessorWithGenerator2() throws StorageClientException {
    return SecretKeyAccessor.getAccessor(() -> secretsDataInJson);
}

//using implementation of SecretsDataGenerator with SecretsData
private SecretKeyAccessor getAccessorWithGenerator3() throws StorageClientException {
    int currentVersion = 0;
    SecretKey secretKey1 = new SecretKey("yourSecret", currentVersion, true);
    List<SecretKey> secretKeyList = new ArrayList<>();
    secretKeyList.add(secretKey1);
    SecretsData data = new SecretsData(secretKeyList, currentVersion);

    return SecretKeyAccessor.getAccessor(() -> data);
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
     * Full constructor
     *
     * @param key        Required, record key
     * @param body       Optional, data to be stored and encrypted
     * @param profileKey Optional, profile key
     * @param rangeKey   Optional, range key
     * @param key2       Optional, key2
     * @param key3       Optional, key3
     */
    public Record(String key, String body, String profileKey, Integer rangeKey, String key2, String key3)
    //...
}
```

Below is the example of how you may use `write` method
```java      
key="user_1";
body="some PII data";
profile_key="customer";
range_key=10000;
key2="english";
key3="rolls-royce";        
Record record = new Record(key, body, profileKey, batchWriteRangeKey, key2, key3);
Storage storage=initStorage();
storage.write("us", record);
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
    //...
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
list.add(new Record(firstKey, firstBody, firstProfileKey, firstRangeKey, firstKey2, firstKey3));
list.add(new Record(secondKey, secondBody, secondProfileKey, secondRangeKey, secondKey2, secondKey3));
Storage storage = initStorage();
storage.batchWrite ("us", list);
```
 
### Data Migration and Key Rotation support

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

For a detailed example of a migration please [see example](/src/integration/java/com/incountry/residence/sdk/FullMigrationExample.java). 

### Reading stored data

Stored record can be read by `key` using `read` method.
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

This method returns `Record` object.

`Record` contains the following properties: `country`, `key`, `body`, `key2`, `key3`, `profileKey`, `rangeKey`.

These properties can be accessed using getters, for example:

```java
String key2 = record.getKey2();
String body = record.getBody();
```

Below is the example of how you may use `batchWrite` method
 ```java
String recordKey = "user_1";                 
Storage storage = initStorage();
Record record = storage.read ("us", recordKey);
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
Parameters:
`country` - country code,
`builder` - object representing find filters and search options

Usage of `FindFilterBuilder` :
```java
FindFilterBuilder builder = FindFilterBuilder.create()
                  .keyEq("someKeyValueToFilter")
                  .rangeKeyEq(someRnageKey)
                  .limitAndOffset(50, 0);
```

Here is the example of how `find` method can be used:
```java
FindFilterBuilder builder = FindFilterBuilder.create()
                  .key2Eq("kitty")
                  .key3NotIn(Arrays.asList("firstExcludeValue","secondExclude"))
                  .rangeKeyBetween(123, 456);                                

BatchRecord founded = storage.find("us", builder);
if (founded.getCount>0) {
    Record record = founded.getRecords().get(0);
    //...
}
```

This call returns all records with `key2` equals `kitty` AND (`key3` not in `firstExcludeValue' , 'secondExclude`) AND (123 < = `rangeKey` < = 456)
Filter conditions on each field of `Record` class are union with predicate `AND` 

Note: SDK returns 100 records at most. Use pagination to iterate over all the records.
```java
FindFilterBuilder builder = FindFilterBuilder.create()
                  //...                                
                  .limitAndOffset(20, 80);        
BatchRecord records = storage.find("us", builder);
```

Next predicate types are available for each field of class `Record` via individaul methods of `FindFilterBuilder`:
```java
EQUALS         (FindFilterBuilder::keyE)
NOT EQUALS     (FindFilterBuilder::keyNotE)
IN             (FindFilterBuilder::keyI)
NOT IN         (FindFilterBuilder::keyNotI)
```

Filtering by `rangeKey` values of class `Record` is providing additional methods of `FindFilterBuilder`:
```java
GREATER             (FindFilterBuilder::rangeKeyGT)
GREATER OR EQUALS   (FindFilterBuilder::rangeKeyGT)
LESS                (FindFilterBuilder::rangeKeyLT)
LESS OR EQUALS      (FindFilterBuilder::rangeKeyLTE)
BETWEEN             (FindFilterBuilder::rangeKeyBetween)
```

Method `find` returns `BatchRecord` object which contains an list of `Record` and some metadata:
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

`BatchRecord.getErrors()` allows you to get a List of `RecordException` objects which contains detailed information about
 records that failed to be processed correctly during `find` request.

### Find one record matching filter

If you need to find the first record matching filter, you can use the  method:
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

It works the same way as `find` but returns the first record or `null` if no matching records.
Here is the example of how `find` method can be used:
```java
FindFilterBuilder builder = FindFilterBuilder.create()
                .key2Eq("kitty")
                .key3NotIn(Arrays.asList("bow-wow", "сock-a-doodle-do"))
                .rangeKeyBetween(123, 456);                                

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
    * @return TRUE when record was deleted
    * @throws StorageClientException if validation finished with errors
    * @throws StorageServerException if server connection failed
    */
    boolean delete(String country, String recordKey) 
            throws StorageClientException, StorageServerException;
    //...
}
```
Here
`country` - country code of the record,
`recordKey` - the record's key

Below is the example of how you may use `delete` method
 ```java
String recordKey = "user_1";
Storage storage = initStorage();
storage.delete ("us", recordKey);
 ```

### Error Handling
InCountry Java SDK throws following Exceptions:
`StorageClientException` - used for various input validation errors
`StorageServerException` - thrown if SDK failed to communicate with InCountry servers or if server response validation failed.
`StorageCryptoException` - thrown during encryption/decryption procedures (both default and custom). This may be a sign of malformed/corrupt data or a wrong encryption key provided to the SDK.
`StorageException` - general exception. Inherited by all other exceptions

We suggest gracefully handling all the possible exceptions:

```java
public void test () {
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