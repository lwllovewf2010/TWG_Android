package com.modusgo.ubi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.modusgo.dd.TrackingStatusService;
import com.modusgo.ubi.db.DbHelper;
import com.modusgo.ubi.db.VehicleContract.VehicleEntry;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class DriverSetupActivity extends MainActivity{
	
	VehiclesAdapter vehiclesAdapter;
	ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>();

	ListView lvVehicles;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_driver_setup);
		super.onCreate(savedInstanceState);
		
		setActionBarTitle("Driver Setup");

		lvVehicles = (ListView) findViewById(R.id.listViewDrivers);
		
		vehiclesAdapter = new VehiclesAdapter(this, vehicles);
		lvVehicles.setAdapter(vehiclesAdapter);
		
		setButtonUpVisibility(false);
		
		updateVehicles();
	}
	
	private void updateVehicles(){
		DbHelper dbHelper = DbHelper.getInstance(this);
		vehicles = dbHelper.getVehiclesShort();
		dbHelper.close();
		
		if(vehicles.size()==1){
			Intent i = new Intent(this, DriverActivity.class);
			i.putExtra(VehicleEntry._ID, vehicles.get(0).id);
			startActivity(i);
			finish();
		}
		
		vehiclesAdapter.notifyDataSetChanged();
	}
	
	class VehiclesAdapter extends BaseAdapter{
		
		Context ctx;
		LayoutInflater lInflater;
		
		SimpleDateFormat sdfFrom = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.getDefault());
		SimpleDateFormat sdfTo = new SimpleDateFormat("MM/dd/yyyy KK:mm aa z", Locale.getDefault());
		
		VehiclesAdapter(Context context, ArrayList<Vehicle> drivers) {
		    ctx = context;
		    lInflater = (LayoutInflater) ctx
		        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
			
			View view = convertView;
		    if (view == null) {
		      view = lInflater.inflate(R.layout.home_drivers_list_item, parent, false);
		    }

		    final Vehicle d = getDriver(position);

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
		    
		    view.findViewById(R.id.imageDiagnostics).setVisibility(View.GONE);
		    view.findViewById(R.id.imageAlerts).setVisibility(View.GONE);
		    
		    return view;
		}
		
		Vehicle getDriver(int position) {
			return ((Vehicle) getItem(position));
		}
		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		setNavigationDrawerItemSelected(MenuItems.DRIVERSETUP);
		vehiclesAdapter.notifyDataSetChanged();
	}
}
