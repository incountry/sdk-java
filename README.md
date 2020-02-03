InCountry Storage SDK
============

Important notes
---------------
We've changed the encryption algorithm since version `0.5.0` so it is not compatible with earlier versions.

Usage
-----
To access your data in InCountry using Java SDK, you need to create an instance of `Storage` class.
```
Storage(
    String environmentID, 
    String apiKey, 
    String endpoint, 
    boolean encrypt, 
    SecretKeyAccessor secretKeyAccessor
)
```

`apiKey` and `environmentID` can be fetched from your dashboard on `Incountry` site.

`endpoint` defines API URL and is used to override default one.

You can turn off encryption (not recommended). Set `encrypt` parameter to `false` if you want to do this. 

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

2. As an object implementing `SecretKeyGenerator` interface. SecretKeyGenerator's `generate` method should return `SecretKeysData` object or a valid JSON string, representing the following schema (or secretsData object as we call it) (this JSON string will then be parsed as a `SecretKeysData`)

```
/* secretsData JSON object */
{
  "secrets": [{
       "secret": <string>,
       "version": <int>,   // Should be a positive integer
       "isKey": <boolean> // Should be True only for user-defined encryption keys
    }
  }, ....],
  "currentVersion": <int>,
} 

...


SecretKeyAccessor secretKeyAccessor = SecretKeyAccessor.getAccessor(new SecretKeyGenerator <String>() {
    @Override
    public String generate() {
        String secretsDataJsonString = dataSource.methodReturningJsonString();
        return secretsDataJsonString;
    }
});
        
...

SecretKeyAccessor secretKeyAccessor = SecretKeyAccessor.getAccessor(new SecretKeyGenerator <SecretKeysData>() {
    @Override
    public SecretKeysData generate() {
        SecretKey secretKey = new SecretKey();
        secretKey.setSecret("your_secret_goes_here");
        secretKey.setVersion(0);
        secretKey.setIsKey(true);

        List<SecretKey> secretKeyList = new ArrayList<>();
        secretKeyList.add(secretKey);

        SecretKeysData secretKeysData = new SecretKeysData();
        secretKeysData.setSecrets(secretKeyList);
        secretKeysData.setCurrentVersion(0);
        return secretKeysData;
    }
}
        
```

Both JSON string and `SecretKeysData` allow you to specify multiple keys/secrets which SDK will use for decryption based on the version of the key or secret used for encryption.
Meanwhile SDK will encrypt only using key/secret that matches currentVersion provided in JSON or `SecretKeysData`.

This enables the flexibility required to support Key Rotation policies when secrets/keys need to be changed with time.
SDK will encrypt data using current secret/key while maintaining the ability to decrypt records encrypted with old keys/secrets.
SDK also provides a method for data migration which allows to re-encrypt data with the newest key/secret.
For details please see migrate method.

SDK allows you to use custom encryption keys, instead of secrets. To do so, use `isKey` param in secretsData JSON object or in SecretKey object which is a part of `SecretKeysData`.
Please note that user-defined encryption key should be a 32-characters 'utf8' encoded string as required by AES-256 cryptographic algorithm.

Note: even though SDK uses PBKDF2 to generate a cryptographically strong encryption key, you must make sure you provide a secret/password which follows modern security best practices and standards.

### Writing data to Storage

Use `write` method in order to create a record.
```
public void write(Record record) throws StorageException, GeneralSecurityException, IOException
```

Here is how you initialize a record object:

```
public Record(
  String country,           // Required country code of where to store the data
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
public boolean batchWrite(String country, List<Record> records) throws StorageException, GeneralSecurityException, IOException

// `batchWrite` returns True on success
```

## Data Migration and Key Rotation support

Using `SecretKeyAccessor` that provides `SecretKeysData` object enables key rotation and data migration support.

SDK introduces `public MigrateResult migrate(String country, int limit) throws StorageException, FindOptionsException, GeneralSecurityException, IOException` 
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

For a detailed example of a migration please see `/examples/java/com/incountry/FullMigration`

#### Encryption
InCountry uses client-side encryption for your data. Note that only body is encrypted. Some of other fields are hashed.
Here is how data is transformed and stored in InCountry database:

```
{
	key, 		// hashed
	body, 		// encrypted
	profile_key,// hashed
	range_key, 	// plain
	key2, 		// hashed
	key3 		// hashed
 }
```
### Reading stored data

Stored record can be read by `key` using `read` method.
```
public Record read(String country, String key) throws StorageException, IOException, GeneralSecurityException
```
`country` is a country code of the record
`key` is a record key

This method returns Record object. It contains the following properties: `country`, `key`, `body`, `key2`, `key3`, `profileKey`, `rangeKey`.

These properties can be accessed using getters, for example:

```
String key2 = record.getKey2();
String body = record.getBody();
```

### Find records

It is possible to search by random keys using `find` method.
```
public BatchRecord find(String country, FindFilter filter, FindOptions options) throws StorageException, IOException, GeneralSecurityException
```
Parameters:  
`country` - country code,  
`filter` - a filter object (see below),  
`options` - an object containing search options.

`FindFilter` has the following constructor:
```
public FindFilter(FilterStringParam key, FilterStringParam profileKey, FilterRangeParam rangeKey, FilterStringParam key2, FilterStringParam key3)
```
And for `FindOptions`:
```
public FindOptions(int limit, int offset) throws FindOptionsException
```

There are two different types of filter params: `FilterStringParam` and `FilterRangeParam`.
`FilterStringParam` is used for all the keys except `rangeKey`:

```
public FilterStringParam(List<String> value);
```
or
```
public FilterStringParam(String value);
```

Here is the example of how `find` method can be used:

```
FindFilter filter = new FindFilter(
    null,
    null,
    null, 
    new FilterStringParam("kitty"),
    new FilterStringParam(List<String> value)
);

FindOptions options = new FindOptions(10, 10);

BatchRecord records = storage.find("us", filter, options);
```
This call returns all records with `key2` equals `kitty` AND `key3` equals `mew` OR `purr`.  
Note: SDK returns 100 records at most. Use pagination to iterate over all the records.  

`Find` returns BatchRecord object which contains an array of `Record` and some metadata:

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

`FilterRangeParam` works differently from `FilterStringParam`. `rangeKey` is an integer non-encrypted field so you can perform range operations on it.  
For example you can request all the records with `rangeKey` less than 1000:

```

FilterRangeParam rangeParam = new FilterRangeParam("$lt", 1000);

```
or if you want just to check equality:

```


FilterRangeParam rangeParam = new FilterRangeParam(1000);

```
Available request options for `FilterRangeParam`: `$lt`, `$lte`, `$gt`, `$gte`.

### Find one record matching filter

If you need to find the first record matching filter, you can use the `findOne` method.
It works the same way as `find` but returns the first record or `null` if no matching records.

### Delete records
Use `delete` method in order to delete a record from InCountry storage. It is only possible using `key` field.
```
public void delete(String country, String key) throws StorageException, IOException
```
Here
`country` - country code of the record,
`key` - the record's key
