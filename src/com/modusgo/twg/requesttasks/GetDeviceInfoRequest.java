package com.modusgo.twg.requesttasks;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import android.util.Log;

import com.modusgo.twg.Constants;
import com.modusgo.twg.utils.Device;

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
			e.putBoolean(Device.PREF_DEVICE_IN_TRIP, false);
			e.putLong(Constants.PREF_EVENTS_LAST_CHECK, System.currentTimeMillis());			
			e.commit();
		}
		
		Log.w("UBI", message);
	}

	@Override
	protected void onSuccess(JSONObject responseJSON) throws JSONException {
		
		Editor e = prefs.edit();
		e.putString(Device.PREF_DEVICE_TYPE, responseJSON.optString("type"));
		e.putString(Device.PREF_DEVICE_MEID, responseJSON.optString("meid"));
		e.putBoolean(Device.PREF_DEVICE_EVENTS, responseJSON.optBoolean("events"));
		e.putBoolean(Device.PREF_DEVICE_IN_TRIP, !TextUtils.isEmpty(responseJSON.optString("in_trip")));
		e.putString(Device.PREF_DEVICE_LATITUDE, responseJSON.optString("latitude"));
		e.putString(Device.PREF_DEVICE_LONGITUDE, responseJSON.optString("longitude"));
		e.putString(Device.PREF_DEVICE_LOCATION_DATE, responseJSON.optString("location_date"));
		
		e.putLong(Constants.PREF_EVENTS_LAST_CHECK, System.currentTimeMillis());			
		e.commit();
		
		Device.checkDevice(context);
		
		super.onSuccess(responseJSON);
	}
}
