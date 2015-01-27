package com.modusgo.dd;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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
		if(action.equals(Intent.ACTION_USER_PRESENT)){
		   	prefs.edit().putInt(PREF_UNLOCK_COUNT, prefs.getInt(PREF_UNLOCK_COUNT, 0)+1).commit();
	    }
	    
		boolean screenOn = prefs.getBoolean(PREF_PHONE_ON, false);
		
	    if(action.equals(Intent.ACTION_SCREEN_ON) && !screenOn){
		   	savePhoneUsageStart();
	    }
	    	
	    if(action.equals(Intent.ACTION_SCREEN_OFF) && screenOn){
		   	savePhoneUsageStop();
	    }
    }
	
	private void savePhoneUsageStart(){
		if(!prefs.getBoolean(PREF_PHONE_ON, false)){
	    	prefs.edit().putBoolean(PREF_PHONE_ON, true).commit();
	    	
	    	DbHelper dbHelper = DbHelper.getInstance(this);
	    	dbHelper.saveTrackingEvent(new Tracking("phone_usage_start"));
	    	dbHelper.close();
		}
	}
	
	private void savePhoneUsageStop(){
		if(prefs.getBoolean(PREF_PHONE_ON, false)){
	    	prefs.edit().putBoolean(PREF_PHONE_ON, false).commit();
	    	
	    	DbHelper dbHelper = DbHelper.getInstance(this);
	    	dbHelper.saveTrackingEvent(new Tracking("phone_usage_end"));
	    	dbHelper.close();
		}
	}

}
