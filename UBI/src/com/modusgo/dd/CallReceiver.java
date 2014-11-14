package com.modusgo.dd;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import com.modusgo.ubi.Constants;
import com.modusgo.ubi.db.DbHelper;

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
		    	SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT_ZULU,Locale.US);
		    	sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		    	String callStartTimestamp = sdf.format(c.getTime());
		    	prefs.edit().putString(PREF_CALL_START, callStartTimestamp).commit();
		    	
		    	DbHelper dbHelper = DbHelper.getInstance(context);
		    	dbHelper.saveDDEvent("call_usage_start", callStartTimestamp);
		    	dbHelper.close();
		    }
		    else if(b.getString(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_IDLE)){
		    	if(callStarted){
			    	Editor e = prefs.edit();
			    	e.putLong(PREF_CALL_DURATION, prefs.getLong(PREF_CALL_DURATION, 0)+(System.currentTimeMillis()-callStartTime));
		    		callStarted = false;
		    		callStartTime = 0;
		    		
			    	Calendar c = Calendar.getInstance();
			    	SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT_ZULU,Locale.US);
			    	sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			    	String callEndTimestamp = sdf.format(c.getTime());
			    	e.putString(PREF_CALL_END, callEndTimestamp);
			    	e.commit();
			    	
			    	DbHelper dbHelper = DbHelper.getInstance(context);
			    	dbHelper.saveDDEvent("call_usage_stop", callEndTimestamp);
			    	dbHelper.close();
		    	}
		    }
	    }
    }
}