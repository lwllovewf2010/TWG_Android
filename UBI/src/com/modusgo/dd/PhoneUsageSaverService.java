package com.modusgo.dd;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.modusgo.ubi.Constants;
import com.modusgo.ubi.db.DbHelper;
import com.modusgo.ubi.utils.RequestGet;
import com.modusgo.ubi.utils.Utils;

public class PhoneUsageSaverService extends IntentService {

	public static final String PREF_UNLOCK_COUNT = "unlockCount";
	private static final String PREF_PHONE_ON = "phoneScreenOn";
	
	SharedPreferences prefs;
	
	public PhoneUsageSaverService() {
		super("PhoneUsageSaver");
	}

	@Override
    protected void onHandleIntent(Intent workIntent) {
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		String baseUrl = Constants.API_BASE_URL_PREFIX+prefs.getString(Constants.PREF_CLIENT_ID, "")+Constants.API_BASE_URL_POSTFIX;
		List<NameValuePair> requestParams = new ArrayList<NameValuePair>();
		requestParams.add(new BasicNameValuePair("auth_key", prefs.getString(Constants.PREF_AUTH_KEY, "")));
		HttpResponse result = new RequestGet(baseUrl+"device.json", requestParams).execute();
		
		int status = 0;
		
		try{
			status = result.getStatusLine().getStatusCode();
			//message = "Error "+result.getStatusLine().getStatusCode()+": "+result.getStatusLine().getReasonPhrase();
		}
		catch(NullPointerException e){
			e.printStackTrace();
			status = 0;
		}
		
		if(status>=200 && status<300){
			JSONObject responseJSON = Utils.getJSONObjectFromHttpResponse(result);
			if(responseJSON!=null && responseJSON.optString("status").equals("success")){
				Editor e = prefs.edit();
				e.putBoolean(Constants.PREF_DEVICE_EVENTS, responseJSON.optBoolean("events"));
				e.putBoolean(Constants.PREF_DEVICE_TRIPS, responseJSON.optBoolean("trips"));
				e.putBoolean(Constants.PREF_DEVICE_IN_TRIP, responseJSON.optBoolean("in_trip"));
				e.putBoolean(Constants.PREF_DEVICE_TYPE, responseJSON.optBoolean("type"));
				e.putLong(Constants.PREF_EVENTS_LAST_CHECK, System.currentTimeMillis());			
				e.commit();
				
		        update(workIntent.getExtras().getString("action"));
			}
		}
		else{
			if(System.currentTimeMillis() - prefs.getLong(Constants.PREF_EVENTS_LAST_CHECK, 0) > (1000 * 60 * 5)){
				Editor e = prefs.edit();
				e.putBoolean(Constants.PREF_DEVICE_IN_TRIP, false);
				e.putLong(Constants.PREF_EVENTS_LAST_CHECK, System.currentTimeMillis());			
				e.commit();
				
				savePhoneUsageStop();
			}
		}
    }
	
	private void update(String action){
		if(prefs.getBoolean(Constants.PREF_DEVICE_EVENTS, false) && prefs.getBoolean(Constants.PREF_DEVICE_IN_TRIP, false)){
	    	if(action.equals(Intent.ACTION_USER_PRESENT)){
		    	prefs.edit().putInt(PREF_UNLOCK_COUNT, prefs.getInt(PREF_UNLOCK_COUNT, 0)+1).commit();
	    	}
	    	
	    	if(action.equals(Intent.ACTION_SCREEN_ON)){	    	
		    	savePhoneUsageStart();
	    	}
	    	
	    	if(action.equals(Intent.ACTION_SCREEN_OFF)){
		    	savePhoneUsageStop();
	    	}
		}
    }
	
	private void savePhoneUsageStart(){
		Calendar c = Calendar.getInstance();
    	SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT_ZULU,Locale.US);
    	sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    	String timestamp = sdf.format(c.getTime());
    	
    	prefs.edit().putBoolean(PREF_PHONE_ON, true);
    	
    	DbHelper dbHelper = DbHelper.getInstance(this);
    	dbHelper.saveDDEvent("phone_usage_start", timestamp);
    	dbHelper.close();
	}
	
	private void savePhoneUsageStop(){
		Calendar c = Calendar.getInstance();
    	SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT_ZULU,Locale.US);
    	sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    	String timestamp = sdf.format(c.getTime());
    	
    	prefs.edit().putBoolean(PREF_PHONE_ON, false);
    	
    	DbHelper dbHelper = DbHelper.getInstance(this);
    	dbHelper.saveDDEvent("phone_usage_end", timestamp);
    	dbHelper.close();
	}

}
