package com.modusgo.ubi;

import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;

public class DriverActivity extends MainActivity{
	
	private FragmentTabHost mTabHost;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_driver);
		super.onCreate(savedInstanceState);
		
		mTabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
        mTabHost.setup(getApplicationContext(), getSupportFragmentManager(), R.id.realtabcontent);

        Bundle b = new Bundle();
		b.putInt("id", getIntent().getIntExtra("id", 0));
        mTabHost.addTab(mTabHost.newTabSpec("tab1").setIndicator("Driver"), DriverDetailsFragment.class, b);
        mTabHost.addTab(mTabHost.newTabSpec("tab2").setIndicator("Trips"), TripsFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("tab3").setIndicator("Score"), ScoreFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("tab4").setIndicator("Diagnostics"), DiagnosticsFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("tab5").setIndicator("Limits"), LimitsFragment.class, null);
	}

}
