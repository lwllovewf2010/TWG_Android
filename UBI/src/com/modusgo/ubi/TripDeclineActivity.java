package com.modusgo.ubi;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.modusgo.dd.LocationService;
import com.modusgo.ubi.db.DbHelper;

public class TripDeclineActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trip_decline);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.edit().putBoolean(Constants.PREF_TRIP_DECLINED, true).commit();
		
		DbHelper dbHelper = DbHelper.getInstance(this);
    	dbHelper.saveTrackingEvent(new Tracking(LocationService.EVENT_TRIP_DECLINED), prefs.getLong(Constants.PREF_DRIVER_ID, 0));
    	dbHelper.close();
		
    	Intent i = new Intent(this, LocationService.class);
    	i.putExtra(LocationService.EXTRA_ACTION, LocationService.Action.TRIP_DECLINE);
    	startService(i);
    	
		System.out.println("TRIP DECLINE ACTIVITY");
		finish();
	}
}
