package com.modusgo.ubi;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.modusgo.ubi.utils.Utils;

public class TripsFragment extends Fragment{
	
	Driver driver;
	DriversHelper dHelper;
	int driverIndex = 0;
	SharedPreferences prefs;
	
	LinkedHashMap<String, ArrayList<Trip>> tripsMap;
	TripsAdapter adapter;
	
	LinearLayout llProgress;
	ListView lv;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LinearLayout rootView = (LinearLayout)inflater.inflate(R.layout.framgent_trips, container, false);
		
		((MainActivity)getActivity()).setActionBarTitle("TRIPS");

		if(savedInstanceState!=null){
			driverIndex = savedInstanceState.getInt("id");
		}
		else if(getArguments()!=null){
			driverIndex = getArguments().getInt("id");
		}

		dHelper = DriversHelper.getInstance();
		driver = dHelper.getDriverByIndex(driverIndex);
		tripsMap = driver.tripsMap;
		
		((TextView)rootView.findViewById(R.id.tvName)).setText(driver.name);
		((ImageView)rootView.findViewById(R.id.imagePhoto)).setImageResource(driver.imageId);
		
		rootView.findViewById(R.id.btnSwitchDriverMenu).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((DriverActivity)getActivity()).menu.toggle();
			}
		});
		
		rootView.findViewById(R.id.btnTimePeriod).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				createDialog().show();
			}
		});
		
		llProgress = (LinearLayout)rootView.findViewById(R.id.llProgress);
		lv = (ListView)rootView.findViewById(R.id.listView);
		
		adapter = new TripsAdapter();
		lv.setAdapter(adapter);
		
		new GetTripsTask(getActivity()).execute("drivers/"+driver.id+"/trips.json");
		
		return rootView;
	}
	
	String[] timePeriods = new String[]{"Last Month", "This Month", "All"};
	
	private Dialog createDialog() {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    builder.setTitle("Change time period")
	           .setItems(timePeriods, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int which) {
	               // The 'which' argument contains the index position
	               // of the selected item
	           }
	    });
	    return builder.create();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt("id", driverIndex);
		super.onSaveInstanceState(outState);
	}
	
	class GetTripsTask extends BaseRequestAsyncTask{
		
		public GetTripsTask(Context context) {
			super(context);
		}
		
		@Override
		protected void onPreExecute() {
			if(tripsMap.size()==0){
				llProgress.setVisibility(View.VISIBLE);
				lv.setVisibility(View.GONE);
			}
			super.onPreExecute();
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
				JSONArray tripsJSON = responseJSON.getJSONArray("trips");
				
				SimpleDateFormat sdfDate = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
				
				ArrayList<Trip> trips = new ArrayList<Trip>();
				Calendar cPrev = Calendar.getInstance();
				Calendar cNow = Calendar.getInstance();
				int j = 0;
				tripsMap.clear();
				for (int i = 0; i < tripsJSON.length(); i++) {
					JSONObject tipJSON = tripsJSON.getJSONObject(i);
					
					System.out.println("i = "+i);
					Trip t = new Trip(
							tipJSON.getLong("id"), 
							tipJSON.getInt("total_harsh_events"), 
							Utils.fixTimezoneZ(tipJSON.getString("start_time")), 
							Utils.fixTimezoneZ(tipJSON.getString("end_time")), 
							tipJSON.getDouble("mileage"));
					
					
					if(j>0){
						cPrev.setTime(trips.get(j-1).getStartDate());
						cNow.setTime(t.getStartDate());
						if(cNow.get(Calendar.YEAR) != cPrev.get(Calendar.YEAR) || 
								(cNow.get(Calendar.YEAR) == cPrev.get(Calendar.YEAR) && cNow.get(Calendar.DAY_OF_YEAR) != cPrev.get(Calendar.DAY_OF_YEAR))){
							tripsMap.put(sdfDate.format(trips.get(j-1).getStartDate()), trips);
							trips = new ArrayList<Trip>();
							j = 0;
						}
					}
					j++;
					trips.add(t);
				}
				
				driver.tripsMap = tripsMap;
				
				adapter.notifyDataSetChanged();
				
			} catch (JSONException e) {
				e.printStackTrace();
			}

			llProgress.setVisibility(View.GONE);
			lv.setVisibility(View.VISIBLE);
			
			super.onSuccess(responseJSON);
		}
	}
	
	class TripsAdapter extends BaseAdapter{

		LayoutInflater lInflater;
		
		public TripsAdapter() {
		    lInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
			((TextView)view.findViewById(R.id.tvDistance)).setText(new DecimalFormat("0.00").format(t.distance)+" MI");
			
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(getActivity(), TripActivity.class);
					intent.putExtra(TripActivity.EXTRA_TRIP_ID, 0);
					startActivity(intent);
				}
			});
			
			return view;
		}
		
	}

}
