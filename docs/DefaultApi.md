# DefaultApi

All URIs are relative to *https://87lh3zngr4.execute-api.us-east-1.amazonaws.com/prod*

Method | HTTP request | Description
------------- | ------------- | -------------
[**deletePost**](DefaultApi.md#deletePost) | **POST** /delete | 
[**keylookupPost**](DefaultApi.md#keylookupPost) | **POST** /keylookup | 
[**lookupPost**](DefaultApi.md#lookupPost) | **POST** /lookup | 
[**readPost**](DefaultApi.md#readPost) | **POST** /read | 
[**writePost**](DefaultApi.md#writePost) | **POST** /write | 

<a name="deletePost"></a>
# **deletePost**
> Data deletePost(country, rowid)



### Example
```java
// Import classes:
//import com.incountry.api.ApiClient;
//import com.incountry.api.ApiException;
//import com.incountry.api.Configuration;
//import com.incountry.api.auth.*;
//import com.incountry.api.DefaultApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: api_key
ApiKeyAuth api_key = (ApiKeyAuth) defaultClient.getAuthentication("api_key");
api_key.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//api_key.setApiKeyPrefix("Token");

DefaultApi apiInstance = new DefaultApi();
String country = "country_example"; // String | 
String rowid = "rowid_example"; // String | 
try {
    Data result = apiInstance.deletePost(country, rowid);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#deletePost");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **country** | [**String**](.md)|  |
 **rowid** | [**String**](.md)|  |

### Return type

[**Data**](Data.md)

### Authorization

[api_key](../README.md#api_key)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="keylookupPost"></a>
# **keylookupPost**
> Data keylookupPost(country, key2, key3, key4, key5, key1)



### Example
```java
// Import classes:
//import com.incountry.api.ApiClient;
//import com.incountry.api.ApiException;
//import com.incountry.api.Configuration;
//import com.incountry.api.auth.*;
//import com.incountry.api.DefaultApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: api_key
ApiKeyAuth api_key = (ApiKeyAuth) defaultClient.getAuthentication("api_key");
api_key.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//api_key.setApiKeyPrefix("Token");

DefaultApi apiInstance = new DefaultApi();
String country = "country_example"; // String | 
String key2 = "key2_example"; // String | 
String key3 = "key3_example"; // String | 
String key4 = "key4_example"; // String | 
String key5 = "key5_example"; // String | 
String key1 = "key1_example"; // String | 
try {
    Data result = apiInstance.keylookupPost(country, key2, key3, key4, key5, key1);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#keylookupPost");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **country** | [**String**](.md)|  |
 **key2** | [**String**](.md)|  | [optional]
 **key3** | [**String**](.md)|  | [optional]
 **key4** | [**String**](.md)|  | [optional]
 **key5** | [**String**](.md)|  | [optional]
 **key1** | [**String**](.md)|  | [optional]

### Return type

[**Data**](Data.md)

### Authorization

[api_key](../README.md#api_key)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="lookupPost"></a>
# **lookupPost**
> Data lookupPost(country, key2, key3, key4, key5, key1)



### Example
```java
// Import classes:
//import com.incountry.api.ApiClient;
//import com.incountry.api.ApiException;
//import com.incountry.api.Configuration;
//import com.incountry.api.auth.*;
//import com.incountry.api.DefaultApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: api_key
ApiKeyAuth api_key = (ApiKeyAuth) defaultClient.getAuthentication("api_key");
api_key.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//api_key.setApiKeyPrefix("Token");

DefaultApi apiInstance = new DefaultApi();
String country = "country_example"; // String | 
String key2 = "key2_example"; // String | 
String key3 = "key3_example"; // String | 
String key4 = "key4_example"; // String | 
String key5 = "key5_example"; // String | 
String key1 = "key1_example"; // String | 
try {
    Data result = apiInstance.lookupPost(country, key2, key3, key4, key5, key1);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#lookupPost");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **country** | [**String**](.md)|  |
 **key2** | [**String**](.md)|  | [optional]
 **key3** | [**String**](.md)|  | [optional]
 **key4** | [**String**](.md)|  | [optional]
 **key5** | [**String**](.md)|  | [optional]
 **key1** | [**String**](.md)|  | [optional]

### Return type

[**Data**](Data.md)

### Authorization

[api_key](../README.md#api_key)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="readPost"></a>
# **readPost**
> Data readPost(country, rowid)



### Example
```java
// Import classes:
//import com.incountry.api.ApiClient;
//import com.incountry.api.ApiException;
//import com.incountry.api.Configuration;
//import com.incountry.api.auth.*;
//import com.incountry.api.DefaultApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: api_key
ApiKeyAuth api_key = (ApiKeyAuth) defaultClient.getAuthentication("api_key");
api_key.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//api_key.setApiKeyPrefix("Token");

DefaultApi apiInstance = new DefaultApi();
String country = "country_example"; // String | 
String rowid = "rowid_example"; // String | 
try {
    Data result = apiInstance.readPost(country, rowid);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#readPost");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **country** | [**String**](.md)|  |
 **rowid** | [**String**](.md)|  |

### Return type

[**Data**](Data.md)

### Authorization

[api_key](../README.md#api_key)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="writePost"></a>
# **writePost**
> Data writePost(country, rowid, blob, key1, key2, key3, key4, key5)



### Example
```java
// Import classes:
//import com.incountry.api.ApiClient;
//import com.incountry.api.ApiException;
//import com.incountry.api.Configuration;
//import com.incountry.api.auth.*;
//import com.incountry.api.DefaultApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: api_key
ApiKeyAuth api_key = (ApiKeyAuth) defaultClient.getAuthentication("api_key");
api_key.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//api_key.setApiKeyPrefix("Token");

DefaultApi apiInstance = new DefaultApi();
String country = "country_example"; // String | 
String rowid = "rowid_example"; // String | 
String blob = "blob_example"; // String | 
String key1 = "key1_example"; // String | 
String key2 = "key2_example"; // String | 
String key3 = "key3_example"; // String | 
String key4 = "key4_example"; // String | 
String key5 = "key5_example"; // String | 
try {
    Data result = apiInstance.writePost(country, rowid, blob, key1, key2, key3, key4, key5);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#writePost");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **country** | [**String**](.md)|  |
 **rowid** | [**String**](.md)|  |
 **blob** | [**String**](.md)|  |
 **key1** | [**String**](.md)|  | [optional]
 **key2** | [**String**](.md)|  | [optional]
 **key3** | [**String**](.md)|  | [optional]
 **key4** | [**String**](.md)|  | [optional]
 **key5** | [**String**](.md)|  | [optional]

### Return type

[**Data**](Data.md)

### Authorization

[api_key](../README.md#api_key)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

