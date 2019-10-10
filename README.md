InCountry Storage SDK
============

Usage
-----

1) Create Storage instance

    1.1. Import Storage package
    ```
    import com.incountry.Storage;
    ```
    1.2. Set environment variables (optional)
    ```java
    export INC_ENVIRONMENT_ID=<environment id>
    export INC_API_KEY=<api key>
    export INC_SECRET_KEY=<secret key>- Create an instance
    ```
    1.3. Create an instance
    If environment variables are set:
    ```java
    Storage store = new Storage();
    ```
    If not, pass values to the constructor:
    ```java
    Storage store = new Storage(environment_id, api_key, secret_key);
    ```
2) Writes
    Write method has the following signature:
    ```
    void write(String country, String key, String body, String profile_key, String range_key, String key2, String key3)
    ```
    All the parameters except country and key can be null. For example,
    ```
    import com.incountry.Data;
    ...
    String country = "US";
    store.write(country, "some_row_key", "Some data", null, null, null, null);
    ```
3) Reads
    Read method has the following signature:
    ```
    Data read(String country, String key)
    ```
    Parameters `country` and `key` are mandatory. For example:
    ```
    import com.incountry.Data;
    ...
    Data d = store.read("US", "some_row_key");
    ```
4) Deletes
    The record can be deleted using the following syntax:
    ```
    store.delete("US", "some_row_key");
    ```
