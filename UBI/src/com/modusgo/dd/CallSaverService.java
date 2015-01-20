package com.modusgo.dd;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import com.bugsnag.android.Bugsnag;
import com.modusgo.ubi.Constants;
import com.modusgo.ubi.Tracking;
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
    	if(phoneState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)){
    		savePhoneCallStart();
		}
		else if(phoneState.equals(TelephonyManager.EXTRA_STATE_IDLE)){
		   	savePhoneCallStop();
		}
	}
	
	private void savePhoneCallStart(){
		System.out.println("call start");
		if(!prefs.getBoolean(PREF_CALL_STARTED, false)){
			Editor e = prefs.edit();
	    	e.putBoolean(PREF_CALL_STARTED, true).commit();
	    	e.commit();
	    	
	    	DbHelper dbHelper = DbHelper.getInstance(this);
	    	dbHelper.saveTrackingEvent(new Tracking("call_usage_start"), prefs.getLong(Constants.PREF_DRIVER_ID, 0));
	    	dbHelper.close();
			Bugsnag.notify(new RuntimeException("dd event: call started"));
		}
	}
	
	private void savePhoneCallStop(){
		System.out.println("call end");
		if(prefs.getBoolean(PREF_CALL_STARTED, false)){
	    	Editor e = prefs.edit();
	    	e.putBoolean(PREF_CALL_STARTED, false);
	    	e.commit();
	    	
	    	DbHelper dbHelper = DbHelper.getInstance(this);
	    	dbHelper.saveTrackingEvent(new Tracking("call_usage_end"), prefs.getLong(Constants.PREF_DRIVER_ID, 0));
	    	dbHelper.close();
			Bugsnag.notify(new RuntimeException("dd event: call ended"));
		}
	}

}
