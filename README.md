# swagger-java-client

## Requirements

Building the API client library requires [Maven](https://maven.apache.org/) to be installed.

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
    <groupId>io.swagger</groupId>
    <artifactId>swagger-java-client</artifactId>
    <version>1.0.0</version>
    <scope>compile</scope>
</dependency>
```

### Gradle users

Add this dependency to your project's build file:

```groovy
compile "io.swagger:swagger-java-client:1.0.0"
```

### Others

At first generate the JAR by executing:

    mvn package

Then manually install the following JARs:

* target/swagger-java-client-1.0.0.jar
* target/lib/*.jar

## Getting Started

You will need an InCountry API Key and a unique seed for client-side encryption. Log in to https://portal.incountry.com to look up or reset your API key. The cryptography seed can be any unique value you choose, and will be used to encrypt your data prior to sending it to InCountry for storage. <b>Do not lose the cryptography seed" as InCountry <b>CANNOT</b> decrypt your data. Please follow the [installation](#installation) instruction and execute the following Java code:

```java
import com.incountry.InCountry;

public class Main 
{
	public static void main(String[] args) 
	{
		// Log in to https://portal.incountry.com to look up or reset your API key
		String APIKEY = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
		
		// Choose a unique seed value for client-side encryption 
		String CRYPTOSEED = "supersecret";

		try
		{
			InCountry api = new InCountry(APIKEY, CRYPTOSEED);
			api.write("US", "row0001", "blobbymcblobface", "foo", "bar", null, null, null);
			api.write("US", "row0002", "I am the very model of a modern major general", null, "foo", "bar", null, null);
			api.write("US", "row0003", "We hold these truths to be self-evident", "bar", "foo", null, null, null);
			System.out.println(api.read("US", "row0001"));
			System.out.println(api.lookup("US", null, "foo", null, null, null));
			System.out.println(api.keyLookup("US", "foo", null, null, null, null));
			api.delete("US", "row0001");
			api.delete("US", "row0002");
			api.delete("US", "row0003");
		}
		catch (Exception x) { x.printStackTrace(); }
	}
}
```

## Documentation for API Endpoints

All URIs are relative to *https://87lh3zngr4.execute-api.us-east-1.amazonaws.com/prod*

Class | Method | HTTP request | Description
------------ | ------------- | ------------- | -------------
*DefaultApi* | [**deletePost**](docs/DefaultApi.md#deletePost) | **POST** /delete | 
*DefaultApi* | [**keylookupPost**](docs/DefaultApi.md#keylookupPost) | **POST** /keylookup | 
*DefaultApi* | [**lookupPost**](docs/DefaultApi.md#lookupPost) | **POST** /lookup | 
*DefaultApi* | [**readPost**](docs/DefaultApi.md#readPost) | **POST** /read | 
*DefaultApi* | [**writePost**](docs/DefaultApi.md#writePost) | **POST** /write | 

## Documentation for Models

 - [Data](docs/Data.md)

## Documentation for Authorization

Authentication schemes defined for the API:
### api_key

- **Type**: API key
- **API key parameter name**: x-api-key
- **Location**: HTTP header


## Recommendation

It's recommended to create an instance of `ApiClient` per thread in a multithreaded environment to avoid any potential issues.

## Author


