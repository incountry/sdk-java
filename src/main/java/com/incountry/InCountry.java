package com.incountry;

import java.util.ArrayList;
import java.util.UUID;

import com.google.gson.internal.LinkedTreeMap;
import com.incountry.api.ApiClient;
import com.incountry.api.Configuration;
import com.incountry.api.DefaultApi;
import com.incountry.api.JSON;
import com.incountry.api.auth.ApiKeyAuth;
import com.incountry.model.Data;

public class InCountry 
{
	private static final int VERSION = 1;
	
	private String CONFIG;
	
	DefaultApi apiInstance;
	InCrypto crypto;
	JSON json;
	    
    public InCountry(String apikey, String secret, String account_country) throws Exception 
    {
    	CONFIG = "{\"version\": "+VERSION+", \"country\": \""+account_country+"\"}";
    	crypto = new InCrypto(secret);
        initSdk(apikey);
        json = new JSON();
    }
    
	private void initSdk(String apikey) 
	{
		ApiClient defaultClient = Configuration.getDefaultApiClient();
		ApiKeyAuth api_key = (ApiKeyAuth) defaultClient.getAuthentication("api_key");
		api_key.setApiKey(apikey);
		apiInstance = new DefaultApi();
	}
	
	public String write(String country, String rowid, String blob, String key1, String key2, String key3, String key4, String key5) throws Exception
	{
		if (rowid == null || rowid.equals("")) rowid = UUID.randomUUID().toString();
		rowid = crypto.encrypt(rowid);
		blob = crypto.encrypt(blob);
		if (key1 != null) key1 = crypto.hash(key1);
		if (key2 != null) key2 = crypto.hash(key2);
		if (key3 != null) key3 = crypto.hash(key3);
		if (key4 != null) key4 = crypto.hash(key4);
		if (key5 != null) key5 = crypto.hash(key5);
		Data result = apiInstance.writePost(CONFIG, country, rowid, blob, key1, key2, key3, key4, key5);
		return crypto.decrypt(result.getRowid());
	}
	
	public String read(String country, String rowid) throws Exception
	{
		rowid = crypto.encrypt(rowid);
		Data loco = apiInstance.readPost(CONFIG, country, rowid);
		String blob = crypto.decrypt(loco.getBlob());
		return blob;
	}

	public void delete(String country, String rowid) throws Exception
	{
		rowid = crypto.encrypt(rowid);
		Data result = apiInstance.deletePost(CONFIG, country, rowid);
	}
	
	public ArrayList<Data> lookup(String country, String key1, String key2, String key3, String key4, String key5) throws Exception
	{
		String o1 = key1;
		String o2 = key2;
		String o3 = key3;
		String o4 = key4;
		String o5 = key5;
		
		if (key1 != null) key1 = crypto.hash(key1);
		if (key2 != null) key2 = crypto.hash(key2);
		if (key3 != null) key3 = crypto.hash(key3);
		if (key4 != null) key4 = crypto.hash(key4);
		if (key5 != null) key5 = crypto.hash(key5);

		Data d = apiInstance.lookupPost(CONFIG, country, key1, key2, key3, key4, key5);
		ArrayList<LinkedTreeMap<String, String>> list = json.deserialize(d.getBlob(), ArrayList.class);
		int i = list.size();
		ArrayList<Data> result = new ArrayList<>();
		while (i-->0) 
		{
			LinkedTreeMap<String, String> row = list.get(i);
			Data data = new Data();
			result.add(data);
			data.setRowid(crypto.decrypt(row.get("rowid")));
			data.setBlob(crypto.decrypt(row.get("blob")));
			data.setKey1(o1);
			data.setKey2(o2);
			data.setKey3(o3);
			data.setKey4(o4);
			data.setKey5(o5);
		}
		return result;
	}

	public ArrayList<String> keyLookup(String country, String key1, String key2, String key3, String key4, String key5) throws Exception
	{
		if (key1 != null) key1 = crypto.hash(key1);
		if (key2 != null) key2 = crypto.hash(key2);
		if (key3 != null) key3 = crypto.hash(key3);
		if (key4 != null) key4 = crypto.hash(key4);
		if (key5 != null) key5 = crypto.hash(key5);

		ArrayList<String> result = new ArrayList<>();
		Data d = apiInstance.keylookupPost(CONFIG, country, key1, key2, key3, key4, key5);
		ArrayList<String> list = json.deserialize(d.getBlob(), ArrayList.class);
		int i = list.size();
		while (i-->0) 
		{
			String loco = list.get(i);
			result.add(crypto.decrypt(loco));
		}
		return result;
	}
    
	public static void main(String[] args) 
	{
		String APIKEY = args[0];
		String CRYPTOSEED = args[1];

		try
		{
			InCountry api = new InCountry(APIKEY, CRYPTOSEED, "**");
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
