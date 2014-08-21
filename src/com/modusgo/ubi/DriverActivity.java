package com.modusgo.ubi;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTabHost;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
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

public class DriverActivity extends MainActivity{

	public static final String SAVED_DRIVER = "driver";
	
	private FragmentTabHost tabHost;
	SlidingMenu menu;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_driver);
		super.onCreate(savedInstanceState);
		
		tabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
        tabHost.setup(getApplicationContext(), getSupportFragmentManager(), R.id.realtabcontent);

        Bundle b = new Bundle();
		b.putInt("id", getIntent().getIntExtra("id", 0));
		
		Bundle bDriver = new Bundle();
		bDriver.putSerializable(SAVED_DRIVER, getIntent().getSerializableExtra(SAVED_DRIVER));
		
		setupTab(DriverDetailsFragment.class, bDriver, "Driver", R.drawable.ic_tab_driver, 0);
		setupTab(TripsFragment.class, b, "Trips", R.drawable.ic_tab_trips, 3);
		setupTab(ScoreFragment.class, b, "Score", R.drawable.ic_tab_score, 0);
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
		
		DriversAdapter driversAdapter = new DriversAdapter(this, DbHelper.getDrivers());
		
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
		    
		    ((ImageView)view.findViewById(R.id.imagePhoto)).setImageResource(d.imageId);
		    
		    view.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					prefs.edit().putInt(Constants.PREF_CURRENT_DRIVER, position).commit();
					menu.toggle();
					finish();
					
					Intent i = new Intent(DriverActivity.this, DriverActivity.class);
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
	
	private void setupTab(Class<?> c, Bundle b, final String tag, int imageResId, int counter) {
		View tabview = createTabView(tabHost.getContext(), tag, imageResId, counter);
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
