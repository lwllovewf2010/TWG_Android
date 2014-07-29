package com.modusgo.ubi;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class HomeActivity extends Activity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		ListView lvDrivers = (ListView)findViewById(R.id.listViewDrivers);
		
		ArrayList<Driver> data = new ArrayList<Driver>();
		data.add(new Driver("Melissa\nHasalonglastname", "2012 Ford Edge","07/05/2014 05:00 PM PST"));
		data.add(new Driver("Melissa\nHasalonglastname", "2011 Ford Focus","07/05/2014 05:00 PM PST"));
		data.add(new Driver("Melissa\nHasalonglastname", "2010 Ford Fiesta","07/05/2014 05:00 PM PST"));
		
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
		public View getView(int position, View convertView, ViewGroup parent) {
			// используем созданные, но не используемые view
		    View view = convertView;
		    if (view == null) {
		      view = lInflater.inflate(R.layout.driver_item, parent, false);
		    }

		    Driver p = getDriver(position);

		    ((TextView) view.findViewById(R.id.tvName)).setText(p.name);
		    ((TextView) view.findViewById(R.id.tvVehicle)).setText(p.vehicle);
		    ((TextView) view.findViewById(R.id.tvDate)).setText(p.lastTripDate);

		    return view;
		}
		
		Driver getDriver(int position) {
			return ((Driver) getItem(position));
		}
		
	}

}
