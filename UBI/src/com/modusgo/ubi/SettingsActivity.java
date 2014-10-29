package com.modusgo.ubi;

import android.os.Bundle;


public class SettingsActivity extends MainActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_settings);
		super.onCreate(savedInstanceState);

		setActionBarTitle("SETTINGS");
		
		SettingsViewFragment svf = new SettingsViewFragment();
		
		getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, svf).commit();
	}
	
	@Override
	public void up() {
		prefs.edit().putBoolean(SettingsEditFragment.PREF_JUSTSAVED, false).commit();
		super.up();
	}

	@Override
	public void onResume() {
		setNavigationDrawerItemSelected(MenuItems.SETTINGS);
		super.onResume();
	}
}
