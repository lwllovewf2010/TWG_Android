package com.modusgo.ubi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.modusgo.ubi.db.DbHelper;
import com.modusgo.ubi.db.VehicleContract.VehicleEntry;
import com.modusgo.ubi.requesttasks.BaseRequestAsyncTask;
import com.modusgo.ubi.requesttasks.RequestHelper;
import com.modusgo.ubi.requesttasks.SendEventsRequest;
import com.modusgo.ubi.utils.AnimationUtils;
import com.modusgo.ubi.utils.Device;
import com.modusgo.ubi.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class HomeActivity extends MainActivity{
	
	public static final String VEHICLES = "vehicles";
	
	VehiclesAdapter driversAdapter;
	ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>();
	BroadcastReceiver vehiclesUpdateReceiver;
	IntentFilter vehiclesUpdateFilter;

	SwipeRefreshLayout lRefresh;
	ListView lvVehicles;
	TextView tvError;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_home);
		super.onCreate(savedInstanceState);
		
		setActionBarTitle("HOME");

		lRefresh = (SwipeRefreshLayout) findViewById(R.id.lRefresh);
		lvVehicles = (ListView) findViewById(R.id.listViewDrivers);
		tvError = (TextView) findViewById(R.id.tvError);
		
		driversAdapter = new VehiclesAdapter(this, vehicles);
		lvVehicles.setAdapter(driversAdapter);
		
		btnUp.setImageResource(R.drawable.ic_map);
		setButtonUpVisibility(true);
		
		lRefresh.setColorSchemeResources(R.color.ubi_gray, R.color.ubi_green, R.color.ubi_orange, R.color.ubi_red);
		lRefresh.setOnRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				lRefresh.setRefreshing(true);
				AnimationUtils.collapse(tvError);
				new GetVehiclesTask(HomeActivity.this).execute(RequestHelper.VEHICLES);
			}
		});
		
		vehiclesUpdateFilter = new IntentFilter(Constants.BROADCAST_UPDATE_VEHICLES);
		vehiclesUpdateReceiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				updateDrivers();
			}
		};
		
		Device.checkDevice(this);
		new SendEventsRequest(getApplicationContext(), true).execute();
		
		updateDrivers();
	}
	
	private void updateDrivers(){
		DbHelper dbHelper = DbHelper.getInstance(this);
		vehicles = dbHelper.getVehiclesShort();
		dbHelper.close();
		
		if(vehicles.size()==1){
			Intent i = new Intent(this, DriverActivity.class);
			i.putExtra(VehicleEntry._ID, vehicles.get(0).id);
			startActivity(i);
			finish();
		}
		
		driversAdapter.notifyDataSetChanged();
	}
	
	@Override
	public void up() {
		startActivity(new Intent(this, DriversLocationsActivity.class));
	}
	
	class VehiclesAdapter extends BaseAdapter{
		
		Context ctx;
		LayoutInflater lInflater;
		
		SimpleDateFormat sdfFrom = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.getDefault());
		SimpleDateFormat sdfTo = new SimpleDateFormat("MM/dd/yyyy KK:mm aa z", Locale.getDefault());
		
		VehiclesAdapter(Context context, ArrayList<Vehicle> drivers) {
			ctx = context;
			lInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			TimeZone tzFrom = TimeZone.getTimeZone(Constants.DEFAULT_TIMEZONE);
			sdfFrom.setTimeZone(tzFrom);
		}
		
		@Override
		public void notifyDataSetChanged() {
			TimeZone tzTo = TimeZone.getTimeZone(prefs.getString(Constants.PREF_TIMEZONE_OFFSET, Constants.DEFAULT_TIMEZONE));
			sdfTo.setTimeZone(tzTo);
			super.notifyDataSetChanged();
		}
		
		@Override
		public int getCount() {
		    return vehicles.size();
		}

		@Override
		public Object getItem(int position) {
		    return vehicles.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
		    if (convertView == null) {
		    	convertView = lInflater.inflate(R.layout.home_drivers_list_item, parent, false);
		    	holder = new ViewHolder();
		    	holder.tvName = (TextView) convertView.findViewById(R.id.tvName);
		    	holder.tvVehicle = (TextView) convertView.findViewById(R.id.tvVehicle);
		    	holder.tvDateLabel = (TextView)convertView.findViewById(R.id.tvDateLabel);
		    	holder.tvDate = (TextView) convertView.findViewById(R.id.tvDate);
		    	holder.tvInTrip = convertView.findViewById(R.id.tvInTrip);
		    	holder.imagePhoto = (ImageView)convertView.findViewById(R.id.imagePhoto);
		    	holder.imageDiagnostics = (ImageButton) convertView.findViewById(R.id.imageDiagnostics);
		    	holder.imageAlerts = (ImageButton) convertView.findViewById(R.id.imageAlerts);
		    	convertView.setTag(holder);
		    }else{
		    	holder = (ViewHolder) convertView.getTag();
		    }

		    final Vehicle vehicle = getVehicle(position);

		    holder.tvName.setText(vehicle.name);
		    holder.tvVehicle.setText(vehicle.getCarFullName());
		    
		    if(vehicle.inTrip){
		    	holder.tvDateLabel.setVisibility(View.INVISIBLE);
		    	holder.tvDate.setVisibility(View.INVISIBLE);
		    	holder.tvInTrip.setVisibility(View.VISIBLE);
		    }
		    else if(TextUtils.isEmpty(vehicle.lastTripDate)){
		    	holder.tvDateLabel.setVisibility(View.INVISIBLE);
		    	holder.tvDate.setVisibility(View.INVISIBLE);
		    	holder.tvInTrip.setVisibility(View.GONE);
		    }
		    else{
		    	holder.tvDateLabel.setVisibility(View.VISIBLE);
		    	holder.tvDate.setVisibility(View.VISIBLE);
		    	holder.tvInTrip.setVisibility(View.GONE);
		    	try {
		    		holder.tvDate.setText(sdfTo.format(sdfFrom.parse(vehicle.lastTripDate)));
				} catch (ParseException e) {
					holder.tvDate.setText(vehicle.lastTripDate);
					e.printStackTrace();
				}
		    }
		    
		    if(vehicle.photo == null || vehicle.photo.equals(""))
		    	holder.imagePhoto.setImageResource(R.drawable.person_placeholder);
		    else{
		    	DisplayImageOptions options = new DisplayImageOptions.Builder()
		        .showImageOnLoading(R.drawable.person_placeholder)
		        .showImageForEmptyUri(R.drawable.person_placeholder)
		        .showImageOnFail(R.drawable.person_placeholder)
		        .cacheInMemory(true)
		        .cacheOnDisk(true)
		        .build();
		    	
		    	ImageLoader.getInstance().displayImage(vehicle.photo, holder.imagePhoto, options);
		    }
		    
		    
		    if(prefs.getBoolean(Constants.PREF_DIAGNOSTIC, false) && !vehicle.hideEngineIcon){
		    	holder.imageDiagnostics.setVisibility(View.VISIBLE);
			    if(vehicle.carDTCCount<=0){
			    	holder.imageDiagnostics.setImageResource(R.drawable.ic_diagnostics_green);
			    }else{
			    	holder.imageDiagnostics.setImageResource(R.drawable.ic_diagnostics_red);		    	
			    }
		    }
		    else{
		    	holder.imageDiagnostics.setVisibility(View.GONE);
		    }
		    
		   
		    
		    if(vehicle.alerts<=0){
		    	holder.imageAlerts.setImageResource(R.drawable.ic_alerts_green);
		    }else{
		    	holder.imageAlerts.setImageResource(R.drawable.ic_alerts_red);
		    }
		    
		    holder.imageAlerts.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i = new Intent(HomeActivity.this, AlertsActivity.class);
					i.putExtra(VehicleEntry._ID, vehicle.id);
					startActivity(i);
				}
			});
		    
		    convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(HomeActivity.this);
					prefs.edit().putInt(Constants.PREF_CURRENT_DRIVER, position).commit();
					
					Intent i = new Intent(HomeActivity.this, DriverActivity.class);
					i.putExtra(VehicleEntry._ID, vehicle.id);
					//i.putExtra(DriverActivity.SAVED_DRIVER, drivers.get(position));
					startActivity(i);
				}
			});
		    
		    return convertView;
		}
		
		Vehicle getVehicle(int position) {
			return ((Vehicle) getItem(position));
		}
		
		class ViewHolder {
			ImageView imagePhoto;
			TextView tvName;
			TextView tvVehicle;
			TextView tvDateLabel;
			TextView tvDate;
			View tvInTrip;
			ImageButton imageDiagnostics;
			ImageButton imageAlerts;
		}
		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		registerReceiver(vehiclesUpdateReceiver, vehiclesUpdateFilter);
		setNavigationDrawerItemSelected(MenuItems.HOME);
		updateDrivers();
		Utils.gaTrackScreen(this, "Home Screen");
	}
	
	@Override
	protected void onPause() {
		unregisterReceiver(vehiclesUpdateReceiver);
		super.onPause();
	}
	
	class GetVehiclesTask extends BaseRequestAsyncTask{

		public GetVehiclesTask(Context context) {
			super(context);
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		
		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			lRefresh.setRefreshing(false);
		}

		@Override
		protected JSONObject doInBackground(String... params) {
	        requestParams.add(new BasicNameValuePair("page", "1"));
	        requestParams.add(new BasicNameValuePair("per_page", "1000"));
	        return super.doInBackground(params);
		}
		
		@Override
		protected void onError(String message) {
			AnimationUtils.expand(tvError);
		}
		
		@Override
		protected void onSuccess(JSONObject responseJSON) throws JSONException {
			//responseJSON = Utils.getJSONObjectFromAssets(HomeActivity.this, "vehicles.json");
			tvError.setVisibility(View.GONE);
			
			JSONArray vehiclesJSON = responseJSON.getJSONArray(VEHICLES);
			vehicles.clear();
			for (int i = 0; i < vehiclesJSON.length(); i++) {
				JSONObject vehicleJSON = vehiclesJSON.getJSONObject(i);
				vehicles.add(Vehicle.fromJSON(getApplicationContext(), vehicleJSON));
			}
			
			DbHelper dbHelper = DbHelper.getInstance(HomeActivity.this);
			dbHelper.saveVehicles(vehicles);
			dbHelper.close();
			
			updateDrivers();
			super.onSuccess(responseJSON);
		}
	}
}
