# Introduction

This is the java SDK for the InCountry storage network. Sign up for a free account at
https://incountry.com, then note down your Zone ID and API key.

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
    <version>0.2.0</version>
    <scope>compile</scope>
</dependency>
```

### Gradle users

Add this dependency to your project's build file:

```groovy
compile "com.incountry:incountry-java-client:0.2.0"
```

### Others

At first generate the JAR by executing:

    mvn package

Setup your environment:

    export INC_ENDPOINT=<api endpoint>
    export INC_ZONE_ID=<zone id>
    export INC_API_KEY=<api key>
   	export INC_SECRET_KEY=<generate uuid>

and now use the SDK:

    > import com.incountry.Storage;

    > Storage global_client = Storage()
    > # FIXME - replace snippet
    > global_client.write(country='jp', key='key1', body="Store this data in Japan")

	> r = global_client.read(country='jp', key='key1')
	> print(r)
	{'body': 'Store this data in Japan', 'key': 'key1', 'key2': None, 'key3': None, 'profile_key': None, 'range_key': None, 'version': 1, 'zone_id': 645}
