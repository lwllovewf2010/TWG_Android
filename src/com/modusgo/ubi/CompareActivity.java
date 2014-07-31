package com.modusgo.ubi;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

public class CompareActivity extends MainActivity{
	
	private static enum Paramater { TRIPS_COUNT, HARSH_EVENTS, SCORE};
	
	TabHost tabHost;
	View previousTab;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_compare);
		super.onCreate(savedInstanceState);
		
		setActionBarTitle("COMPARE");
		
		//ListView lvDrivers = (ListView)rootView.findViewById(R.id.listViewDrivers);
		Spinner spinnerTimePerion = (Spinner)findViewById(R.id.spinnerTimePeriod);
		String[] spinnerItems = {"This Month", "Last Month", "All"};
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_item, spinnerItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		spinnerTimePerion.setAdapter(adapter);
		
		spinnerTimePerion.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				//Toast.makeText(getActivity(), "Position = " + position, Toast.LENGTH_SHORT).show();
				//TODO update listViews with data for selected time period
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		
		tabHost = (TabHost) findViewById(android.R.id.tabhost);
        tabHost.setup();
        setupTab(new TextView(this), "Trips", R.id.tab1);
		setupTab(new TextView(this), "Harsh events", R.id.tab2);
		setupTab(new TextView(this), "Score", R.id.tab3);
		
		previousTab = tabHost.getCurrentView();
		
        tabHost.setOnTabChangedListener(new OnTabChangeListener() {
        	public void onTabChanged(String tabId) {
        		View currentTab = tabHost.getCurrentView();
        		currentTab.setAnimation(fadeInAnimation());
                previousTab.setAnimation(fadeOutAnimation());
                previousTab = currentTab;
        	}
        });
		
        ListView lvTrips = (ListView)findViewById(R.id.tab1);
        DriversAdapter driverTripsAdapter = new DriversAdapter(this, DbHelper.getDrivers(), Paramater.TRIPS_COUNT);
		lvTrips.setAdapter(driverTripsAdapter);
		
		ListView lvHarshEvents = (ListView)findViewById(R.id.tab2);
        DriversAdapter driverHarshEventsAdapter = new DriversAdapter(this, DbHelper.getDrivers(), Paramater.HARSH_EVENTS);
        lvHarshEvents.setAdapter(driverHarshEventsAdapter);
        
		ListView lvScore = (ListView)findViewById(R.id.tab3);
        DriversAdapter driverScoreAdapter = new DriversAdapter(this, DbHelper.getDrivers(), Paramater.SCORE);
        lvScore.setAdapter(driverScoreAdapter);
        
	}
	
	private void setupTab(final View view, final String tag, int contentId) {
		View tabview = createTabView(tabHost.getContext(), tag);
	    TabSpec setContent = tabHost.newTabSpec(tag).setIndicator(tabview).setContent(contentId);
	    tabHost.addTab(setContent);
	}

	private static View createTabView(final Context context, final String text) {
		TextView tv = (TextView) LayoutInflater.from(context).inflate(R.layout.tabs_bg, null);
		tv.setText(text);
		return tv;
	}
	
	public Animation fadeOutAnimation()
	{
		Animation fadeOut = new AlphaAnimation(1f, 0f);
	    fadeOut.setDuration(240);
	    fadeOut.setInterpolator(new AccelerateInterpolator());
	    return fadeOut;
	}

	public Animation fadeInAnimation()
	{
		Animation fadeIn = new AlphaAnimation(0f, 1f);
	    fadeIn.setDuration(240);
	    fadeIn.setInterpolator(new AccelerateInterpolator());
	    return fadeIn;
	}
	
	@Override
	public void onResume() {
		setNavigationDrawerItemSelected(MenuItems.COMPARE);
		setButtonUpVisibility(true);
		super.onResume();
	}
	
	class DriversAdapter extends BaseAdapter{
		
		Context ctx;
		LayoutInflater lInflater;
		ArrayList<Driver> objects;
		int maxProgress = 0;
		Paramater param;
		
		DriversAdapter(Context context, ArrayList<Driver> drivers, Paramater param) {
		    ctx = context;
		    objects = drivers;
		    this.param = param;
		    lInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		    
		    for (Driver driver : drivers) {
		    	switch (param) {
				case TRIPS_COUNT:
					if(driver.tripsCount>maxProgress)
						maxProgress = driver.tripsCount;
					break;
				case HARSH_EVENTS:
					if(driver.harshEvents>maxProgress)
						maxProgress = driver.harshEvents;
					break;
				case SCORE:
					if(driver.getScoreAsNumber()>maxProgress)
						maxProgress = driver.getScoreAsNumber();		
					break;
				}
			}
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
		      view = lInflater.inflate(R.layout.compare_item, parent, false);
		    }

		    Driver d = getDriver(position);

		    ((TextView) view.findViewById(R.id.tvName)).setText(d.name);
		    ((ImageView)view.findViewById(R.id.imagePhoto)).setImageResource(d.imageId);
		    
		    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(CompareActivity.this);
		    if(prefs.getInt(Constants.PREF_CURRENT_DRIVER, -1)>0 && prefs.getInt(Constants.PREF_CURRENT_DRIVER, -1)==position)
			    ((TextView) view.findViewById(R.id.tvCurrent)).setVisibility(View.VISIBLE);
		    
		    ProgressBar progress = (ProgressBar)view.findViewById(R.id.progressBar);
		    progress.setMax(maxProgress);
		    
		    switch (param) {
			case TRIPS_COUNT:
			    ((TextView) view.findViewById(R.id.tvParameter)).setText(""+d.tripsCount);
			    progress.setProgress(d.tripsCount);
				break;
			case HARSH_EVENTS:
			    ((TextView) view.findViewById(R.id.tvParameter)).setText(""+d.harshEvents);
			    progress.setProgress(d.harshEvents);
				break;
			case SCORE:
				if(d.score.length()>1){
					((TextView) view.findViewById(R.id.tvParameter)).setText(d.score.substring(0, 1));
					((TextView) view.findViewById(R.id.tvParameterSign)).setText(d.score.substring(1, 2));
					view.findViewById(R.id.tvParameterSign).setVisibility(View.VISIBLE);
				}
				else{
					((TextView) view.findViewById(R.id.tvParameter)).setText(""+d.score);
					view.findViewById(R.id.tvParameterSign).setVisibility(View.INVISIBLE);
				}
			    progress.setProgress(d.getScoreAsNumber());		
				break;
			}
		    	    
		    return view;
		}
		
		Driver getDriver(int position) {
			return ((Driver) getItem(position));
		}
		
	}

}
