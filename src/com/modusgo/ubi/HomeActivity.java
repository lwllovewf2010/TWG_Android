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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.modusgo.ubi.utils.Utils;

public class HomeActivity extends MainActivity{
	
	private static final String SAVED_DRIVERS = "drivers";
	
	ArrayList<Driver> drivers;
	DriversAdapter driversAdapter;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_home);
		super.onCreate(savedInstanceState);
		
		setActionBarTitle("HOME");
		
		ListView lvDrivers = (ListView)findViewById(R.id.listViewDrivers);
		
		if(savedInstanceState!=null){
			drivers = (ArrayList<Driver>) savedInstanceState.getSerializable(SAVED_DRIVERS);
		}
		else
			drivers = new ArrayList<Driver>();
		
		driversAdapter = new DriversAdapter(this, drivers);
		
		lvDrivers.setAdapter(driversAdapter);
		
		new GetDriversTask(this).execute("drivers.json");
		
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
		    
		    ((ImageView)view.findViewById(R.id.imagePhoto)).setImageResource(d.imageId);
		    
		    if(d.diagnosticsOK){
		    	((ImageView)view.findViewById(R.id.imageDiagnostics)).setImageResource(R.drawable.ic_diagnostics_green);
		    }else{
		    	((ImageView)view.findViewById(R.id.imageDiagnostics)).setImageResource(R.drawable.ic_diagnostics_red);		    	
		    }
		    
		    if(d.alertsOK){
		    	((ImageView)view.findViewById(R.id.imageAlerts)).setImageResource(R.drawable.ic_alerts_green);
		    }else{
		    	((ImageView)view.findViewById(R.id.imageAlerts)).setImageResource(R.drawable.ic_alerts_red);		    	
		    }
		    
		    view.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(HomeActivity.this);
					prefs.edit().putInt(Constants.PREF_CURRENT_DRIVER, position).commit();
					
					Intent i = new Intent(HomeActivity.this, DriverActivity.class);
					i.putExtra("id", position);
					i.putExtra(DriverActivity.SAVED_DRIVER, drivers.get(position));
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
		setButtonUpVisibility(false);
		super.onResume();
	}
	
	class GetDriversTask extends BaseRequestAsyncTask{

		public GetDriversTask(Context context) {
			super(context);
		}

		@Override
		protected JSONObject doInBackground(String... params) {
	        requestParams.add(new BasicNameValuePair("page", "1"));
	        requestParams.add(new BasicNameValuePair("per_page", "1000"));
			
	        return super.doInBackground(params);
		}
		
		@Override
		protected void onSuccess(JSONObject responseJSON) {
			try {
				JSONArray driversJSON = responseJSON.getJSONArray("drivers");
				
				drivers.clear();
				for (int i = 0; i < driversJSON.length(); i++) {
					JSONObject driverJSON = driversJSON.getJSONObject(i);
					drivers.add(new Driver(driverJSON.getLong("id"), 
							driverJSON.getString("name"), 
							R.drawable.person_placeholder, 
							driverJSON.getString("year")+" "+driverJSON.getString("make")+" "+driverJSON.getString("model"), 
							"", 
							"", 
							Utils.fixTimezoneZ(driverJSON.getString("last_trip")), 
							driverJSON.getInt("count_new_diags") == 0 ? true : false, 
							driverJSON.getInt("count_new_alerts") == 0 ? true : false, 
							0, 
							0, 
							""));
				}
				
				driversAdapter.notifyDataSetChanged();
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			super.onSuccess(responseJSON);
		}
		
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(SAVED_DRIVERS, drivers);
		super.onSaveInstanceState(outState);
	}

}
