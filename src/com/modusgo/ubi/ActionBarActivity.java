package com.modusgo.ubi;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class ActionBarActivity extends android.support.v7.app.ActionBarActivity{
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.action_settings:
	            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

}
