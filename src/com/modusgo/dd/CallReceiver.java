package com.modusgo.dd;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import com.modusgo.ubi.utils.Device;

public class CallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    	if(prefs.getBoolean(Device.PREF_DEVICE_EVENTS, false) && prefs.getBoolean(Device.PREF_DEVICE_IN_TRIP, false)){
    		Intent i = new Intent(context, CallSaverService.class);
        	i.putExtra("action", intent.getExtras().getString(TelephonyManager.EXTRA_STATE));
        	context.startService(i);
	    }
    	
    }
}