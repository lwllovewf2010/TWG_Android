package com.modusgo.ubi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.modusgo.dd.TrackingStatusService;
import com.modusgo.demo.R;
import com.modusgo.ubi.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class HomeActivity extends MainActivity{
	
	DriversAdapter driversAdapter;
	DriversHelper dHelper;
	
	ListView lvDrivers;
	ProgressBar progressBar;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_home);
		super.onCreate(savedInstanceState);
		
		setActionBarTitle("HOME");

		progressBar = (ProgressBar)findViewById(R.id.progressBar);
		lvDrivers = (ListView)findViewById(R.id.listViewDrivers);
		
		dHelper = DriversHelper.getInstance();
		
		if(dHelper.drivers.size()==1){
			Intent i = new Intent(this, DriverActivity.class);
			i.putExtra("id", 0);
			startActivity(i);
			finish();
		}
		
		driversAdapter = new DriversAdapter(this, dHelper.drivers);
		
		lvDrivers.setAdapter(driversAdapter);
		
		btnUp.setImageResource(R.drawable.ic_map);
		
		new GetDriversTask(this).execute("drivers.json");
		
		setButtonUpVisibility(false);
		
		if(!prefs.getString(Constants.PREF_REG_CODE, "").equals(""))
			startService(new Intent(this, TrackingStatusService.class));
	}
	
	@Override
	public void up() {
		startActivity(new Intent(this, DriversLocationsActivity.class));
	}
	
	class DriversAdapter extends BaseAdapter{
		
		Context ctx;
		LayoutInflater lInflater;
		ArrayList<Driver> objects;
		
		SimpleDateFormat sdfFrom = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.getDefault());
		SimpleDateFormat sdfTo = new SimpleDateFormat("MM/dd/yyyy KK:mm aa z", Locale.getDefault());
		
		DriversAdapter(Context context, ArrayList<Driver> drivers) {
		    ctx = context;
		    objects = drivers;
		    lInflater = (LayoutInflater) ctx
		        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		  }
		
		@Override
		public int getCount() {
		    return objects.size();
		}

		@Override
		public Object getItem(int position) {
		    return objects.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			
			View view = convertView;
		    if (view == null) {
		      view = lInflater.inflate(R.layout.home_drivers_list_item, parent, false);
		    }

		    Driver d = getDriver(position);

		    ((TextView) view.findViewById(R.id.tvName)).setText(d.name);
		    ((TextView) view.findViewById(R.id.tvVehicle)).setText(d.getCarFullName());
		    try {
				((TextView) view.findViewById(R.id.tvDate)).setText(sdfTo.format(sdfFrom.parse(d.lastTripDate)));
			} catch (ParseException e) {
				((TextView) view.findViewById(R.id.tvDate)).setText(d.lastTripDate);
				e.printStackTrace();
			}
		    
		    ImageView imagePhoto = (ImageView)view.findViewById(R.id.imagePhoto);
		    if(d.photo == null || d.photo.equals(""))
		    	imagePhoto.setImageResource(R.drawable.person_placeholder);
		    else{
		    	DisplayImageOptions options = new DisplayImageOptions.Builder()
		        .showImageOnLoading(R.drawable.person_placeholder)
		        .showImageForEmptyUri(R.drawable.person_placeholder)
		        .showImageOnFail(R.drawable.person_placeholder)
		        .cacheInMemory(true)
		        .cacheOnDisk(true)
		        .build();
		    	
		    	ImageLoader.getInstance().displayImage(d.photo, imagePhoto, options);
		    }
		    
		    if(d.diags<=0){
		    	((ImageButton)view.findViewById(R.id.imageDiagnostics)).setImageResource(R.drawable.ic_diagnostics_green);
		    }else{
		    	((ImageButton)view.findViewById(R.id.imageDiagnostics)).setImageResource(R.drawable.ic_diagnostics_red);		    	
		    }
		    
		    ImageButton btnAlerts = (ImageButton) view.findViewById(R.id.imageAlerts);
		    
		    if(d.alerts<=0){
		    	btnAlerts.setImageResource(R.drawable.ic_alerts_green);
		    }else{
		    	btnAlerts.setImageResource(R.drawable.ic_alerts_red);
		    }
		    
		    btnAlerts.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i = new Intent(HomeActivity.this, AlertsActivity.class);
					i.putExtra("id", position);
					startActivity(i);
				}
			});
		    
		    view.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(HomeActivity.this);
					prefs.edit().putInt(Constants.PREF_CURRENT_DRIVER, position).commit();
					
					Intent i = new Intent(HomeActivity.this, DriverActivity.class);
					i.putExtra("id", position);
					//i.putExtra(DriverActivity.SAVED_DRIVER, drivers.get(position));
					startActivity(i);
				}
			});
		    
		    return view;
		}
		
		Driver getDriver(int position) {
			return ((Driver) getItem(position));
		}
		
	}
	
	@Override
	public void onResume() {
		setNavigationDrawerItemSelected(MenuItems.HOME);
		driversAdapter.notifyDataSetChanged();
		super.onResume();
	}
	
	class GetDriversTask extends BaseRequestAsyncTask{

		public GetDriversTask(Context context) {
			super(context);
		}
		
		@Override
		protected void onPreExecute() {
			progressBar.setVisibility(View.VISIBLE);
			lvDrivers.setVisibility(View.GONE);
			super.onPreExecute();
		}
		
		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			progressBar.setVisibility(View.GONE);
			lvDrivers.setVisibility(View.VISIBLE);
		}

		@Override
		protected JSONObject doInBackground(String... params) {
	        requestParams.add(new BasicNameValuePair("page", "1"));
	        requestParams.add(new BasicNameValuePair("per_page", "1000"));
			
	        return super.doInBackground(params);
		}
		
		@Override
		protected void onSuccess(JSONObject responseJSON) throws JSONException {

			JSONArray vehiclesJSON = responseJSON.getJSONArray("vehicles");
			System.out.println(vehiclesJSON);

			dHelper.drivers.clear();
			for (int i = 0; i < vehiclesJSON.length(); i++) {
				JSONObject vehicleJSON = vehiclesJSON.getJSONObject(i);

				Driver d = new Driver();
				d.id = vehicleJSON.getLong("id");
				
				if(vehicleJSON.isNull("driver")){
					JSONObject driverJSON = vehicleJSON.getJSONObject("driver");
					d.name = driverJSON.optString("name");
					d.imageUrl = driverJSON.optString("photo");
					d.markerIcon = driverJSON.optString("icon");
				}
				
				if(!vehicleJSON.isNull("car")){
					JSONObject carJSON = vehicleJSON.getJSONObject("car");
					d.carVIN = carJSON.optString("vin");
					d.carMake = carJSON.optString("make");
					d.carModel = carJSON.optString("model");
					d.carYear = carJSON.optString("year");
					d.carFuelLevel = carJSON.optInt("fuel_level", -1);
					d.carCheckup = carJSON.optBoolean("checkup");
				}
				
				if(!vehicleJSON.isNull("location")){
					JSONObject locationJSON = vehicleJSON.getJSONObject("location");
					d.latitude = locationJSON.optDouble("latitude");
					d.longitude = locationJSON.optDouble("longitude");
					d.address = locationJSON.optString("address");
					d.lastTripDate = Utils.fixTimezoneZ(locationJSON.optString("last_trip_time","Undefined"));
					d.lastTripId = locationJSON.optLong("last_trip_id");
				}
				
				if(!vehicleJSON.isNull("stats")){
					JSONObject statsJSON = vehicleJSON.getJSONObject("stats");
					d.score = statsJSON.optInt("score");
					d.grade = statsJSON.optString("grade");
					d.totalTripsCount = statsJSON.optInt("trips");
					d.totalDrivingTime = statsJSON.optInt("time");
					d.totalDistance = statsJSON.optDouble("distance");
					d.totalBraking = statsJSON.optInt("braking");
					d.totalAcceleration = statsJSON.optInt("acceleration");
					d.totalSpeeding = statsJSON.optInt("speeding");
					d.totalSpeedingDistance = statsJSON.optDouble("speeding_distance");
					d.alerts = statsJSON.optInt("new_alerts");
				}
				
				dHelper.drivers.add(d);
			}

			driversAdapter.notifyDataSetChanged();
			setButtonUpVisibility(true);
			super.onSuccess(responseJSON);
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		//outState.putSerializable(SAVED_DRIVERS, drivers);
		super.onSaveInstanceState(outState);
	}

}
