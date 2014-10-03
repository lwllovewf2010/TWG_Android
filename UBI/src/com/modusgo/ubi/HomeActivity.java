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
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

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
		
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
        .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
        .memoryCacheSize(2 * 1024 * 1024)
        .diskCacheSize(50 * 1024 * 1024)
        .diskCacheFileCount(100)
        .build();
		ImageLoader.getInstance().init(config);

		progressBar = (ProgressBar)findViewById(R.id.progressBar);
		lvDrivers = (ListView)findViewById(R.id.listViewDrivers);
		
		dHelper = DriversHelper.getInstance();
		
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
		    ((TextView) view.findViewById(R.id.tvVehicle)).setText(d.vehicle);
		    try {
				((TextView) view.findViewById(R.id.tvDate)).setText(sdfTo.format(sdfFrom.parse(d.lastTripDate)));
			} catch (ParseException e) {
				((TextView) view.findViewById(R.id.tvDate)).setText(d.lastTripDate);
				e.printStackTrace();
			}
		    
		    ImageView imagePhoto = (ImageView)view.findViewById(R.id.imagePhoto);
		    if(d.imageUrl == null || d.imageUrl.equals(""))
		    	imagePhoto.setImageResource(d.imageId);
		    else{
		    	DisplayImageOptions options = new DisplayImageOptions.Builder()
		        .showImageOnLoading(R.drawable.person_placeholder)
		        .showImageForEmptyUri(R.drawable.person_placeholder)
		        .showImageOnFail(R.drawable.person_placeholder)
		        .cacheInMemory(true)
		        .cacheOnDisk(true)
		        .build();
		    	
		    	ImageLoader.getInstance().displayImage(d.imageUrl, imagePhoto, options);
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

			JSONArray driversJSON = responseJSON.getJSONArray("drivers");
			System.out.println(driversJSON);

			dHelper.drivers.clear();
			for (int i = 0; i < driversJSON.length(); i++) {
				JSONObject driverJSON = driversJSON.getJSONObject(i);

				Driver d = new Driver(driverJSON.getLong("id"),
						driverJSON.optString("name"),
						R.drawable.person_placeholder,
						driverJSON.optString("year") + " "
								+ driverJSON.optString("make") + " "
								+ driverJSON.optString("model"), "", "",
						driverJSON.isNull("last_trip") ? "Undefined" : Utils.fixTimezoneZ(driverJSON.optString("last_trip")),
						0, 0, 
						driverJSON.optString("grade"));

				d.alerts = driverJSON.optInt("count_alerts");
				d.diags = driverJSON.optInt("count_diags");
				d.imageUrl = driverJSON.optString("photo");
				d.fuelLeft = driverJSON.optInt("fuel_left",-1);
				JSONObject locationJSON = driverJSON.optJSONObject("location");
				if(locationJSON!=null){
					d.address = locationJSON.optString("address");
					JSONObject mapJSON = locationJSON.getJSONObject("map");
					if(mapJSON!=null){
						d.latitude = mapJSON.optDouble("latitude");
						d.longitude = mapJSON.optDouble("longitude");
					}
					else{
						d.latitude = 0;
						d.longitude = 0;
					}
				}
				else{
					d.address = "Undefined";
					d.latitude = 0;
					d.longitude = 0;
				}
				d.markerIcon = driverJSON.optString("icon");

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
