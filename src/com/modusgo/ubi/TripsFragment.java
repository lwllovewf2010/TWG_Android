package com.modusgo.ubi;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class TripsFragment extends Fragment{
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LinearLayout rootView = (LinearLayout)inflater.inflate(R.layout.framgent_trips, container, false);
		
		((MainActivity)getActivity()).setActionBarTitle("TRIPS");
		
		ListView lv = (ListView)rootView.findViewById(R.id.listView);
		
		LinkedHashMap<String, ArrayList<Trip>> tripsMap = new LinkedHashMap<>();
		Calendar c = Calendar.getInstance();
		SimpleDateFormat sdfDate = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
		
		for (int i = 0; i < 5; i++) {
			ArrayList<Trip> trips = new ArrayList<Trip>();
			
			int tripsCount = 1 + new Random().nextInt(4);
			
			for (int j = 0; j < tripsCount; j++) {
				trips.add(new Trip(new Random().nextInt(4), 
						c.getTimeInMillis() + j * new Random().nextInt(400000), 
						c.getTimeInMillis() + 400000 + j * new Random().nextInt(400000), 
						new Random().nextInt(40000)));
			}
			tripsMap.put(sdfDate.format(c.getTime()), trips);
			c.add(Calendar.DAY_OF_YEAR, -1);
		}
		
		TripsAdapter adapter = new TripsAdapter(getActivity(), tripsMap);
		lv.setAdapter(adapter);
		
		return rootView;
	}
	
	class TripsAdapter extends BaseAdapter{
		
		LinkedHashMap<String, ArrayList<Trip>> tripsMap;
		LayoutInflater lInflater;
		
		public TripsAdapter(Context context, LinkedHashMap<String, ArrayList<Trip>> tripsMap) {
		    lInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		    this.tripsMap = tripsMap;
		}
		
		@Override
		public int getCount() {
			int count = 0;
			
			Iterator<ArrayList<Trip>> it = tripsMap.values().iterator();
			while (it.hasNext())
			{
				count+=it.next().size();				
			}
			count+=tripsMap.size();
			return count;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			for (Map.Entry<String, ArrayList<Trip>> entry : tripsMap.entrySet()) {
			    ArrayList<Trip> trips = entry.getValue();
				int size = trips.size() + 1; 
				if(position == 0) return getHeaderView(entry.getKey(), parent);
				if(position < size) return getTripView(trips.get(position-1), parent);  
				position -= size;
			}
			
	        return null;  
		}
		
		private View getHeaderView(String text, ViewGroup parent){
			View view = lInflater.inflate(R.layout.trips_list_header, parent, false);
			
			((TextView)view.findViewById(R.id.tvDate)).setText(text);
			
			return view;
		}
		
		private View getTripView(Trip t, ViewGroup parent){
			View view = lInflater.inflate(R.layout.trips_list_item, parent, false);

			if(t.eventsCount>0){
				TextView tvEventsCount = (TextView)view.findViewById(R.id.tvCounter);
				tvEventsCount.setText(""+t.eventsCount);
				tvEventsCount.setVisibility(View.VISIBLE);
			}
			((TextView)view.findViewById(R.id.tvStartTime)).setText(t.getStartDateString());
			((TextView)view.findViewById(R.id.tvEndTime)).setText(t.getEndDateString());
			((TextView)view.findViewById(R.id.tvDistance)).setText(new DecimalFormat("0.00").format(t.getDistanceMiles())+" MI");
			
			return view;
		}
		
	}

}
