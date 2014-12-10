package com.modusgo.ubi.utils;

import com.modusgo.ubi.Constants;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Device {
	
	public static void checkDevice(Context context){
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		String deviceType = prefs.getString(Constants.PREF_DEVICE_TYPE, "");
		
		if(prefs.getBoolean(Constants.PREF_DEVICE_EVENTS, false)){
			switch (deviceType) {
			case "":
				
				break;
	
			default:
				break;
			}
		}
		else{
			
		}
		
	}

}
