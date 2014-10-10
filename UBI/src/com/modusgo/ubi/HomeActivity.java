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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import com.modusgo.ubi.db.DbHelper;
import com.modusgo.ubi.db.VehicleContract.VehicleEntry;
import com.modusgo.ubi.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class HomeActivity extends MainActivity{
	
	DriversAdapter driversAdapter;
	ArrayList<Driver> drivers;
	
	ListView lvDrivers;
	ProgressBar progressBar;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_home);
		super.onCreate(savedInstanceState);
		
		setActionBarTitle("HOME");

		progressBar = (ProgressBar)findViewById(R.id.progressBar);
		lvDrivers = (ListView)findViewById(R.id.listViewDrivers);
		
		drivers = getDriversFromDB();
		
		driversAdapter = new DriversAdapter(this, drivers);
		
		lvDrivers.setAdapter(driversAdapter);
		
		btnUp.setImageResource(R.drawable.ic_map);
		
		new GetDriversTask(this).execute("drivers.json");
		
		setButtonUpVisibility(false);
		
		if(!prefs.getString(Constants.PREF_REG_CODE, "").equals(""))
			startService(new Intent(this, TrackingStatusService.class));
		
//		if(drivers.size()==1){
//			Intent i = new Intent(this, DriverActivity.class);
//			i.putExtra("id", 0);
//			startActivity(i);
//			finish();
//		}
	}
	
	@Override
	public void up() {
		startActivity(new Intent(this, DriversLocationsActivity.class));
	}
	
	class DriversAdapter extends BaseAdapter{
		
		Context ctx;
		LayoutInflater lInflater;
		
		SimpleDateFormat sdfFrom = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.getDefault());
		SimpleDateFormat sdfTo = new SimpleDateFormat("MM/dd/yyyy KK:mm aa z", Locale.getDefault());
		
		DriversAdapter(Context context, ArrayList<Driver> drivers) {
		    ctx = context;
		    lInflater = (LayoutInflater) ctx
		        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		  }
		
		@Override
		public int getCount() {
		    return drivers.size();
		}

		@Override
		public Object getItem(int position) {
		    return drivers.get(position);
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

		    final Driver d = getDriver(position);

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
					i.putExtra(VehicleEntry._ID, d.id);
					startActivity(i);
				}
			});
		    
		    view.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(HomeActivity.this);
					prefs.edit().putInt(Constants.PREF_CURRENT_DRIVER, position).commit();
					
					Intent i = new Intent(HomeActivity.this, DriverActivity.class);
					i.putExtra(VehicleEntry._ID, d.id);
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
	
	private ArrayList<Driver> getDriversFromDB(){
		DbHelper dbHelper = DbHelper.getInstance(this);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = db.query(VehicleEntry.TABLE_NAME, 
				new String[]{
				VehicleEntry._ID,
				VehicleEntry.COLUMN_NAME_DRIVER_NAME,
				VehicleEntry.COLUMN_NAME_CAR_MAKE,
				VehicleEntry.COLUMN_NAME_CAR_MODEL,
				VehicleEntry.COLUMN_NAME_CAR_YEAR,
				VehicleEntry.COLUMN_NAME_CAR_CHECKUP,
				VehicleEntry.COLUMN_NAME_LAST_TRIP_DATE,
				VehicleEntry.COLUMN_NAME_ALERTS}, 
				null, null, null, null, null);
		
		ArrayList<Driver> drivers = new ArrayList<Driver>();
		
		if(c.moveToFirst()){
			while (!c.isAfterLast()) {
				Driver d = new Driver();
				d.id = c.getLong(0);
				d.name = c.getString(1);
				d.carMake = c.getString(2);
				d.carModel = c.getString(3);
				d.carYear = c.getString(4);
				d.carCheckup = c.getInt(5) == 1;
				d.lastTripDate = c.getString(6);
				drivers.add(d);
				
				c.moveToNext();
			}
		}
		c.close();
		dbHelper.close();
		return drivers;
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

			responseJSON = Utils.getJSONObjectFromAssets(HomeActivity.this, "vehicles.json");
			System.out.println(responseJSON);
			JSONArray vehiclesJSON = responseJSON.getJSONArray("vehicles");
			System.out.println(vehiclesJSON);
			drivers.clear();
			for (int i = 0; i < vehiclesJSON.length(); i++) {
				JSONObject vehicleJSON = vehiclesJSON.getJSONObject(i);
				drivers.add(Driver.fromJSON(vehicleJSON));
			}
			
			DbHelper dbHelper = DbHelper.getInstance(HomeActivity.this);
			dbHelper.saveDrivers(drivers);
			dbHelper.close();
			
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
