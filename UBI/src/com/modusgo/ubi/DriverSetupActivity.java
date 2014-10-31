package com.modusgo.ubi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.modusgo.dd.TrackingStatusService;
import com.modusgo.ubi.db.DbHelper;
import com.modusgo.ubi.db.VehicleContract.VehicleEntry;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class DriverSetupActivity extends MainActivity{
	
	DriversAdapter driversAdapter;
	ArrayList<Driver> drivers = new ArrayList<Driver>();

	ListView lvDrivers;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_driver_setup);
		super.onCreate(savedInstanceState);
		
		setActionBarTitle("Driver Setup");

		lvDrivers = (ListView) findViewById(R.id.listViewDrivers);
		
		driversAdapter = new DriversAdapter(this, drivers);
		lvDrivers.setAdapter(driversAdapter);
		
		setButtonUpVisibility(true);
		
		if(!prefs.getString(Constants.PREF_REG_CODE, "").equals(""))
			startService(new Intent(this, TrackingStatusService.class));
		
		updateDrivers();
	}
	
	private void updateDrivers(){
		DbHelper dbHelper = DbHelper.getInstance(this);
		drivers = dbHelper.getDriversShort();
		dbHelper.close();
		
		if(drivers.size()==1){
			Intent i = new Intent(this, DriverActivity.class);
			i.putExtra(VehicleEntry._ID, drivers.get(0).id);
			startActivity(i);
			finish();
		}
		
		driversAdapter.notifyDataSetChanged();
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
		    
		    return view;
		}
		
		Driver getDriver(int position) {
			return ((Driver) getItem(position));
		}
		
	}
	
	@Override
	public void onResume() {
		setNavigationDrawerItemSelected(MenuItems.DRIVERSETUP);
		getActionBar().getCustomView().setBackgroundColor(Color.parseColor("#000000"));
		driversAdapter.notifyDataSetChanged();
		super.onResume();
	}
}
