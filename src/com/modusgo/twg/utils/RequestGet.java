package com.modusgo.twg.utils;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;

public class RequestGet {

	String url;
	List<NameValuePair> nameValuePairs;

	public RequestGet(String url) {
		this.url = url;
	}
	
	public RequestGet(String url, List<NameValuePair> nameValuePairs) {
		this.url = url;
		this.nameValuePairs = nameValuePairs;
	}
	
	public HttpResponse execute() {
		String paramsString = "";
		if(nameValuePairs!=null){
			paramsString = URLEncodedUtils.format(nameValuePairs, "UTF-8");
		}
		
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpGet httpget = new HttpGet(url + "?" + paramsString);

	    try {
			return httpclient.execute(httpget);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
	        return null;
		} catch (IOException e) {
			e.printStackTrace();
	        return null;
		}  
	}
}
