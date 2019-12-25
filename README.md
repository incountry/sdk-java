InCountry Storage SDK
============

Installation
-----
For Maven users please add this section to your dependencies list
```
<dependency>
  <groupId>com.incountry</groupId>
  <artifactId>incountry-java-client</artifactId>
  <version>1.0.0a</version>
</dependency>
```
For Gradle users plase add this line to your dependencies list
```
compile "com.incountry:incountry-java-client:1.0.0a"
```

Usage
-----
To access your data in InCountry using Java SDK, you need to create an instance of `Storage` class.
```java
Storage(
  String environmentID,
  String apiKey,
  String endpoint,
  boolean encrypt,
  ISecretKeyAccessor secretKeyAccessor
)
```

`apiKey` and `environmentID` can be fetched from your dashboard on `Incountry` site.

`endpoint` defines API URL and is used to override default one.

You can turn off encryption (not recommended). Set `encrypt` parameter to `false` if you want to do this.

#### Encryption key

`secretKeyAccessor` is used to pass a secret used for encryption.

Note: even though PBKDF2 is used internally to generate a cryptographically strong encryption key, you must make sure that you use strong enough password.

Here are some examples how you can use `SecretKeyAccessor`.
```java
public class SimpleSecretKeyAccessor implements ISecretKeyAccessor {
    private String secret;

    public SecretKeyAccessor(String secret) {
        this.secret = secret;
    }

    @Override
    public String getKey() {
        return this.secret;
    }
}

SimpleSecretKeyAccessor accessor = new SimpleSecretKeyAccessor("myStrongPassword");
```


### Writing data to Storage

Use `write` method in order to create/overwrite a record for a given `key`.
```java
public void write(Record record) throws StorageException, GeneralSecurityException, IOException
```
Here is how you initialize a record object:
```java
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

#### Encryption
InCountry uses client-side encryption for your data. Note that only body is encrypted. Some of other fields are hashed.
Here is how data is transformed and stored in InCountry database:
```java
{
  key,          // hashed
  body,         // encrypted
  profile_key,  // hashed
  range_key,    // plain
  key2,         // hashed
  key3          // hashed
}
```
### Reading stored data

Stored record can be read by `key` using `read` method.
```java
public Record read(String country, String key) throws StorageException, IOException, GeneralSecurityException
```
`country` is a country code of the record
`key` is a record key

This method returns Record object. It contains the following properties: `country`, `key`, `body`, `key2`, `key3`, `profileKey`, `rangeKey`.

These properties can be accessed using getters, for example:
```java
String key2 = record.getKey2();
String body = record.getBody();
```

### Find records

It is possible to search by random keys using `find` method.
```java
public BatchRecord find(String country, FindFilter filter, FindOptions options) throws StorageException, IOException, GeneralSecurityException
```
Parameters:
`country` - country code,
`filter` - a filter object (see below),
`options` - an object containing search options.

`FindFilter` has the following constructor:
```java
public FindFilter(FilterStringParam key, FilterStringParam profileKey, FilterRangeParam rangeKey, FilterStringParam key2, FilterStringParam key3)
```
And for `FindOptions`:
```java
public FindOptions(int limit, int offset) throws FindOptionsException
```

There are two different types of filter params: `FilterStringParam` and `FilterRangeParam`.
`FilterStringParam` is used for all the keys except `rangeKey`:
```java
public FilterStringParam(String[] value);
```
or
```java
public FilterStringParam(String value);
```

Below is the example of how `find` method can be used:
```java
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
```java
int count;
int limit;
int offset;
int total;
Record[] records;
```
These fields can be accessed using getters, for example:
```java
int limit = records.getTotal();
```

`FilterRangeParam` works differently from `FilterStringParam`. `rangeKey` is an integer non-encrypted field so you can perform range operations on it.
For example you can request all the records with `rangeKey` less than 1000:
```java
FilterRangeParam rangeParam = new FilterRangeParam("$lt", 1000);
```
or if you want just to check equality:
```java
FilterRangeParam rangeParam = new FilterRangeParam(1000);
```
Available request options for `FilterRangeParam`: `$lt`, `$lte`, `$gt`, `$gte`.

### Find one record matching filter

If you need to find the first record matching filter, you can use the `findOne` method.
It works the same way as `find` but returns the first record or `null` if no matching records.

### Delete records
Use `delete` method in order to delete a record from InCountry storage. It is only possible using `key` field.
```java
public void delete(String country, String key) throws StorageException, IOException
```
Where
`country` - country code of the record,
`key` - the record's key
