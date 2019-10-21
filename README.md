# Introduction

This is the java SDK for the InCountry storage network. Sign up for a free account at
https://incountry.com, then note down your Environment ID and API key.

Important notes
---------------
We've changed the encryption algorithm since version `0.3.0` so it is not compatible with earlier versions.


## Installation

To install the API client library to your local Maven repository, simply execute:

```shell
mvn install
```

To deploy it to a remote Maven repository instead, configure the settings of the repository and execute:

```shell
mvn deploy
```

Refer to the [official documentation](https://maven.apache.org/plugins/maven-deploy-plugin/usage.html) for more information.

### Maven users

Add this dependency to your project's POM:

```xml
<dependency>
    <groupId>com.incountry</groupId>
    <artifactId>incountry-java-client</artifactId>
    <version>0.2.6</version>
    <scope>compile</scope>
</dependency>
```

### Gradle users

Add this dependency to your project's build file:

```groovy
compile "com.incountry:incountry-java-client:0.2.5"
```

### Others

At first generate the JAR by executing:

    mvn package

and now use the SDK:

    import com.incountry.Storage;
    import com.incountry.Data;

    ...

    String environment_id = "bd0c665d-ce0b-4f2d-b1dc-7500c9402919";
    String api_key = "key.smnklp.b3167b35c4e24f21939ccdc58f1812f2";
    String secret_key = "SUPERSECRET";

    String country = "US";

    Storage store = new Storage(environment_id, api_key, secret_key);
    store.write(country, "some_row_key", "Some data", null, null, null, null);
    Data d = store.read(country, "some_row_key");
    System.out.println(d);
    store.delete(country, "some_row_key");

Instead of passing parameters, you can configure the client in your environment:

    export INC_ENVIRONMENT_ID=<environment id>
    export INC_API_KEY=<api key>
    export INC_SECRET_KEY=<secret key>
