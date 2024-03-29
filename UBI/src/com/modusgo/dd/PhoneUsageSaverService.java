package com.modusgo.dd;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.bugsnag.android.Bugsnag;
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
		boolean screenLocked = prefs.getBoolean(PREF_PHONE_ON, false);
		
		if(action.equals(Intent.ACTION_USER_PRESENT) && !screenLocked){
//		   	prefs.edit().putInt(PREF_UNLOCK_COUNT, prefs.getInt(PREF_UNLOCK_COUNT, 0)+1).commit();
	    	System.out.println("phone unlocked");
		   	savePhoneUsageStart();
	    }
		
//	    if(action.equals(Intent.ACTION_SCREEN_ON) && !screenOn){
//    		System.out.println("screen on");
//		   	savePhoneUsageStart();
//	    }
	    	
	    if(action.equals(Intent.ACTION_SCREEN_OFF) && screenLocked){
	    	System.out.println("screen off");
		   	savePhoneUsageStop();
	    }
    }
	
	private void savePhoneUsageStart(){
		if(!prefs.getBoolean(PREF_PHONE_ON, false)){
	    	prefs.edit().putBoolean(PREF_PHONE_ON, true).commit();
	    	
	    	DbHelper dbHelper = DbHelper.getInstance(this);
	    	dbHelper.saveTrackingEvent(new Tracking("phone_usage_start"), prefs.getLong(Constants.PREF_DRIVER_ID, 0));
	    	dbHelper.close();
			Bugsnag.notify(new RuntimeException("dd event: phone unlocked"));
		}
	}
	
	private void savePhoneUsageStop(){
		if(prefs.getBoolean(PREF_PHONE_ON, false)){
	    	prefs.edit().putBoolean(PREF_PHONE_ON, false).commit();
	    	
	    	DbHelper dbHelper = DbHelper.getInstance(this);
	    	dbHelper.saveTrackingEvent(new Tracking("phone_usage_end"), prefs.getLong(Constants.PREF_DRIVER_ID, 0));
	    	dbHelper.close();
			Bugsnag.notify(new RuntimeException("dd event: phone screen off"));
		}
	}

}
