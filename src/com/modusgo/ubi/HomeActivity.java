package com.modusgo.ubi;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class HomeActivity extends Activity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		getActionBar().setTitle("HOME");
		
		ListView lvDrivers = (ListView)findViewById(R.id.listViewDrivers);
		
		ArrayList<Driver> data = new ArrayList<Driver>();
		data.add(new Driver("Melissa Hasalonglastname", R.drawable.person_test, "2012 Ford Edge","07/05/2014 05:00 PM PST", true, true));
		data.add(new Driver("Diana Johnson", R.drawable.person_test2, "2011 Ford Focus","07/05/2014 05:00 PM PST", true, false));
		data.add(new Driver("Kate Summerton", R.drawable.person_test3, "1967 Ford Mustang","07/05/2014 05:00 PM PST", false, true));
		
		DriversAdapter driversAdapter = new DriversAdapter(this, data);
		
		lvDrivers.setAdapter(driversAdapter);
	}
	
	class DriversAdapter extends BaseAdapter{
		
		Context ctx;
		LayoutInflater lInflater;
		ArrayList<Driver> objects;
		
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
			// используем созданные, но не используемые view
		    View view = convertView;
		    if (view == null) {
		      view = lInflater.inflate(R.layout.driver_item, parent, false);
		    }

		    Driver d = getDriver(position);

		    ((TextView) view.findViewById(R.id.tvName)).setText(d.name);
		    ((TextView) view.findViewById(R.id.tvVehicle)).setText(d.vehicle);
		    ((TextView) view.findViewById(R.id.tvDate)).setText(d.lastTripDate);
		    
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
					Intent i = new Intent(HomeActivity.this, DriverDetailsActivity.class);
					i.putExtra("id", position);
					startActivity(i);
				}
			});
		    
		    return view;
		}
		
		Driver getDriver(int position) {
			return ((Driver) getItem(position));
		}
		
	}

}
