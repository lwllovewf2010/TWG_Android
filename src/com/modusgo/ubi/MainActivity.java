package com.modusgo.ubi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.CrashManagerListener;
import net.hockeyapp.android.FeedbackManager;
import net.hockeyapp.android.UpdateManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    
    SharedPreferences prefs;

    Fragment fragment;
    
    private static final String ATTRIBUTE_NAME_TEXT = "text"; 
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		checkForUpdates();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        
        prefs = getSharedPreferences(getPackageName(), MODE_MULTI_PROCESS);
        
        View v = getLayoutInflater().inflate(R.layout.drawer_list_header, null);
        ((TextView)v.findViewById(R.id.tvName)).setText("John");
        mDrawerList.addHeaderView(v);
        
        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        
        final String[] menuItems = new String[]{"Score","Dashboard","Trips","Comparsion","Alerts","-","Distraction","Limits","Engine"};
        
        ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>(menuItems.length);
        Map<String, Object> m;
        for (int i = 0; i < menuItems.length; i++) {
        	m = new HashMap<String, Object>();
	        m.put(ATTRIBUTE_NAME_TEXT, menuItems[i]);
	        data.add(m);
        }

        String[] from = { ATTRIBUTE_NAME_TEXT};
        int[] to = { R.id.tvText };

        SimpleAdapter sAdapter = new SimpleAdapter(this, data, R.layout.drawer_list_item,
            from, to){
        	@Override
        	public View getView(int position, View convertView,
        			ViewGroup parent) {
        		if(menuItems[position].equals("-"))
        			convertView = getLayoutInflater().inflate(R.layout.drawer_list_item_divider, null);
        		return super.getView(position, convertView, parent);
        	}
        };
        
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(sAdapter);

        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        
        mDrawerLayout.setScrimColor(Color.argb(120, 200, 200, 200));
        
        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  	/* host Activity */
                mDrawerLayout,         	/* DrawerLayout object */
                R.drawable.ic_drawer,  	/* nav drawer image to replace 'Up' caret */
                R.string.app_name,  	/* "open drawer" description for accessibility */
                R.string.app_name  		/* "close drawer" description for accessibility */
                ) {
            public void onDrawerClosed(View view) {
                //getActionBar().setTitle(mTitle);
                //invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                //getActionBar().setTitle(mDrawerTitle);
                //invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        fragment = new MainFragment();
        
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment,"test").commit();

    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
	    return super.onCreateOptionsMenu(menu);
	}
    
    @Override
    protected void onResume() {
    	super.onResume();        
    	overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		checkForCrashes();
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        //boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        //menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         // The action bar home/up action should open or close the drawer.
         // ActionBarDrawerToggle will take care of this.
    	if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
    	else{
    		switch (item.getItemId()) {
		        case R.id.action_settings:
		            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
		            return true;
		        default:
		            return super.onOptionsItemSelected(item);
    		}
    	}
    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        	mDrawerLayout.closeDrawers();
        	switch (position) {
	        case 1:
	        	//Score
	        	getSupportFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
				.replace(R.id.content_frame, new ScoreFragment())
				.addToBackStack(null)
				.commit();
	            break;
	        case 2:
	        	//Dashboard
	            break;
	        case 3:
	        	//Trips
	        	getSupportFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
				.replace(R.id.content_frame, new TripFragment())
				.addToBackStack(null)
				.commit();
	            break;
	        case 4:
	        	//Comparsion
	            break;
	        case 5:
	        	//Alerts
	        	break;
	        case 6:
	        	// - Divider -
	            break;
	        case 7:
	        	//Distraction
	            break;
	        case 8:
	        	//Limits
	            break;
	        case 9:
	        	//Engine
	            break;
        	}
        }
    }

   /* @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }*/

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    
    @Override
    public boolean onKeyDown(int keycode, KeyEvent e) {
        switch(keycode) {
            case KeyEvent.KEYCODE_MENU:
                if(mDrawerLayout.isDrawerOpen(Gravity.LEFT))
                    mDrawerLayout.closeDrawers();
                else
                    mDrawerLayout.openDrawer(Gravity.LEFT);
                return true;
        }

        return super.onKeyDown(keycode, e);
    }
    
	private void checkForCrashes() {
		CrashManager.register(this, Constants.HOCKEY_APP_ID, new CrashManagerListener() {
			@Override
			public boolean shouldAutoUploadCrashes() {
				return true;
			}
		});
	}

	private void checkForUpdates() {
		//TODO Remove this for store builds!
		UpdateManager.register(this, Constants.HOCKEY_APP_ID);
	}
	
	public void showFeedbackActivity() {
		  FeedbackManager.register(this, Constants.HOCKEY_APP_ID, null);
		  FeedbackManager.showFeedbackActivity(this);
		}
}