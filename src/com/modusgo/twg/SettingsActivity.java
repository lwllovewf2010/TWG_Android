package com.modusgo.twg;

import android.os.Bundle;
import com.modusgo.twg.R;

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
	public void onResume() {
		super.onResume();
		setNavigationDrawerItemSelected(MenuItems.SETTINGS);
	}
}
