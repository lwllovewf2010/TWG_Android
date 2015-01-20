package com.modusgo.dd;

import com.modusgo.ubi.Constants;
import com.modusgo.ubi.utils.Device;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.support.v4.content.WakefulBroadcastReceiver;

public class ChargerConnectionReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
    	String action = intent.getAction();
    	
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    	Editor e = prefs.edit();
    	
        if(action.equals(Intent.ACTION_POWER_CONNECTED)) {
            e.putBoolean(Constants.PREF_CHARGER_CONNECTED, true);
        }
        else if(action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
            e.putBoolean(Constants.PREF_CHARGER_CONNECTED, false);
        }
        e.commit();
        
        Device.checkDevice(context);
    }
}
