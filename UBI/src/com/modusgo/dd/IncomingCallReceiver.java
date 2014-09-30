package com.modusgo.dd;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import com.modusgo.ubi.Constants;

public class IncomingCallReceiver extends BroadcastReceiver {

	public static final String ADDRESS = "address"; 
	public static final String DATE = "date"; 
	public static final String READ = "read"; 
	public static final String STATUS = "status"; 
	public static final String TYPE = "type"; 
	public static final String BODY = "body"; 
	public static final int MESSAGE_TYPE_INBOX = 1;
	public static final int MESSAGE_TYPE_SENT = 2; 
	
	private static boolean callStarted = false;
	private static long callStartTime = 0;
	
    @Override
    public void onReceive(Context context, Intent intent) {
    	
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

    	Bundle b = intent.getExtras();
		String phoneNumber = b.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
	    if(phoneNumber==null)
	    	phoneNumber="nonum";
	    
	    if(b.getString(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_OFFHOOK)){
	    	    	callStarted = true;
	    	    	callStartTime = System.currentTimeMillis();
	    	    }
	    	    else if(b.getString(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_IDLE)){
	    	    	if(callStarted){
		    	    	prefs.edit().putLong(Constants.PREF_CALL_TIME, prefs.getLong(Constants.PREF_CALL_TIME, 0)+(System.currentTimeMillis()-callStartTime)).commit();
	    		        callStarted = false;
	    		        callStartTime = 0;
	    	    	}
	    	    }
    }

}