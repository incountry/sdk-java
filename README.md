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
  ISecretKeyAccessor secretKeyAccessorImpl
)
```

`apiKey` and `environmentID` can be fetched from your dashboard on `Incountry` site.

`endpoint` defines API URL and is used to override default one.

You can turn off encryption (not recommended). Set `encrypt` parameter to `false` if you want to do this.

#### Encryption key

`SecretKeyAccessor` is used to pass a secret used for encryption.

To get secretKeyAccessor object you must use `secretKeyAccessor` interface `getAccessor` static method.

`getAccessor` method takes as argument string password or object which implements `SecretKeyGenerator` interface.

Note: even though PBKDF2 is used internally to generate a cryptographically strong encryption key, you must make sure that you use strong enough password.

Using the `SecretKeyGenerator` interface, you can pass a list of keys with their versions.

To do it, the generate method of `SecretKeyGenerator` interface should return json of the form

```
{
  "secrets": [
    {
      "secret": "123",
      "version": 0
    }
  ],
  "currentVersion": 0
}
```

or a `SecretKeysData` object containing currentVersion and a list of `SecretKey` objects each of which contains a String secret and its version

```
public class SecretKeysData {
    private List<SecretKey> secrets;
    private int currentVersion;
}

public class SecretKey {
    private String secret;
    private int version;
}
```

Note: the `generate` method should return either valid json as a string or a `SecretKeysData` object.


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

Use the `batchWrite` method to write multiple records to the storage at once.

public boolean batchWrite(String country, List<Record> records) throws StorageException, GeneralSecurityException, IOException

#### Keys migration

Use `migrate` method for batched key-rotation-migration of records

public MigrateResult migrate(String country, int limit) throws StorageException, FindOptionsException, GeneralSecurityException, IOException

`limit` is the batch-limit parameter for handling the batch size 

method returns `MigrateResult` object

```
public class MigrateResult {
    private int migrated;
    private int totalLeft;
}
```

`migrated` is total records left to migrate

`totalLeft` is total amount of migrated records

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
public FilterStringParam(String[] value);
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
    new FilterStringParam(new String[]{"mew", "pur"})
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
    Record[] records;
```
These fields can be accessed using getters, for example:
```
int limit = records.getTotal();
```

`FilterRangeParam` works differently from `FilterStringParam`. `rangeKey` is an integer non-encrypted field so you can perform range operations on it.  
For example you can request all the records with `rangeKey` less than 1000:
```
{
    FilterRangeParam rangeParam = new FilterRangeParam("$lt", 1000);
}
```
or if you want just to check equality:
```
{
    FilterRangeParam rangeParam = new FilterRangeParam(1000);
}
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
