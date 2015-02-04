package com.modusgo.dd;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.modusgo.twg.utils.Device;

public class PhoneScreenOnOffReceiver extends BroadcastReceiver {
	
    @Override
    public void onReceive(Context context, Intent intent) {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    	if(prefs.getBoolean(Device.PREF_DEVICE_EVENTS, false) && prefs.getBoolean(Device.PREF_DEVICE_IN_TRIP, false)){
        	Intent i = new Intent(context, PhoneUsageSaverService.class);
        	i.putExtra("action", intent.getAction());
        	context.startService(i);
	    }
    }
}