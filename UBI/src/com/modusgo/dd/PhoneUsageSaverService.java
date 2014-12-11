package com.modusgo.dd;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.modusgo.ubi.Constants;
import com.modusgo.ubi.Tracking;
import com.modusgo.ubi.db.DbHelper;

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
		update(workIntent.getExtras().getString("action"));
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
    	dbHelper.saveTrackingEvent(new Tracking(timestamp, "phone_usage_start"));
    	dbHelper.close();
	}
	
	private void savePhoneUsageStop(){
		Calendar c = Calendar.getInstance();
    	SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT_ZULU,Locale.US);
    	sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    	String timestamp = sdf.format(c.getTime());
    	
    	prefs.edit().putBoolean(PREF_PHONE_ON, false);
    	
    	DbHelper dbHelper = DbHelper.getInstance(this);
    	dbHelper.saveTrackingEvent(new Tracking(timestamp, "phone_usage_end"));
    	dbHelper.close();
	}

}
