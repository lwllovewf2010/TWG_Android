package com.modusgo.dd;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.modusgo.ubi.Constants;

public class PhoneUnlockReceiver extends BroadcastReceiver {

	public static final String PREF_UNLOCK_COUNT = "unlockCount";
	
    @Override
    public void onReceive(Context context, Intent intent) {

    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    	String action = intent.getAction();
    	
    	if(prefs.getBoolean(Constants.PREF_DD_ENABLED, false) && action.equals(Intent.ACTION_USER_PRESENT)){
	    	prefs.edit().putInt(PREF_UNLOCK_COUNT, prefs.getInt(PREF_UNLOCK_COUNT, 0)+1).commit();
    	}
    }

}