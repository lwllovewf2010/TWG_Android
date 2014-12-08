package com.modusgo.dd;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import com.modusgo.ubi.Constants;
import com.modusgo.ubi.db.DbHelper;

public class CallSaverService extends IntentService {

	public static final String PREF_CALL_STARTED = "callStarted";
	
	SharedPreferences prefs;
	
	public CallSaverService() {
		super("CallSaver");
	}

	@Override
    protected void onHandleIntent(Intent workIntent) {
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		update(workIntent.getExtras().getString("action"));
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
