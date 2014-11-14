package com.modusgo.dd;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.modusgo.ubi.Constants;
import com.modusgo.ubi.db.DbHelper;

public class PhoneScreenOnOffReceiver extends BroadcastReceiver {

	public static final String PREF_UNLOCK_COUNT = "unlockCount";
	
    @Override
    public void onReceive(Context context, Intent intent) {

    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    	String action = intent.getAction();
    	
    	if(prefs.getBoolean(Constants.PREF_DD_ENABLED, false) && action.equals(Intent.ACTION_USER_PRESENT)){
	    	prefs.edit().putInt(PREF_UNLOCK_COUNT, prefs.getInt(PREF_UNLOCK_COUNT, 0)+1).commit();
    	}
    	
    	if(prefs.getBoolean(Constants.PREF_DD_ENABLED, false) && action.equals(Intent.ACTION_SCREEN_ON)){	    	
	    	Calendar c = Calendar.getInstance();
	    	SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT_ZULU,Locale.US);
	    	sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
	    	String timestamp = sdf.format(c.getTime());
	    	
	    	DbHelper dbHelper = DbHelper.getInstance(context);
	    	dbHelper.saveDDEvent("phone_usage_start", timestamp);
	    	dbHelper.close();
    	}
    	
    	if(prefs.getBoolean(Constants.PREF_DD_ENABLED, false) && action.equals(Intent.ACTION_SCREEN_OFF)){
	    	Calendar c = Calendar.getInstance();
	    	SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT_ZULU,Locale.US);
	    	sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
	    	String timestamp = sdf.format(c.getTime());
	    	
	    	DbHelper dbHelper = DbHelper.getInstance(context);
	    	dbHelper.saveDDEvent("phone_usage_stop", timestamp);
	    	dbHelper.close();
    	}
    }
}