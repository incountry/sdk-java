InCountry Storage SDK
============

Installation
-----
For Maven users please add this section to your dependencies list
```
<dependency>
  <groupId>com.incountry</groupId>
  <artifactId>incountry-java-client</artifactId>
  <version>2.0.0</version>
</dependency>
```
For Gradle users plase add this line to your dependencies list
```
compile "com.incountry:incountry-java-client:2.0.0"
```

Countries List
----
For a full list of supported countries and their codes please [follow this link](countries.md).


Usage
-----
To access your data in InCountry using Java SDK, you need to create an implementation of `Storage` interface.
Use 'StorageImpl' for it 
```
Storage storage=StorageImpl.getInstance(
    String environmentID,                   // Required to be passed in, or as environment variable INC_API_KEY
    String apiKey,                          // Required to be passed in, or as environment variable INC_ENVIRONMENT_ID
    String endpoint,                        // Optional. Defines API URL
    SecretKeyAccessor secretKeyAccessor     // Instance of SecretKeyAccessor class. Used to fetch encryption secret. 
)
```

`apiKey` and `environmentID` can be fetched from your dashboard on `Incountry` site.

`endpoint` defines API URL and is used to override default one.

You can turn off encryption (not recommended). For it use `null` value for parameter `secretKeyAccessor`.

#### Encryption key

The SDK has a `SecretKeyAccessor` interface which allows you to pass your own secrets/keys to the SDK.

`SecretKeyAccessor` introduces `getAccessor` static method which allows you to pass your secrets/keys to the SDK.
Secrets/keys can be passed in multiple ways:

1. As a string

```
private static SecretKeyAccessor initializeSecretKeyAccessorWithString() {
    return SecretKeyAccessor.getAccessor("your_secret_goes_here");
}
```

2. As an object implementing `SecretsDataGenerator` interface. SecretsDataGenerator's `generate` method should return `SecretsData` object or a valid JSON string, representing the following schema (or secretsData object as we call it) (this JSON string will then be parsed as a `SecretsData`)

```
/* secretsData JSON object */
{
  "secrets": [{
       "secret": <string>,   
       "version": <int>,     // Should be a non-negative integer
       "isKey": <boolean>    // Should be True only for user-defined encryption keys
    }
  }, ....],
  "currentVersion": <int>,   // Should be a non-negative integer
}

...


SecretKeyAccessor secretKeyAccessor = SecretKeyAccessor.getAccessor(new SecretsDataGenerator () {
    @Override
    public String generate() {
        String secretsDataJsonString = dataSource.methodReturningJsonString();
        return secretsDataJsonString;
    }
});

...

SecretKeyAccessor secretKeyAccessor = SecretKeyAccessor.getAccessor(new SecretsDataGenerator () {
    @Override
    public SecretsData generate() {
        int version = 0;
        String secret = "your_secret_goes_here";         
        SecretKey secretKey = new SecretKey(secret, version, true);        
        List<SecretKey> secretKeyList = new ArrayList<>();
        secretKeyList.add(secretKey);                        
        return new SecretsData(secretKeyList, version);;
    }
}
```

Both JSON string and `SecretsData` allow you to specify multiple keys/secrets which SDK will use for decryption based on the version of the key or secret used for encryption.
Meanwhile SDK will encrypt only using key/secret that matches currentVersion provided in JSON or `SecretsData`.

This enables the flexibility required to support Key Rotation policies when secrets/keys need to be changed with time.
SDK will encrypt data using current secret/key while maintaining the ability to decrypt records encrypted with old keys/secrets.
SDK also provides a method for data migration which allows to re-encrypt data with the newest key/secret.
For details please see migrate method.

SDK allows you to use custom encryption keys, instead of secrets. To do so, use `isKey` param in secretsData JSON object or in SecretKey object which is a part of `SecretsData`.
Please note that user-defined encryption key should be a 32-characters 'utf8' encoded string as required by AES-256 cryptographic algorithm.

Note: even though SDK uses PBKDF2 to generate a cryptographically strong encryption key, you must make sure you provide a secret/password which follows modern security best practices and standards.

### Writing data to Storage

Use `write` method in order to create a record.
```
Record write(String country, Record record) throws StorageServerException, StorageCryptoException;
```

Here is how you initialize a record object:

```
public Record(
    String key,               // Required record key
    String body,              // Optional payload
    String profileKey,        // Optional
    Integer rangeKey,         // Optional
    String key2,              // Optional
    String key3               // Optional
)
```
#### Batches

Use the `batchWrite` method to write multiple records to the storage in a single request.

```
BatchRecord batchWrite(String country, List<Record> records) throws StorageServerException, StorageCryptoException;

// `batchWrite` returns `BatchRecord` object
```

## Data Migration and Key Rotation support

Using `SecretKeyAccessor` that provides `SecretsData` object enables key rotation and data migration support.

SDK introduces `MigrateResult migrate(String country, int limit) throws StorageException;`
method which allows you to re-encrypt data encrypted with old versions of the secret. You should specify `country` you want to conduct migration in
and `limit` for precise amount of records to migrate. `migrate` returns a `MigrateResult` object which contains some information about the migration - the
amount of records migrated (`migrated`) and the amount of records left to migrate (`totalLeft`) (which basically means the amount of records with
version different from `currentVersion` provided by `SecretKeyAccessor`)

```
public class MigrateResult {
    private int migrated;
    private int totalLeft;
}
```

For a detailed example of a migration please [see example](/src/integration/java/com/incountry/residence/sdk/FullMigrationExample.java). 

#### Encryption
InCountry uses client-side encryption for your data. Note that only body is encrypted. Some of other fields are hashed.
Here is how data is transformed and stored in InCountry database:

```
Record
{
    key,           // hashed
    body,          // encrypted
    profile_key,   // hashed
    range_key,     // plain
    key2,          // hashed
    key3           // hashed
}
```
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

Note: `StorageCryptoException` and `StorageServerException` extends `StorageException`, that's why you need to catch them at first