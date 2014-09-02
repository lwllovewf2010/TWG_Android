package com.modusgo.ubi.utils;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

public class RequestPost {

	String url;
	List<NameValuePair> nameValuePairs;
	
	public RequestPost(String url, List<NameValuePair> nameValuePairs) {
		this.url = url;
		this.nameValuePairs = nameValuePairs;
	}
	
	public HttpResponse execute() {
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpPost httppost = new HttpPost(url);

	    try {
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
	        return httpclient.execute(httppost);
	    } catch (ClientProtocolException e) {
	    	e.printStackTrace();
	        return null;
	    } catch (IOException e) {
	    	e.printStackTrace();
	        return null;
	    }  
	}
}
