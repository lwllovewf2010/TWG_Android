package com.modusgo.ubi.requesttasks;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.modusgo.ubi.Constants;

public class GetDeviceInfoRequest extends BaseRequestAsyncTask {
	
	ArrayList<Long> eventIds;
	
	public GetDeviceInfoRequest(Context context) {
		super(context);
	}
	
	@Override
	protected JSONObject doInBackground(String... params) {
		return super.doInBackground("device.json");
	}
	
	@Override
	protected void onError(String message) {
		if(System.currentTimeMillis() - prefs.getLong(Constants.PREF_EVENTS_LAST_CHECK, 0) > (1000 * 60 * 5)){
			Editor e = prefs.edit();
			e.putBoolean(Constants.PREF_DEVICE_IN_TRIP, false);
			e.putLong(Constants.PREF_EVENTS_LAST_CHECK, System.currentTimeMillis());			
			e.commit();
		}
		
		Log.w("UBI", message);
	}

	@Override
	protected void onSuccess(JSONObject responseJSON) throws JSONException {
		
		boolean in_trip = responseJSON.optBoolean("in_trip");
		
		Editor e = prefs.edit();
		e.putBoolean(Constants.PREF_DEVICE_EVENTS, responseJSON.optBoolean("events"));
		e.putBoolean(Constants.PREF_DEVICE_TRIPS, responseJSON.optBoolean("trips"));
		e.putBoolean(Constants.PREF_DEVICE_IN_TRIP, in_trip);
		e.putString(Constants.PREF_DEVICE_TYPE, responseJSON.optString("type"));
		e.putLong(Constants.PREF_EVENTS_LAST_CHECK, System.currentTimeMillis());			
		e.commit();
		
		if(!in_trip){
			new SendEventsRequest(context).execute();
		}
		
		super.onSuccess(responseJSON);
	}
}
