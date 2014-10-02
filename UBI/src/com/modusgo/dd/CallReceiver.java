package com.modusgo.dd;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import com.modusgo.ubi.Constants;

public class CallReceiver extends BroadcastReceiver {

	public static final String PREF_CALL_START = "callStartTime";
	public static final String PREF_CALL_END = "callEndTime";
	public static final String PREF_CALL_DURATION = "callDuration";
	private static boolean callStarted = false;
	private static long callStartTime = 0;
	
    @Override
    public void onReceive(Context context, Intent intent) {
    	
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

    	Bundle b = intent.getExtras();
		
    	if(prefs.getBoolean(Constants.PREF_DD_ENABLED, false)){
		    if(b.getString(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_OFFHOOK)){
		    	callStarted = true;
		    	callStartTime = System.currentTimeMillis();
		    	
		    	Calendar c = Calendar.getInstance();
		    	SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT,Locale.getDefault());
		    	prefs.edit().putString(PREF_CALL_START, sdf.format(c.getTime())).commit();
		    }
		    else if(b.getString(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_IDLE)){
		    	if(callStarted){
			    	prefs.edit().putLong(PREF_CALL_DURATION, prefs.getLong(PREF_CALL_DURATION, 0)+(System.currentTimeMillis()-callStartTime)).commit();
		    		callStarted = false;
		    		callStartTime = 0;
		    		
		    		Editor e = prefs.edit();
			    	e.putLong(PREF_CALL_DURATION, prefs.getLong(PREF_CALL_DURATION, 0)+(System.currentTimeMillis()-callStartTime));
		    		callStarted = false;
		    		callStartTime = 0;
		    		
			    	Calendar c = Calendar.getInstance();
			    	SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT,Locale.getDefault());
			    	e.putString(PREF_CALL_END, sdf.format(c.getTime()));
			    	e.commit();
		    	}
		    }
	    }
    }
}