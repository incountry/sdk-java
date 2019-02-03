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
> Data deletePost()



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
try {
    Data result = apiInstance.deletePost();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#deletePost");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**Data**](Data.md)

### Authorization

[api_key](../README.md#api_key)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="keylookupPost"></a>
# **keylookupPost**
> Data keylookupPost()



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
try {
    Data result = apiInstance.keylookupPost();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#keylookupPost");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**Data**](Data.md)

### Authorization

[api_key](../README.md#api_key)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="lookupPost"></a>
# **lookupPost**
> Data lookupPost()



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
try {
    Data result = apiInstance.lookupPost();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#lookupPost");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**Data**](Data.md)

### Authorization

[api_key](../README.md#api_key)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="readPost"></a>
# **readPost**
> Data readPost()



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
try {
    Data result = apiInstance.readPost();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#readPost");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**Data**](Data.md)

### Authorization

[api_key](../README.md#api_key)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="writePost"></a>
# **writePost**
> Data writePost()



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
try {
    Data result = apiInstance.writePost();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#writePost");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**Data**](Data.md)

### Authorization

[api_key](../README.md#api_key)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

