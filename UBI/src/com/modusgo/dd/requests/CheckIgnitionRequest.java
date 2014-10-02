package com.modusgo.dd.requests;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;

import com.modusgo.ubi.Constants;
import com.modusgo.ubi.utils.Utils;

public class CheckIgnitionRequest extends BasicAsyncTask{
	String finalScore[] = new String[]{"-","-","-"};
	SharedPreferences prefs;
	double lat, lon;
	Context context;
	
	public CheckIgnitionRequest(Context context, double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
		this.context = context;
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
	}
	
    @Override
    protected String[] doInBackground(Void... v) {
    	
		// POST
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost;
		try {
			
			JSONObject requestJSON = new JSONObject();
			requestJSON.put("mobile_id", Utils.getUUID(context));
			requestJSON.put("lat", lat);
			requestJSON.put("lon", lon);
			httpPost = new HttpPost(Constants.getCheckIgnitionURL(Utils.getUUID(context)));
			// Prepare JSON to send by setting the entity
			httpPost.setEntity(new StringEntity(requestJSON.toString(), "UTF-8"));

			// Set up the header types needed to properly transfer JSON
			httpPost.setHeader("Content-Type", "application/json");
			httpPost.setHeader("Accept-Encoding", "application/json");
			httpPost.setHeader("Accept-Language", "en-US");
			    
			//Basic access authentication
			String encoding = Base64.encodeToString((Constants.API_AUTH_LOGIN+":"+Constants.API_AUTH_PASS).getBytes(), Base64.NO_WRAP);
			httpPost.setHeader("Authorization", "Basic " + encoding);
			// Execute POST
			HttpResponse httpResponse = httpClient.execute(httpPost);
			BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), "UTF-8"));
			String json = reader.readLine();
			JSONTokener tokener = new JSONTokener(json);
			JSONObject finalResult = new JSONObject(tokener);
			
			System.out.println(finalResult);
			
			
			if(httpResponse.getStatusLine().getStatusCode()>=HttpStatus.SC_OK && httpResponse.getStatusLine().getStatusCode()<=299){
				if(!finalResult.isNull("status") && finalResult.getString("status").equals("ok")){
					finalScore[0] = !finalResult.isNull("ignition") ? String.valueOf(finalResult.getInt("ignition")) : "0";
					finalScore[1] = !finalResult.isNull("away") ? String.valueOf(finalResult.getInt("away")) : "0";
					finalScore[2] = !finalResult.isNull("away_distance") ? String.valueOf(finalResult.getInt("away_distance")) : "0";
				}
				else{
					finalScore[0] = "error";
					finalScore[1] = "Server error";
					finalScore[2] = "Wrong status field";
				}
			}
			else{
				finalScore[0] = "error";
				finalScore[1] = Integer.toString(httpResponse.getStatusLine().getStatusCode());
				finalScore[2] = httpResponse.getStatusLine().getReasonPhrase();
			}
			    
			    
		} catch (Exception e) {
			e.printStackTrace();
			finalScore[0] = "error";
			finalScore[1] = "Server error";
			finalScore[2] = e.getMessage();
		}   
		
        return finalScore;
    }

    @Override
    protected void onPostExecuteSuccess(String ignition, String away, String meters) {
    	super.onPostExecuteSuccess(ignition, away, meters);
    	
    }
    
    @Override
    protected void onPostExecuteError(String errorCode, String errorMessage) {
    	super.onPostExecuteError(errorCode, errorMessage);
    	
    }
}