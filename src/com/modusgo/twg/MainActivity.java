package com.modusgo.twg;

import java.util.ArrayList;
import java.util.Locale;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.CrashManagerListener;
import net.hockeyapp.android.FeedbackManager;
import net.hockeyapp.android.UpdateManager;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.modusgo.dd.LocationService;
import com.modusgo.twg.R;
import com.modusgo.twg.db.VehicleContract.VehicleEntry;
import com.modusgo.twg.utils.Utils;
import com.modusgo.twg.utils.Vehicle;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class MainActivity extends FragmentActivity {
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private int mDrawerSelectedItem = -1;
	protected ImageButton btnUp;
	protected TextView tvActionBarTitle;
	private ImageButton btnNavigationDrawer;
	private String actionBarTitle = "";
	public Vehicle vehicle;
	public ProgressDialog busyDialog = null;
	
	public FragmentTabHost tabHost;

	public static enum MenuItems {
		HOME("HOME", 0),
//		COMPARE("COMPARE", 1),
		CALLSUPPORT("CALLSUPPORT", 2), 
//		AGENT("AGENT", 3), 
		FEEDBACK("FEEDBACK", 4), 
		FINDAMECHANIC("FACILITIES", 5), 
		SETTINGS("SETTINGS", 6), 
//		DRIVERSETUP("DRIVER SETUP", 7), 
		LOGOUT("LOGOUT", 8),
		EXIT("EXIT", 9);
		
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

		public String[] valuesString() {
			return null;
		}
	};

	ArrayList<MenuItems> menuItems;

	SharedPreferences prefs;
	ActionBar actionBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_main);
		
		//Are we exiting
		 if (getIntent().getBooleanExtra("EXIT", false)) {
	         this.finishAffinity();
	         return;
	    }
		 
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		//Determine screen size
//		if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) {
//		    Toast.makeText(this, "Large screen", Toast.LENGTH_LONG).show();
//		}
//		else if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL) {
//		    Toast.makeText(this, "Normal sized screen", Toast.LENGTH_LONG).show();
//		}
//		else if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL) {
//		    Toast.makeText(this, "Small sized screen", Toast.LENGTH_LONG).show();
//		}
//		else {
//		    Toast.makeText(this, "Screen size is neither large, normal or small", Toast.LENGTH_LONG).show();
//		}
		//Determine density
//		DisplayMetrics metrics = new DisplayMetrics();
//		getWindowManager().getDefaultDisplay().getMetrics(metrics);
//		int density = metrics.densityDpi;
//
//		if (density == DisplayMetrics.DENSITY_XXHIGH) {
//		    Toast.makeText(this, "DENSITY_XXHIGH... Density is " + String.valueOf(density), Toast.LENGTH_LONG).show();
//		}
//		else if (density == DisplayMetrics.DENSITY_XHIGH) {
//		    Toast.makeText(this, "DENSITY_XHIGH... Density is " + String.valueOf(density), Toast.LENGTH_LONG).show();
//		}
//		else if (density == DisplayMetrics.DENSITY_HIGH) {
//		    Toast.makeText(this, "DENSITY_HIGH... Density is " + String.valueOf(density), Toast.LENGTH_LONG).show();
//		}
//		else if (density == DisplayMetrics.DENSITY_MEDIUM) {
//		    Toast.makeText(this, "DENSITY_MEDIUM... Density is " + String.valueOf(density), Toast.LENGTH_LONG).show();
//		}
//		else if (density == DisplayMetrics.DENSITY_LOW) {
//		    Toast.makeText(this, "DENSITY_LOW... Density is " + String.valueOf(density), Toast.LENGTH_LONG).show();
//		}
//		else {
//		    Toast.makeText(this, "Density is neither HIGH, MEDIUM OR LOW.  Density is " + String.valueOf(density), Toast.LENGTH_LONG).show();
//		}
		
		actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setCustomView(R.layout.action_bar);

		tvActionBarTitle = (TextView) actionBar.getCustomView().findViewById(
				R.id.tvTitle);

		btnNavigationDrawer = (ImageButton) actionBar.getCustomView()
				.findViewById(R.id.btnMenu);
		btnNavigationDrawer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
					mDrawerLayout.closeDrawer(Gravity.RIGHT);
				} else {
					mDrawerLayout.openDrawer(Gravity.RIGHT);
					Utils.gaTrackScreen(MainActivity.this, "Menu");
				}
			}
		});

		btnUp = (ImageButton) actionBar.getCustomView()
				.findViewById(R.id.btnUp);
		btnUp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				up();
			}
		});

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		MenuItems[] menuItemsArray = MenuItems.values();
		menuItems = new ArrayList<MenuItems>();

		// menuItems.add(MenuItems.HOME);
		menuItems.add(MenuItems.FEEDBACK);
//		menuItems.add(MenuItems.COMPARE);
		menuItems.add(MenuItems.FINDAMECHANIC);
		menuItems.add(MenuItems.SETTINGS);
//		menuItems.add(MenuItems.CALLSUPPORT);
//		menuItems.add(MenuItems.AGENT);
		// menuItems.add(MenuItems.DRIVERSETUP);
		menuItems.add(MenuItems.LOGOUT);
		menuItems.add(MenuItems.EXIT);

		ArrayAdapter<MenuItems> adapter = new ArrayAdapter<MenuItems>(this,
				R.layout.drawer_list_item, menuItemsArray) {

			@Override
			public int getCount() {
				return menuItems.size();
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				ViewHolder holder;

				View rowView = convertView;
				if (rowView == null) {
					rowView = getLayoutInflater().inflate(
							R.layout.drawer_list_item, parent, false);
					holder = new ViewHolder();
					holder.tvTitle = (TextView) rowView
							.findViewById(R.id.tvText);
					holder.imageIcon = (ImageView) rowView
							.findViewById(R.id.imageIcon);
					rowView.setTag(holder);
				} else {
					holder = (ViewHolder) rowView.getTag();
				}
				holder.tvTitle.setText(menuItems.get(position).text);

				if (menuItems.get(position).equals(MenuItems.LOGOUT))
					holder.tvTitle.setTextColor(getResources().getColor(R.color.orange));

				// if(menuItems.get(position).equals(MenuItems.AGENT))
				// holder.imageIcon.setImageResource(R.drawable.ic_external_link);
				// else
				holder.imageIcon.setVisibility(View.GONE);

				/*
				 * if( ((HashMap<?,
				 * ?>)getItem(position)).get("text").equals("Alerts") ){
				 * rowView.
				 * findViewById(R.id.divider).setVisibility(View.VISIBLE); }
				 */

				return rowView;
			}
		};

		// set up the drawer's list view with items and click listener
		mDrawerList.setAdapter(adapter);

		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		// enable ActionBar app icon to behave as action to toggle nav drawer
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		if (!ImageLoader.getInstance().isInited()) {
			ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
					getApplicationContext()).build();
			ImageLoader.getInstance().init(config);
		}

		final ImageView menuLogo = (ImageView) actionBar.getCustomView()
				.findViewById(R.id.imageLogo);
		DisplayImageOptions options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.logo_menu)
				.showImageForEmptyUri(R.drawable.logo_menu)
				.showImageOnFail(R.drawable.logo_menu).cacheInMemory(true)
				.cacheOnDisk(true).build();

		ImageLoader.getInstance().displayImage(
				prefs.getString(Constants.PREF_BR_MENU_LOGO, ""), menuLogo,
				options);
		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
		mDrawerLayout, /* DrawerLayout object */
		R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
		R.string.app_name, /* "open drawer" description for accessibility */
		R.string.app_name /* "close drawer" description for accessibility */
		) {
			public void onDrawerClosed(View view) {
				getActionBar().getCustomView().findViewById(R.id.tvTitle)
						.setVisibility(View.VISIBLE);
				menuLogo.setVisibility(View.GONE);
				btnNavigationDrawer.setImageResource(R.drawable.ic_menu);
				// getActionBar().setTitle(mTitle);
				// invalidateOptionsMenu(); // creates call to
				// onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().getCustomView().findViewById(R.id.tvTitle)
						.setVisibility(View.GONE);
				menuLogo.setVisibility(View.VISIBLE);
				btnNavigationDrawer.setImageResource(R.drawable.ic_menu_close);
				// getActionBar().setTitle(mDrawerTitle);
				// invalidateOptionsMenu(); // creates call to
				// onPrepareOptionsMenu()
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
	}

	private class ViewHolder {
		public TextView tvTitle;
		public ImageView imageIcon;
	}

	public void setActionBarTitle(String title) {
		actionBarTitle = title;
		((TextView) getActionBar().getCustomView().findViewById(R.id.tvTitle))
				.setText(title);
	}

	public void setButtonUpVisibility(boolean visible) {
		if (visible)
			btnUp.setVisibility(View.VISIBLE);
		else
			btnUp.setVisibility(View.GONE);
	}

	public void up() {
		NavUtils.navigateUpFromSameTask(this);
	}

	public void setButtonNavigationDrawerVisibility(boolean visible) {
		if (visible)
			btnNavigationDrawer.setVisibility(View.VISIBLE);
		else
			btnNavigationDrawer.setVisibility(View.GONE);
	}

	protected void setActionBarAppearance() {
		String titleBarBgURL = prefs.getString(Constants.PREF_BR_TITLE_BAR_BG,
				"");
		if (!titleBarBgURL.equals("")) {
			System.out.println("action bar color img: " + titleBarBgURL);
			DisplayImageOptions options = new DisplayImageOptions.Builder()
					.cacheInMemory(true).cacheOnDisk(true).build();
			ImageLoader.getInstance().loadImage(
					prefs.getString(Constants.PREF_BR_TITLE_BAR_BG, ""),
					options, new SimpleImageLoadingListener() {
						@Override
						public void onLoadingComplete(String imageUri,
								View view, Bitmap loadedImage) {
							getActionBar().getCustomView()
									.setBackgroundDrawable(
											new BitmapDrawable(getResources(),
													loadedImage));
							super.onLoadingComplete(imageUri, view, loadedImage);
						}
					});
		} else {
			getActionBar().getCustomView().setBackgroundColor(
					Color.parseColor(prefs.getString(
							Constants.PREF_BR_TITLE_BAR_BG_COLOR,
							Constants.TITLE_BAR_BG_COLOR)));
			System.out.println("action bar color: "
					+ Constants.TITLE_BAR_BG_COLOR);
		}

		try {
			tvActionBarTitle.setTextColor(Color.parseColor(prefs.getString(
					Constants.PREF_BR_TITLE_BAR_TEXT_COLOR,
					Constants.TITLE_BAR_TEXT_COLOR)));
			Mode mMode = Mode.SRC_ATOP;
			getResources().getDrawable(R.drawable.ic_arrow_left)
					.setColorFilter(
							Color.parseColor(prefs.getString(
									Constants.PREF_BR_TITLE_BAR_BUTTONS_COLOR,
									Constants.TITLE_BAR_BUTTONS_COLOR)), mMode);
			getResources().getDrawable(R.drawable.ic_menu).setColorFilter(
					Color.parseColor(prefs.getString(
							Constants.PREF_BR_TITLE_BAR_BUTTONS_COLOR,
							Constants.TITLE_BAR_BUTTONS_COLOR)), mMode);
			getResources().getDrawable(R.drawable.ic_menu_close)
					.setColorFilter(
							Color.parseColor(prefs.getString(
									Constants.PREF_BR_TITLE_BAR_BUTTONS_COLOR,
									Constants.TITLE_BAR_BUTTONS_COLOR)), mMode);
			getResources().getDrawable(R.drawable.ic_map).setColorFilter(
					Color.parseColor(prefs.getString(
							Constants.PREF_BR_TITLE_BAR_BUTTONS_COLOR,
							Constants.TITLE_BAR_BUTTONS_COLOR)), mMode);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * showBusyDialog
	 * 
	 * @param resId
	 */
	public void showBusyDialog(final int resId)
	{
		final MainActivity	main = (MainActivity)this;

		runOnUiThread(new Runnable() {

			@Override
			public void run() {

				busyDialog = new ProgressDialog(main);
				busyDialog.setMessage(getResources().getString(resId));
				busyDialog.setCancelable(false);
				busyDialog.show();
			}
		});
	}
	
	public void hideBusyDialog()
	{
		if(busyDialog != null && busyDialog.isShowing())
		{
			runOnUiThread(new Runnable() 
			{
				
				@Override
				public void run() 
				{
					busyDialog.dismiss();
					busyDialog = null;
				}
			});
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		// overridePendingTransition(R.anim.slide_in_right,
		// R.anim.slide_out_left);
		//Are we exiting
		 if (getIntent().getBooleanExtra("EXIT", false)) {
	         this.finishAffinity();
	         return;
	    }
		setNavigationDrawerItemsUnselected();
		setActionBarAppearance();
		checkForCrashes();
	}

	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content
		// view
		// boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		// menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
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
	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			
			Intent intent = null;
			
			System.out.println("pos: " + position + " checked: "
					+ mDrawerList.getCheckedItemPosition());
			System.out.println(mDrawerList.isItemChecked(1));

			System.out.println();

			if (position != mDrawerSelectedItem) {
				switch (menuItems.get(position)) {
				case HOME:
					// Home
					startActivity(new Intent(MainActivity.this,
							HomeActivity.class));
					break;
//				case COMPARE:
//					// Compare
//					startActivity(new Intent(MainActivity.this,
//							CompareActivity.class));
//					break;
				case SETTINGS:
					// Settings
					startActivity(new Intent(MainActivity.this,
							SettingsActivity.class));
					break;
//				case DRIVERSETUP:
//					// Driver setup
//					startActivity(new Intent(MainActivity.this,
//							DriverSetupActivity.class));
//					break;
				case FEEDBACK:
					// Feedback
					String driverName = vehicle != null ? ", " + vehicle.name
							: "";
					new DialogFeedback(actionBarTitle.toLowerCase(Locale.US)
							+ " screen" + driverName).show(
							getSupportFragmentManager(), "FeedbackDialog");
					setNavigationDrawerItemsUnselected();
					Utils.gaTrackScreen(MainActivity.this, "Feedback Dialog");
					break;
				case FINDAMECHANIC:
					// Facilities
					intent = new Intent(MainActivity.this,	FindMechanicActivity.class);
					intent.putExtra(VehicleEntry._ID, vehicle.id);		
					startActivity(intent);
					break;
//				case CALLSUPPORT:
//					// Call support
//					String contactPhone = prefs.getString(
//							Constants.PREF_CONTACT_PHONE, "");
//					if (!contactPhone.equals("")) {
//						Intent callSupportIntent = new Intent(
//								Intent.ACTION_VIEW);
//						callSupportIntent.setData(Uri.parse("tel:"
//								+ contactPhone));
//						startActivity(callSupportIntent);
//					}
//
//					Utils.gaTrackScreen(MainActivity.this, "Contact claims");
//					break;
//				case AGENT:
//					// Agent
//					String agentPhone = prefs.getString(
//							Constants.PREF_AGENT_PHONE, "");
//					if (!agentPhone.equals("")) {
//						Intent callAgentIntent = new Intent(Intent.ACTION_VIEW);
//						callAgentIntent.setData(Uri.parse("tel:" + agentPhone));
//						startActivity(callAgentIntent);
//					}
//
//					Utils.gaTrackScreen(MainActivity.this, "Call my agent");
//					break;
				case LOGOUT:
					// Logout
					Utils.gaTrackScreen(MainActivity.this, "Logout");
					prefs.edit().putString(Constants.PREF_AUTH_KEY, "")
							.commit();
					stopService(new Intent(MainActivity.this,
							LocationService.class));
					intent = new Intent(MainActivity.this, InitActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK	| Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
					break;
				case EXIT:
					// EXIT
					intent = new Intent(getApplicationContext(), MainActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtra("EXIT", true);
					startActivity(intent);
				}

				mDrawerLayout.closeDrawers();
			}
		}
	}

	/**
	 * 
	 * @param item
	 *            MenuItem num
	 */
	public void setNavigationDrawerItemSelected(MenuItems item) {
		for (int i = 0; i < menuItems.size(); i++) {
			if (menuItems.get(i).equals(item)) {
				mDrawerSelectedItem = i;
				break;
			}
		}

		mDrawerList.setItemChecked(mDrawerSelectedItem, true);
	}

	public void setNavigationDrawerItemsUnselected() {
		mDrawerSelectedItem = -1;
		int menuItemsSize = menuItems.size();
		for (int i = 0; i < menuItemsSize; i++) {
			mDrawerList.setItemChecked(i, false);
		}
	}

	/*
	 * @Override public void setTitle(CharSequence title) { mTitle = title;
	 * getActionBar().setTitle(mTitle); }
	 */

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		setNavigationDrawerItemsUnselected();
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
		switch (keycode) {
		case KeyEvent.KEYCODE_MENU:
			if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT))
				mDrawerLayout.closeDrawers();
			else
				mDrawerLayout.openDrawer(Gravity.RIGHT);
			return true;
		}

		return super.onKeyDown(keycode, e);
	}

	@Override
	public void onBackPressed() 
	{
		if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
			mDrawerLayout.closeDrawers();
			return;
		}
		else
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.ExitTitle).setMessage(R.string.ExitMessage);
			builder.setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener()
			{
				
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					Intent intent = new Intent(getApplicationContext(), MainActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtra("EXIT", true);
					startActivity(intent);
				}
			});
			builder.setNegativeButton(R.string.No, new DialogInterface.OnClickListener()
			{
				
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
					MainActivity.super.onBackPressed();
				}
			});
			
			AlertDialog lert = builder.create();
			lert.show();
			return;
		}
	}

	private void checkForCrashes() {
		CrashManager.register(this, Constants.HOCKEY_APP_ID,
				new CrashManagerListener() {
					@Override
					public boolean shouldAutoUploadCrashes() {
						return false;
					}
				});
	}

	public void showFeedbackActivity() {
		FeedbackManager.register(this, Constants.HOCKEY_APP_ID, null);
		FeedbackManager.showFeedbackActivity(this);
	}
}