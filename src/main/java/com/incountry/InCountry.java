package com.incountry;

import java.util.ArrayList;

import com.incountry.api.ApiClient;
import com.incountry.api.Configuration;
import com.incountry.api.DefaultApi;
import com.incountry.api.auth.ApiKeyAuth;
import com.incountry.model.Data;

public class InCountry 
{
	DefaultApi apiInstance;
	InCrypto crypto;
    
    public InCountry(String apikey, String secret) throws Exception 
    {
	    	crypto = new InCrypto(secret);
	        initSdk(apikey);
    }
    
	private void initSdk(String apikey) 
	{
		ApiClient defaultClient = Configuration.getDefaultApiClient();
		ApiKeyAuth api_key = (ApiKeyAuth) defaultClient.getAuthentication("api_key");
		api_key.setApiKey(apikey);
		apiInstance = new DefaultApi();
	}
	
	public void put(String country, String rowid, String blob, String key1, String key2, String key3, String key4, String key5) throws Exception 
	{ 
		rowid = crypto.encrypt(rowid);
		blob = crypto.encrypt(blob);
		if (key1 != null) key1 = crypto.hash(key1);
		if (key2 != null) key2 = crypto.hash(key2);
		if (key3 != null) key3 = crypto.hash(key3);
		if (key4 != null) key4 = crypto.hash(key4);
		if (key5 != null) key5 = crypto.hash(key5);
		Data result = apiInstance.writePost(country, rowid, blob, key1, key2, key3, key4, key5);
	}
	
	public String get(String country, String rowid) throws Exception
	{
		rowid = crypto.encrypt(rowid);
		Data loco = apiInstance.readPost(country, rowid);
		String blob = crypto.decrypt(loco.getBlob());
		return blob;
	}

	public void delete(String country, String rowid) throws Exception
	{
		rowid = crypto.encrypt(rowid);
		Data result = apiInstance.deletePost(country, rowid);
	}
	
	public LocoList lookup(String country, String key1, String key2, String key3, String key4, String key5) throws Exception
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

		LocoList result = apiInstance.locoscanGet(country, key1, key2, key3, key4, key5);
		int i = result.size();
		while (i-->0) 
		{
			LocoModel loco = result.get(i);
			loco.setRowid(crypto.decrypt(loco.getRowid()));
			loco.setBlob(crypto.decrypt(loco.getBlob()));
			loco.setKey1(o1);
			loco.setKey2(o2);
			loco.setKey3(o3);
			loco.setKey4(o4);
			loco.setKey5(o5);
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
		IDList ll = apiInstance.locokeyscanGet(country, key1, key2, key3, key4, key5);
		int i = ll.size();
		while (i-->0) 
		{
			String loco = ll.get(i);
			result.add(loco);
		}
		return result;
	}
    
	public static void main(String[] args) 
	{
		String APIKEY = "1PS2lhygQEmVdcD2g6CWaOgTZxChmPJaE6HFUmVc";
		String CRYPTOSEED = "supersecret";

		try
		{
			InCountry api = new InCountry(APIKEY, CRYPTOSEED);
			api.put("US", "row0001", "blobbymcblobface", "foo", "bar", null, null, null);
			api.put("US", "row0002", "I am the very model of a modern major general", null, "foo", "bar", null, null);
			api.put("US", "row0003", "We hold these truths to be self-evident", "bar", "foo", null, null, null);
			System.out.println(api.get("US", "row0001"));
			System.out.println(api.lookup("US", null, "foo", null, null, null));
			System.out.println(api.keyLookup("US", "foo", null, null, null, null));
			api.delete("US", "row0001");
			api.delete("US", "row0002");
			api.delete("US", "row0003");
		}
		catch (Exception x) { x.printStackTrace(); }
	}

}
