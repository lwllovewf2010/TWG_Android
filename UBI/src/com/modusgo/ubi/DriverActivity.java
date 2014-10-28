package com.modusgo.ubi;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTabHost;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnCloseListener;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnOpenListener;
import com.modusgo.demo.R;
import com.modusgo.ubi.db.DbHelper;
import com.modusgo.ubi.db.VehicleContract.VehicleEntry;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class DriverActivity extends MainActivity{

	public static final String SAVED_DRIVER = "driver";
	
	private FragmentTabHost tabHost;
	SlidingMenu menu;

	public Driver driver;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_driver);
		super.onCreate(savedInstanceState);
		
		tabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
        tabHost.setup(getApplicationContext(), getSupportFragmentManager(), R.id.realtabcontent);
        tabHost.addOnAttachStateChangeListener(new OnAttachStateChangeListener() {

            @Override
            public void onViewDetachedFromWindow(View v) {}

            @Override
            public void onViewAttachedToWindow(View v) {
            	tabHost.getViewTreeObserver().removeOnTouchModeChangeListener(tabHost);
            }
        });

        Bundle b = new Bundle();
		b.putLong("id", getIntent().getLongExtra(VehicleEntry._ID, 0));
		
		driver = getDriverFromDB(getIntent().getLongExtra(VehicleEntry._ID, 0));
		
		setupTab(DriverDetailsFragment.class, b, "Driver Detail", R.drawable.ic_tab_driver, 0);
		setupTab(TripsFragment.class, b, "Trips", R.drawable.ic_tab_trips, 0);
		setupTab(ScoreFragment.class, b, "Score", driver.grade, 0);
		setupTab(DiagnosticsFragment.class, b, "Diagnostics", R.drawable.ic_tab_diagnostics, 0);
		setupTab(LimitsFragment.class, b, "Limits", R.drawable.ic_tab_limits, 0);
		
		menu = new SlidingMenu(this);
        menu.setMode(SlidingMenu.LEFT);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        menu.setBehindOffsetRes(R.dimen.drivers_menu_offset);
        menu.setFadeDegree(0.35f);
        menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        menu.setMenu(R.layout.switch_driver_menu);
        menu.setOnOpenListener(new OnOpenListener() {
			@Override
			public void onOpen() {
				setButtonUpVisibility(false);
				setButtonNavigationDrawerVisibility(false);
			}
		});
        menu.setOnCloseListener(new OnCloseListener() {
			@Override
			public void onClose() {
				setButtonUpVisibility(true);
				setButtonNavigationDrawerVisibility(true);			
			}
		});
        
        ListView lvDrivers = (ListView)menu.findViewById(R.id.listViewDrivers);
		
		DriversAdapter driversAdapter = new DriversAdapter(this, DriversHelper.getInstance().drivers);
		
		lvDrivers.setAdapter(driversAdapter);
		
	}
	
	@Override
	protected void onResume() {
		setNavigationDrawerItemsUnselected();
		super.onResume();
	}
	
	public void switchTab(int index){
		tabHost.setCurrentTab(index);
	}
	
	private Driver getDriverFromDB(long id){
		DbHelper dbHelper = DbHelper.getInstance(this);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = db.query(VehicleEntry.TABLE_NAME, 
				new String[]{
				VehicleEntry._ID,
				VehicleEntry.COLUMN_NAME_DRIVER_NAME,
				VehicleEntry.COLUMN_NAME_DRIVER_MARKER_ICON,
				VehicleEntry.COLUMN_NAME_DRIVER_PHOTO,
				VehicleEntry.COLUMN_NAME_CAR_MAKE,
				VehicleEntry.COLUMN_NAME_CAR_MODEL,
				VehicleEntry.COLUMN_NAME_CAR_YEAR,
				VehicleEntry.COLUMN_NAME_CAR_VIN,
				VehicleEntry.COLUMN_NAME_CAR_FUEL,
				VehicleEntry.COLUMN_NAME_CAR_CHECKUP,
				VehicleEntry.COLUMN_NAME_LAST_TRIP_DATE,
				VehicleEntry.COLUMN_NAME_LAST_TRIP_ID,
				VehicleEntry.COLUMN_NAME_ALERTS,
				VehicleEntry.COLUMN_NAME_LATITUDE,
				VehicleEntry.COLUMN_NAME_LONGITUDE,
				VehicleEntry.COLUMN_NAME_ADDRESS,
				VehicleEntry.COLUMN_NAME_GRADE,
				VehicleEntry.COLUMN_NAME_SCORE,
			    VehicleEntry.COLUMN_NAME_TOTAL_TRIPS_COUNT,
			    VehicleEntry.COLUMN_NAME_TOTAL_DRIVING_TIME,
			    VehicleEntry.COLUMN_NAME_TOTAL_DISTANCE,
			    VehicleEntry.COLUMN_NAME_TOTAL_BREAKING,
			    VehicleEntry.COLUMN_NAME_TOTAL_ACCELERATION,
			    VehicleEntry.COLUMN_NAME_TOTAL_SPEEDING,
			    VehicleEntry.COLUMN_NAME_TOTAL_SPEEDING_DISTANCE}, 
				VehicleEntry._ID+" = ?", new String[]{Long.toString(id)}, null, null, null);
		
		Driver d = new Driver();
		
		if(c.moveToFirst()){
			d.id = c.getLong(0);
			d.name = c.getString(1);
			d.markerIcon = c.getString(2);
			d.photo = c.getString(3);
			d.carMake = c.getString(4);
			d.carModel = c.getString(5);
			d.carYear = c.getString(6);
			d.carVIN = c.getString(7);
			d.carFuelLevel = c.getInt(8);
			d.carCheckup = c.getInt(9) == 1;
			d.lastTripDate = c.getString(10);
			d.lastTripId = c.getLong(11);
			d.alerts = c.getInt(12);
			d.latitude = c.getLong(13);
			d.longitude = c.getLong(14);
			d.address = c.getString(15);
			d.grade = c.getString(16);
			d.score = c.getInt(17);
			d.totalTripsCount = c.getInt(18);
			d.totalDrivingTime = c.getInt(19);
			d.totalDistance = c.getDouble(20);
			d.totalBraking = c.getInt(21);
			d.totalAcceleration = c.getInt(22);
			d.totalSpeeding = c.getInt(23);
			d.totalSpeedingDistance = c.getDouble(24);
				
		}
		c.close();
		db.close();
		dbHelper.close();
		return d;
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
		      view = lInflater.inflate(R.layout.switch_driver_item, parent, false);
		    }

		    Driver d = getDriver(position);
		    
		    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(DriverActivity.this);
		    if(prefs.getInt(Constants.PREF_CURRENT_DRIVER, -1)>=0 && prefs.getInt(Constants.PREF_CURRENT_DRIVER, -1)==position){
		    	
		    	Spannable span = new SpannableString(Html.fromHtml("<font size=\"10px\" color=\"#3c454f\" face=\"fonts/EncodeSansNormal-600-SemiBold.ttf\">CURRENT</font><br>"+d.name));
		    	span.setSpan(new RelativeSizeSpan(0.8f), 0, 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		    	
		    	((TextView) view.findViewById(R.id.tvName)).setText(span);
		    }else
		    	((TextView) view.findViewById(R.id.tvName)).setText(d.name);
		    
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
		    
		    view.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					if(prefs.getInt(Constants.PREF_CURRENT_DRIVER, 0)!=position){
						prefs.edit().putInt(Constants.PREF_CURRENT_DRIVER, position).commit();
						menu.toggle();
						finish();
						
						Intent i = new Intent(DriverActivity.this, DriverActivity.class);
						i.putExtra("id", position);
						startActivity(i);
					}
					else{
						menu.toggle();
					}
					
				}
			});
		    
		    return view;
		}
		
		Driver getDriver(int position) {
			return ((Driver) getItem(position));
		}
		
	}
	
	private void setupTab(Class<?> c, Bundle b, final String tag, int imageResId, int counter) {
		View tabview = createTabView(tabHost.getContext(), tag, imageResId, counter);
	    TabSpec setContent = tabHost.newTabSpec(tag).setIndicator(tabview);
	    tabHost.addTab(setContent, c, b);
	}
	
	private void setupTab(Class<?> c, Bundle b, final String tag, String title, int counter) {
		View tabview = createTabView(tabHost.getContext(), tag, title.equals("") ? "C" : title, counter);
	    TabSpec setContent = tabHost.newTabSpec(tag).setIndicator(tabview);
	    tabHost.addTab(setContent, c, b);
	}

	private static View createTabView(final Context context, final String text, int imageResId, int counter) {
		View rootView = LayoutInflater.from(context).inflate(R.layout.driver_tabs_bg, null);
		TextView tv = (TextView)rootView.findViewById(R.id.tabsText);
		tv.setText(text);
		ImageView icon = (ImageView)rootView.findViewById(R.id.imageIcon);
		icon.setImageResource(imageResId);
		if(counter>0){
			TextView tvCounter = (TextView)rootView.findViewById(R.id.tvCounter);
			tvCounter.setVisibility(View.VISIBLE);
			tvCounter.setText(""+counter);
		}
		return rootView;
	}
	
	private static View createTabView(final Context context, final String text, String title, int counter) {
		View rootView = LayoutInflater.from(context).inflate(R.layout.driver_tabs_bg, null);
		TextView tv = (TextView)rootView.findViewById(R.id.tabsText);
		tv.setText(text);
		ImageView icon = (ImageView)rootView.findViewById(R.id.imageIcon);
		icon.setVisibility(View.GONE);
		TextView tvTitle = (TextView)rootView.findViewById(R.id.tvTitle);
		tvTitle.setText(title);
		tvTitle.setVisibility(View.VISIBLE);
		
		if(counter>0){
			TextView tvCounter = (TextView)rootView.findViewById(R.id.tvCounter);
			tvCounter.setVisibility(View.VISIBLE);
			tvCounter.setText(""+counter);
		}
		return rootView;
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
	public void onBackPressed() {
	    if (menu.isMenuShowing()) {
            menu.toggle();
	        return;
	    }
	
	    super.onBackPressed();
	}
}
