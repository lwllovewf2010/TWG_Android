package com.modusgo.ubi;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TabHost.TabSpec;
import android.widget.ImageView;
import android.widget.TextView;

public class DriverActivity extends MainActivity{
	
	private FragmentTabHost tabHost;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_driver);
		super.onCreate(savedInstanceState);
		
		tabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
        tabHost.setup(getApplicationContext(), getSupportFragmentManager(), R.id.realtabcontent);

        Bundle b = new Bundle();
		b.putInt("id", getIntent().getIntExtra("id", 0));
		
		setupTab(DriverDetailsFragment.class, b, "Driver", R.drawable.ic_tab_trips, 0);
		setupTab(TripsFragment.class, b, "Trips", R.drawable.ic_tab_trips, 3);
		setupTab(ScoreFragment.class, b, "Score", R.drawable.ic_tab_score, 0);
		setupTab(DiagnosticsFragment.class, null, "Diagnostics", R.drawable.ic_tab_diagnostics, 0);
		setupTab(LimitsFragment.class, b, "Limits", R.drawable.ic_tab_limits, 0);
		
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

}
