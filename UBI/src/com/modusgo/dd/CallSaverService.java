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
import android.telephony.TelephonyManager;

import com.modusgo.ubi.Constants;
import com.modusgo.ubi.db.DbHelper;
import com.modusgo.ubi.utils.RequestGet;
import com.modusgo.ubi.utils.Utils;

public class CallSaverService extends IntentService {

	public static final String PREF_CALL_STARTED = "callStarted";
	
	SharedPreferences prefs;
	
	public CallSaverService() {
		super("CallSaver");
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
				
				savePhoneCallStop();
			}
		}
    }
	
	private void update(String phoneState){
    	if(prefs.getBoolean(Constants.PREF_DEVICE_EVENTS, false) && prefs.getBoolean(Constants.PREF_DEVICE_IN_TRIP, false)){
		    if(phoneState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)){
				savePhoneCallStart();
		    }
		    else if(phoneState.equals(TelephonyManager.EXTRA_STATE_IDLE)){
		    	savePhoneCallStop();
		    }
	    }
    }
	
	private void savePhoneCallStart(){
		Editor e = prefs.edit();
    	e.putBoolean(PREF_CALL_STARTED, true).commit();
    	e.commit();
    	
    	Calendar c = Calendar.getInstance();
    	SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT_ZULU,Locale.US);
    	sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    	String callStartTimestamp = sdf.format(c.getTime());
    	
    	DbHelper dbHelper = DbHelper.getInstance(this);
    	dbHelper.saveDDEvent("call_usage_start", callStartTimestamp);
    	dbHelper.close();
	}
	
	private void savePhoneCallStop(){
		if(prefs.getBoolean(PREF_CALL_STARTED, false)){
	    	Editor e = prefs.edit();
	    	e.putBoolean(PREF_CALL_STARTED, false);
	    	e.commit();
			
	    	Calendar c = Calendar.getInstance();
	    	SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT_ZULU,Locale.US);
	    	sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
	    	String callEndTimestamp = sdf.format(c.getTime());
	    	
	    	DbHelper dbHelper = DbHelper.getInstance(this);
	    	dbHelper.saveDDEvent("call_usage_end", callEndTimestamp);
	    	dbHelper.close();
		}
	}

}
