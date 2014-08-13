package com.modusgo.ubi;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TripActivity extends MainActivity {
	
	public static final String EXTRA_TRIP_ID = "tripId";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_trip);
		super.onCreate(savedInstanceState);
		
		setActionBarTitle("TRIP DETAILS");
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		Driver driver = DbHelper.getDrivers().get(prefs.getInt(Constants.PREF_CURRENT_DRIVER, 0));
		
		((TextView)findViewById(R.id.tvName)).setText(driver.name);
		((ImageView)findViewById(R.id.imagePhoto)).setImageResource(driver.imageId);
		
		findViewById(R.id.btnMenu).setVisibility(View.GONE);
		findViewById(R.id.btnTimePeriod).setVisibility(View.GONE);
		
		LinearLayout llContent = (LinearLayout)findViewById(R.id.llContent);
		
		for (int i = 0; i < 3; i++) {
			RelativeLayout eventItem = (RelativeLayout) getLayoutInflater().inflate(R.layout.trip_event_item, llContent, false);
			llContent.addView(eventItem);
		}
		
		/*try{
			((MainActivity)getActivity()).setNavigationDrawerItemSelected(MenuItems.SETTINGS.toInt());
		}
		catch(ClassCastException e){
			e.printStackTrace();
		}*/
		
	}
}