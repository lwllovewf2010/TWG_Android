package com.modusgo.ubi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.CrashManagerListener;
import net.hockeyapp.android.FeedbackManager;
import net.hockeyapp.android.UpdateManager;
import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class MainActivity extends FragmentActivity {
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private int mDrawerSelectedItem = -1;
    protected ImageButton btnUp;
    private ImageButton btnNavigationDrawer; 
    
    public static enum MenuItems {HOME("HOME",1), COMPARE("COMPARE",2), SETTINGS("SETTINGS",3), 
    	CALLSUPPORT("CALL SUPPORT",4), AGENT("AGENT",5), LOGOUT("LOGOUT",6); 
	    private MenuItems(final String text, final int num) {
	        this.text = text;
	        this.num = num;
	    }
	
	    private final String text;
	    private final int num;
		
	    @Override
	    public String toString() {
	        return text;
	    }
	    
	    public int toInt() {
	        return num;
	    }
	    
	    public String[] valuesString(){
	    	return null;
	    }
    };
    
    SharedPreferences prefs;
    
    private static final String ATTRIBUTE_NAME_TEXT = "text"; 
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
		checkForUpdates();

		final ActionBar actionBar = getActionBar();
	    actionBar.setDisplayShowHomeEnabled(false);
	    actionBar.setDisplayShowTitleEnabled(true);
	    actionBar.setDisplayShowCustomEnabled(true);
	    actionBar.setCustomView(R.layout.action_bar);
	    
	    btnNavigationDrawer = (ImageButton)actionBar.getCustomView().findViewById(R.id.btnMenu);
	    btnNavigationDrawer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				 if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
					 mDrawerLayout.closeDrawer(Gravity.RIGHT);
		         } else {
		        	 mDrawerLayout.openDrawer(Gravity.RIGHT);
		         }
			}
		});
	    
	    btnUp = (ImageButton)actionBar.getCustomView().findViewById(R.id.btnUp);
	    btnUp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				up();
			}
		});
	    
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        final MenuItems[] menuItemsArray = MenuItems.values();
        //final String[] menuItems = new String[]{"Score","Dashboard","Trips","Comparsion","Alerts","Distraction","Limits","Engine"};
        
        ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>(menuItemsArray.length);
        Map<String, Object> m;
        for (int i = 0; i < menuItemsArray.length; i++) {
        	m = new HashMap<String, Object>();
	        m.put(ATTRIBUTE_NAME_TEXT, menuItemsArray[i].toString());
	        data.add(m);
        }

        String[] from = { ATTRIBUTE_NAME_TEXT};
        int[] to = { R.id.tvText };

        SimpleAdapter sAdapter = new SimpleAdapter(this, data, R.layout.drawer_list_item,
            from, to){
        	@Override
        	public View getView(int position, View convertView,	ViewGroup parent) {
        		View rowView = getLayoutInflater().inflate(R.layout.drawer_list_item, parent, false);
        		TextView textView = ((TextView)rowView.findViewById(R.id.tvText));
        		textView.setText(menuItemsArray[position].toString());
        		
        		if(position==MenuItems.LOGOUT.num-1)
        			textView.setTextColor(getResources().getColor(R.color.orange));
        		
        		if(position==MenuItems.AGENT.num-1)
        			((ImageView)rowView.findViewById(R.id.imageIcon)).setImageResource(R.drawable.ic_external_link);
        		else
        			((ImageView)rowView.findViewById(R.id.imageIcon)).setVisibility(View.GONE);
        		
        		/*if( ((HashMap<?, ?>)getItem(position)).get("text").equals("Alerts") ){
        			rowView.findViewById(R.id.divider).setVisibility(View.VISIBLE);
    			}*/
        		
        		return rowView;
        	}
        };
        
        
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(sAdapter);

        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        
        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

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
            	getActionBar().getCustomView().findViewById(R.id.tvTitle).setVisibility(View.VISIBLE);
            	getActionBar().getCustomView().findViewById(R.id.imageLogo).setVisibility(View.GONE);
            	btnNavigationDrawer.setImageResource(R.drawable.ic_menu);
                //getActionBar().setTitle(mTitle);
                //invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
            	getActionBar().getCustomView().findViewById(R.id.tvTitle).setVisibility(View.GONE);
            	getActionBar().getCustomView().findViewById(R.id.imageLogo).setVisibility(View.VISIBLE);
            	btnNavigationDrawer.setImageResource(R.drawable.ic_menu_close);
                //getActionBar().setTitle(mDrawerTitle);
                //invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }
    
    public void setActionBarTitle(String title){
	    ((TextView)getActionBar().getCustomView().findViewById(R.id.tvTitle)).setText(title);
    }
    
    public void setButtonUpVisibility(boolean visible){
    	if(visible)
    		btnUp.setVisibility(View.VISIBLE);
    	else
    		btnUp.setVisibility(View.GONE);
    }
    
    public void up(){
    	NavUtils.navigateUpFromSameTask(this);
    }
    
    public void setButtonNavigationDrawerVisibility(boolean visible){
    	if(visible)
    		btnNavigationDrawer.setVisibility(View.VISIBLE);
    	else
    		btnNavigationDrawer.setVisibility(View.GONE);
    }
    
    @Override
    protected void onResume() {
    	super.onResume();        
    	//overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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
        if (item != null && item.getItemId() == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                mDrawerLayout.closeDrawer(Gravity.RIGHT);
            } else {
                mDrawerLayout.openDrawer(Gravity.RIGHT);
            }
        }
        return false;
    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        	System.out.println("pos: "+position+" checked: "+mDrawerList.getCheckedItemPosition());
        	System.out.println(mDrawerList.isItemChecked(1));
        	if(position!=mDrawerSelectedItem){
	        	boolean changeSelectedItem = false;
        		switch (position) {
		        case 0:
		        	//Home
		        	changeSelectedItem = true;
		        	startActivity(new Intent(MainActivity.this, HomeActivity.class));
//		        	getFragmentManager().beginTransaction()
//					.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
//					.replace(R.id.content_frame, homeFragment)
//					.addToBackStack(null)
//					.commit();
		            break;
		        case 1:
		        	//Compare
		        	changeSelectedItem = true;
		        	startActivity(new Intent(MainActivity.this, CompareActivity.class));
//		        	getFragmentManager().beginTransaction()
//					.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
//					.replace(R.id.content_frame, new CompareFragment())
//					.addToBackStack("testt")
//					.commit();
		            break;
		        case 2:
		        	//Settings
		        	changeSelectedItem = true;
		        	startActivity(new Intent(MainActivity.this, SettingsActivity.class));
		        	/*getSupportFragmentManager().beginTransaction()
					.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
					.replace(R.id.content_frame, new TripFragment())
					.addToBackStack(null)
					.commit();*/
		            break;
		        case 3:
		        	//Call support
		            break;
		        case 4:
		        	//Agent
		        	break;
		        case 5:
		        	//Logout
		        	prefs.edit().putString(Constants.PREF_AUTH_KEY, "").commit();
		        	finish();
		            break;
	        	}
	        	
        		if(changeSelectedItem){
        			mDrawerSelectedItem = position;
        		}
    	        mDrawerList.setItemChecked(mDrawerSelectedItem, true);        			
        		mDrawerLayout.closeDrawers();
        	}
        }
    }
    
    /**
     * 
     * @param item MenuItem num
     */
    public void setNavigationDrawerItemSelected(MenuItems item){
    	mDrawerSelectedItem = item.num-1;
    	mDrawerList.setItemChecked(item.num-1, true);
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
                if(mDrawerLayout.isDrawerOpen(Gravity.RIGHT))
                    mDrawerLayout.closeDrawers();
                else
                    mDrawerLayout.openDrawer(Gravity.RIGHT);
                return true;
        }

        return super.onKeyDown(keycode, e);
    }
    
    @Override
	public void onBackPressed() {
	    if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            mDrawerLayout.closeDrawers();
	        return;
	    }
	
	    super.onBackPressed();
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