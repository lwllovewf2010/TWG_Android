package com.modusgo.ubi.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.model.LatLng;
import com.modusgo.dd.LocationService;
import com.modusgo.ubi.Constants;
import com.modusgo.ubi.requesttasks.SendEventsRequest;

public class Device {
	
	private static final int EVENTS_SENDING_FREQUENCY = 3*60*1000;
	
	public static final String PREF_CURRENT_TRACKING_MODE = "currentTracking";
	public static final String PREF_DEVICE_MEID = "deviceMEID";
	public static final String PREF_DEVICE_TYPE = "deviceType";
	public static final String PREF_DEVICE_EVENTS = "deviceEvents";
	public static final String PREF_DEVICE_TRIPS = "deviceTrips";
	public static final String PREF_DEVICE_IN_TRIP = "deviceInTrip";
	public static final String PREF_DEVICE_LATITUDE = "deviceLatitude";
	public static final String PREF_DEVICE_LONGITUDE = "deviceLongitude";
	public static final String PREF_DEVICE_LOCATION_DATE = "locationDate";
	public static final String PREF_IN_TRIP_NOW = "inTripNow";
	private static final String PREF_LAST_SENDEVENT_MILLIS = "lastSendEventMillis";

	public static final String  DEVICE_TYPE_OBD = "obd";
	public static final String  DEVICE_TYPE_OBDBLE = "obdble";
	public static final String  DEVICE_TYPE_SMARTPHONE = "smartphone";
	public static final String  DEVICE_TYPE_IBEACON = "ibeacon";
	
	public static final String  MODE_LIGHT_TRACKING = "lightTracking";
	public static final String  MODE_MEDIUM_TRACKING = "mediumTracking";
	public static final String  MODE_SIGNIFICATION_TRACKING = "significationTracking";
	public static final String  MODE_NAVIGATION_TRACKING = "navigationTracking";
	public static final String  MODE_SUPER_TRACKING = "superTracking";
	
	public static void checkDevice(Context context){
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor e = prefs.edit();
		
		if(System.currentTimeMillis() - prefs.getLong(PREF_LAST_SENDEVENT_MILLIS, 0) >= EVENTS_SENDING_FREQUENCY){
			e.putLong(PREF_LAST_SENDEVENT_MILLIS, System.currentTimeMillis());
			new SendEventsRequest(context).execute();
		}
		
		String newTrackingMode = "";
		
		String deviceType = prefs.getString(PREF_DEVICE_TYPE, "");
		boolean eventsTrackingEnabled = prefs.getBoolean(PREF_DEVICE_EVENTS, false);
		boolean forceServiceUpdate = false;
		System.out.println("Check device: '"+deviceType+"'");
		System.out.println("Events enabled: '"+eventsTrackingEnabled+"'");
		
		switch (deviceType) {
			case DEVICE_TYPE_OBD:
				if(eventsTrackingEnabled){
					newTrackingMode = MODE_LIGHT_TRACKING;
					boolean inTripNow = checkOBDInTrip(prefs.getBoolean(PREF_DEVICE_IN_TRIP, false), 
							new LatLng(Double.parseDouble(prefs.getString(PREF_DEVICE_LATITUDE, "0")), Double.parseDouble(prefs.getString(PREF_DEVICE_LONGITUDE, "0"))), 
							new LatLng(Double.parseDouble(prefs.getString(Constants.PREF_MOBILE_LATITUDE, "0")), Double.parseDouble(prefs.getString(Constants.PREF_MOBILE_LONGITUDE, "0"))));
					System.out.println("intrip now = "+inTripNow);
					
					if(prefs.getBoolean(PREF_IN_TRIP_NOW, false)!=inTripNow){
						forceServiceUpdate = true;
						e.putBoolean(PREF_IN_TRIP_NOW, inTripNow);
					}
				}
				else{
					forceServiceUpdate = true;
					newTrackingMode = "";
					e.putBoolean(PREF_IN_TRIP_NOW, false);
				}
				break;
			case DEVICE_TYPE_SMARTPHONE:
				newTrackingMode = MODE_MEDIUM_TRACKING;	
				break;
			case DEVICE_TYPE_IBEACON:			
			case DEVICE_TYPE_OBDBLE:
				newTrackingMode = MODE_SIGNIFICATION_TRACKING;
				break;
	
			default:
				newTrackingMode = MODE_SIGNIFICATION_TRACKING;
				e.putBoolean(PREF_IN_TRIP_NOW, false);
				break;
		}
		e.commit();
		
		if(!isMyServiceRunning(context,LocationService.class)){
			forceServiceUpdate = true;			
		}
		
		if(!deviceType.equals(DEVICE_TYPE_OBD)){
			if(prefs.getBoolean(PREF_IN_TRIP_NOW, false)){
				newTrackingMode = prefs.getBoolean(Constants.PREF_CHARGER_CONNECTED, false) ? MODE_SUPER_TRACKING : MODE_NAVIGATION_TRACKING;
			}
		}
		
		if(!prefs.getString(PREF_CURRENT_TRACKING_MODE, "").equals(newTrackingMode) || forceServiceUpdate){
			prefs.edit().putString(PREF_CURRENT_TRACKING_MODE, newTrackingMode).commit();
			if(newTrackingMode.equals("")){
				//Stop all tracking services
				context.stopService(new Intent(context, LocationService.class));
			}
			else{
				System.out.println("check device start service, trip: "+prefs.getBoolean(PREF_IN_TRIP_NOW, false));
				context.startService(new Intent(context, LocationService.class));				
			}
		}

		System.out.println("final Tracking mode: "+newTrackingMode);
	}
	
	public static boolean checkOBDInTrip(boolean inTrip, LatLng deviceLocation, LatLng mobileLocation){
		System.out.println("check obd in trip");
		
//		if(deviceLocation.latitude!=0 && deviceLocation.longitude!=0 && mobileLocation.latitude!=0 && mobileLocation.longitude!=0)
//			return false;
		
		if(inTrip){
			float[] results = new float[1];
			Location.distanceBetween(deviceLocation.latitude, deviceLocation.longitude, mobileLocation.latitude, mobileLocation.longitude, results);
			
			System.out.println("odb device distance = "+results[0]);
			
			if(results[0]<500)
				return true;
		}
		
		return false;
	}
	
	private static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
	    ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (serviceClass.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}

}
