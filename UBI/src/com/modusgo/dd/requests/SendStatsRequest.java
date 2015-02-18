package com.modusgo.dd.requests;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Base64;

import com.modusgo.ubi.Constants;

public class SendStatsRequest extends AsyncTask<String, String, String[]>{
	String finalScore[] = new String[]{"-","-","-"};
	SharedPreferences prefs;
	
	public SendStatsRequest(Context context) {
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
	}
	
    @Override
    protected String[] doInBackground(String... uri) {
    	
		// POST
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost;
		try {
			DateFormat dateTimeformatter = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.getDefault());
			Calendar c = Calendar.getInstance();
			
			JSONObject requestJSON = new JSONObject();
			c.setTimeInMillis(System.currentTimeMillis());
			requestJSON.put("date",dateTimeformatter.format(c.getTime()));
			requestJSON.put("usage_time",0/*prefs.getLong(Constants.PREF_USAGE_TIME, 0)/1000f*/);
			//requestJSON.put("call_time",prefs.getLong(CallReceiver.PREF_CALL_DURATION, 0)/1000f);
			requestJSON.put("sms_count",0/*prefs.getInt(Constants.PREF_SMS_COUNT, 0)*/);
			requestJSON.put("blocked_calls",0/*prefs.getInt(Constants.PREF_BLOCKED_CALLS_COUNT, 0)*/);
			requestJSON.put("blocked_sms", 0/*prefs.getInt(Constants.PREF_BLOCKED_SMS_COUNT, 0)*/);
				
			httpPost = new HttpPost(uri[0]);
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
			
			if(httpResponse.getStatusLine().getStatusCode()>=HttpStatus.SC_OK && httpResponse.getStatusLine().getStatusCode()<=299){
				if(!finalResult.isNull("status") && finalResult.getString("status").equals("ok")){
					
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
    protected void onPostExecute(String result[]) {
        super.onPostExecute(result);
        if(!result[0].equals("error")){
//        	Editor e = prefs.edit();
//        	e.putLong(CallReceiver.PREF_CALL_DURATION, 0);
//        	e.putString(CallReceiver.PREF_CALL_START, "");
//        	e.putString(CallReceiver.PREF_CALL_END, "");
//        	e.putInt(PhoneScreenOnOffReceiver.PREF_UNLOCK_COUNT, 0);
//        	e.commit();
        	
        	//Toast.makeText(app.getApplicationContext(), "Usage statistics sent", Toast.LENGTH_SHORT).show();
        }        
    }
}