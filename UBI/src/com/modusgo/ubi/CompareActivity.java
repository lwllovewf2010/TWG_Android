package com.modusgo.ubi;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.modusgo.demo.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class CompareActivity extends MainActivity{
	
	private static enum Paramater { TRIPS_COUNT, TIME, SCORE, DISTANCE};
	
	TabHost tabHost;
    LinearLayout llProgress;
	View previousTab;
	
	ArrayList<Driver> drivers;
	DriversHelper dHelper;
	
	DriversAdapter driverTripsAdapter;
	DriversAdapter driverTimeAdapter;
	DriversAdapter driverScoreAdapter;
	DriversAdapter driverDistanceAdapter;
	
	Calendar cStart;
	Calendar cEnd;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_compare);
		super.onCreate(savedInstanceState);
		
		setActionBarTitle("COMPARE");
		
		dHelper = DriversHelper.getInstance();
		drivers = dHelper.getDrivers();
		
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
				switch (position) {
				case 0:
					cStart.setTimeInMillis(System.currentTimeMillis());
					cStart.set(cStart.get(Calendar.YEAR), cStart.get(Calendar.MONTH), 1, 0, 0);
					cEnd.setTimeInMillis(System.currentTimeMillis());
					break;
				case 1:
					cStart.setTimeInMillis(System.currentTimeMillis());
					cStart.set(cStart.get(Calendar.YEAR), cStart.get(Calendar.MONTH)-1, 1, 0, 0);
					cEnd.set(cStart.get(Calendar.YEAR), cStart.get(Calendar.MONTH)+1,1,23,59);
					break;
				case 2:
					cStart.set(2000, Calendar.JANUARY, 1, 0, 0);
					cEnd.setTimeInMillis(System.currentTimeMillis());
					break;

				default:
					break;
				}
				new GetCompareInfoTask(CompareActivity.this).execute("compare.json");
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		
		llProgress = (LinearLayout) findViewById(R.id.llProgress);
		
		tabHost = (TabHost) findViewById(android.R.id.tabhost);
        tabHost.setup();
        setupTab(new TextView(this), "Score", R.id.tab1);
		setupTab(new TextView(this), "Trips", R.id.tab2);
		setupTab(new TextView(this), "Time", R.id.tab3);
		setupTab(new TextView(this), "Distance", R.id.tab4);
		
		previousTab = tabHost.getCurrentView();
		
        tabHost.setOnTabChangedListener(new OnTabChangeListener() {
        	public void onTabChanged(String tabId) {
        		View currentTab = tabHost.getCurrentView();
        		currentTab.setAnimation(fadeInAnimation());
                previousTab.setAnimation(fadeOutAnimation());
                previousTab = currentTab;
        	}
        });
		
        ListView lvScore = (ListView)findViewById(R.id.tab1);
        driverScoreAdapter = new DriversAdapter(this, Paramater.SCORE);
        lvScore.setAdapter(driverScoreAdapter);
        
        ListView lvTrips = (ListView)findViewById(R.id.tab2);
        driverTripsAdapter = new DriversAdapter(this, Paramater.TRIPS_COUNT);
		lvTrips.setAdapter(driverTripsAdapter);
		
		ListView lvTime = (ListView)findViewById(R.id.tab3);
        driverTimeAdapter = new DriversAdapter(this, Paramater.TIME);
        lvTime.setAdapter(driverTimeAdapter);
        
        ListView lvDistance = (ListView)findViewById(R.id.tab4);
        driverDistanceAdapter = new DriversAdapter(this, Paramater.DISTANCE);
        lvDistance.setAdapter(driverDistanceAdapter);        
		
        
        cStart = Calendar.getInstance();
		cStart.set(cStart.get(Calendar.YEAR), cStart.get(Calendar.MONTH), 1, 0, 0);
		
		cEnd = Calendar.getInstance();
		cEnd.setTimeInMillis(System.currentTimeMillis());
        
		new GetCompareInfoTask(this).execute("compare.json");
	}
	
	private void setupTab(final View view, final String tag, int contentId) {
		View tabview = createTabView(tabHost.getContext(), tag);
	    TabSpec setContent = tabHost.newTabSpec(tag).setIndicator(tabview).setContent(contentId);
	    tabHost.addTab(setContent);
	}

	private static View createTabView(final Context context, final String text) {
		TextView tv = (TextView) LayoutInflater.from(context).inflate(R.layout.compare_tabs_bg, null);
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
		int maxProgress = 0;
		Paramater param;
		
		DriversAdapter(Context context, Paramater param) {
		    ctx = context;
		    this.param = param;
		    lInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		    
		    updateMaxValue();
		}
		
		public void updateMaxValue(){
			for (Driver driver : drivers) {
		    	switch (param) {
				case TRIPS_COUNT:
					if(driver.totalTripsCount>maxProgress)
						maxProgress = driver.totalTripsCount;
					break;
				case TIME:
					if(driver.totalDrivingTime>maxProgress)
						maxProgress = driver.totalDrivingTime;
					break;
				case SCORE:
					if(driver.score>maxProgress)
						maxProgress = driver.score;
					break;
				case DISTANCE:
					if(driver.totalDistance>maxProgress)
						maxProgress = (int)driver.totalDistance;
					break;
				}
			}
		}
		
		@Override
		public void notifyDataSetChanged() {
			updateMaxValue();
			super.notifyDataSetChanged();
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
		      view = lInflater.inflate(R.layout.compare_item, parent, false);
		    }

		    Driver d = getDriver(position);

		    ((TextView) view.findViewById(R.id.tvName)).setText(d.name);
		    ImageView imagePhoto = (ImageView)view.findViewById(R.id.imagePhoto);
		    if(d.imageUrl == null || d.imageUrl.equals(""))
		    	imagePhoto.setImageResource(R.drawable.person_placeholder);
		    else{
		    	DisplayImageOptions options = new DisplayImageOptions.Builder()
		        .showImageOnLoading(R.drawable.person_placeholder)
		        .showImageForEmptyUri(R.drawable.person_placeholder)
		        .showImageOnFail(R.drawable.person_placeholder)
		        .cacheInMemory(true)
		        .cacheOnDisk(true)
		        .build();
		    	
		    	ImageLoader.getInstance().displayImage(d.imageUrl, imagePhoto, options);
		    }
		    
		    ProgressBar progress = (ProgressBar)view.findViewById(R.id.progressBar);
		    progress.setMax(maxProgress);
		    
		    switch (param) {
			case TRIPS_COUNT:
			    ((TextView) view.findViewById(R.id.tvParameter)).setText(""+d.totalTripsCount);
			    progress.setProgress(d.totalTripsCount);
				break;
			case TIME:
			    ((TextView) view.findViewById(R.id.tvParameter)).setText(""+d.totalDrivingTime);
			    progress.setProgress(d.totalDrivingTime);
				break;
			case SCORE:
				((TextView) view.findViewById(R.id.tvParameter)).setText(""+d.score);
			    progress.setProgress(d.score);		
				break;
			case DISTANCE:
				DecimalFormat df = new DecimalFormat("0.0");
				((TextView) view.findViewById(R.id.tvParameter)).setText(""+df.format(d.totalDistance));
			    progress.setProgress((int)d.totalDistance);		
				break;
			}
		    	    
		    return view;
		}
		
		Driver getDriver(int position) {
			return ((Driver) getItem(position));
		}
		
	}
	
	class GetCompareInfoTask extends BaseRequestAsyncTask{

		public GetCompareInfoTask(Context context) {
			super(context);
		}
		
		@Override
		protected void onPreExecute() {
			llProgress.setVisibility(View.VISIBLE);
			tabHost.setVisibility(View.GONE);
			super.onPreExecute();
		}
		
		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			llProgress.setVisibility(View.GONE);
			tabHost.setVisibility(View.VISIBLE);
		}

		@Override
		protected JSONObject doInBackground(String... params) {
			SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.US);
			
	        requestParams.add(new BasicNameValuePair("page", "1"));
	        requestParams.add(new BasicNameValuePair("per_page", "1000"));
	        requestParams.add(new BasicNameValuePair("start_time", sdf.format(cStart.getTime())));
	        requestParams.add(new BasicNameValuePair("end_time", sdf.format(cEnd.getTime())));
			
	        return super.doInBackground(params);
		}
		
		@Override
		protected void onSuccess(JSONObject responseJSON) throws JSONException {

			JSONArray compareJSON = responseJSON.getJSONArray("compare");
			
			for (int i = 0; i < compareJSON.length(); i++) {
				JSONObject driverJSON = compareJSON.getJSONObject(i);
				Driver d = dHelper.getDriverById(driverJSON.getLong("id"));
				d.totalDistance = driverJSON.getDouble("mileage");
				d.totalTripsCount = driverJSON.getInt("number_of_trips");
				d.score = driverJSON.getInt("drive_score");
				d.totalDrivingTime = driverJSON.getInt("driving_time");
				
				dHelper.setDriverById(d.id, d);
			}
			
			drivers = dHelper.getDrivers();
			
			driverScoreAdapter.notifyDataSetChanged();
			driverTripsAdapter.notifyDataSetChanged();
			driverTimeAdapter.notifyDataSetChanged();
			driverDistanceAdapter.notifyDataSetChanged();

			super.onSuccess(responseJSON);
		}
	}

}
