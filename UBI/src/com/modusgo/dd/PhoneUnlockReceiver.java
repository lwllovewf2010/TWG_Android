package com.modusgo.dd;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.modusgo.ubi.Constants;

public class PhoneUnlockReceiver extends BroadcastReceiver {

	public static final String ADDRESS = "address"; 
	public static final String DATE = "date"; 
	public static final String READ = "read"; 
	public static final String STATUS = "status"; 
	public static final String TYPE = "type"; 
	public static final String BODY = "body"; 
	public static final int MESSAGE_TYPE_INBOX = 1;
	public static final int MESSAGE_TYPE_SENT = 2; 
	
    @Override
    public void onReceive(Context context, Intent intent) {

    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    	String action = intent.getAction();
    	
    	if(action.equals(Intent.ACTION_USER_PRESENT) /*&& prefs.getBoolean(Constants.PREF_ENABLED, false)*/){
	    	System.out.println("unlock");
    	}
    }

}