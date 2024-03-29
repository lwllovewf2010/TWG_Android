package com.modusgo.ubi;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
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
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnCloseListener;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnOpenListener;
import com.modusgo.ubi.db.DbHelper;
import com.modusgo.ubi.db.VehicleContract.VehicleEntry;
import com.modusgo.ubi.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class DriverActivity extends MainActivity{

	public static final String SAVED_DRIVER = "driver";

	private FragmentTabHost tabHost;
	SlidingMenu menu;
	View driverTabView;
	private VehiclesAdapter vehiclesAdapter;

	ArrayList<Vehicle> vehiclesShort;

	BroadcastReceiver vehiclesUpdateReceiver;
	IntentFilter vehiclesUpdateFilter;
	private DbHelper dbHelper;

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

		dbHelper = DbHelper.getInstance(this);
		vehicle = dbHelper.getVehicle(getIntent().getLongExtra(VehicleEntry._ID, 0));

		driverTabView = setupTab(DriverDetailsFragment.class, b, "Driver Detail", R.drawable.ic_tab_driver, 0);
		setupTab(TripsFragment.class, b, "Trips", R.drawable.ic_tab_trips, 0);
		setupTab(ScoreFragment.class, b, "Score", vehicle.grade, 0);
		if(prefs.getBoolean(Constants.PREF_DIAGNOSTIC, false))
			setupTab(DiagnosticsFragment.class, b, "Diagnostics", R.drawable.ic_tab_diagnostics, 0);
		setupTab(LimitsFragment.class, b, "Limits", R.drawable.ic_tab_limits, 0);

		setButtonUpVisibility(false);

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
				setButtonNavigationDrawerVisibility(false);
				Utils.gaTrackScreen(DriverActivity.this, "Switch Driver Menu");
			}
		});
		menu.setOnCloseListener(new OnCloseListener() {
			@Override
			public void onClose() {
				setButtonNavigationDrawerVisibility(true);			
			}
		});

		ListView lvVehicles = (ListView) menu.findViewById(R.id.listViewDrivers);


		vehiclesShort = dbHelper.getVehiclesShort();
		vehiclesAdapter = new VehiclesAdapter(this, vehiclesShort);
		dbHelper.close();

		lvVehicles.setAdapter(vehiclesAdapter);
		lvVehicles.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				if(prefs.getInt(Constants.PREF_CURRENT_DRIVER, 0)!=position){
					prefs.edit().putInt(Constants.PREF_CURRENT_DRIVER, position).commit();
					menu.toggle();
					finish();

					Intent i = new Intent(DriverActivity.this, DriverActivity.class);
					i.putExtra(VehicleEntry._ID,vehiclesShort.get(position).id);
					startActivity(i);
				}
				else{
					menu.toggle();
				}



			}
		});

		vehiclesUpdateFilter = new IntentFilter(Constants.BROADCAST_UPDATE_VEHICLES);
		vehiclesUpdateReceiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				vehicle = dbHelper.getVehicle(getIntent().getLongExtra(VehicleEntry._ID, 0));
				updateDriverDetailsTab();
				vehiclesAdapter.updateVehicle(dbHelper.getVehiclesShort());
				if(getSupportFragmentManager().findFragmentByTag("Driver Detail") instanceof DriverDetailsFragment){
					((DriverDetailsFragment)getSupportFragmentManager().findFragmentByTag("Driver Detail")).updateDriverInfo();
				}
			}
		};
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(vehiclesUpdateReceiver, vehiclesUpdateFilter);
		setNavigationDrawerItemsUnselected();
		updateDriverDetailsTab();
	}

	@Override
	protected void onPause() {
		unregisterReceiver(vehiclesUpdateReceiver);
		super.onPause();
	}

	private void updateDriverDetailsTab(){
		if(driverTabView!=null){

			ImageView icon = (ImageView) driverTabView.findViewById(R.id.imageIcon);
			TextView tvTitle = (TextView) driverTabView.findViewById(R.id.tabsText);

			if(vehicle.inTrip){
				int inTripColor = getResources().getColor(R.color.blue);
				icon.setColorFilter(inTripColor, PorterDuff.Mode.MULTIPLY);
				tvTitle.setTextColor(getResources().getColorStateList(R.drawable.driver_in_trip_tab_text_selector));
			}
			else{
				icon.setColorFilter(null);
				tvTitle.setTextColor(getResources().getColorStateList(R.drawable.driver_tab_text_selector));
			}
		}
	}

	public void switchTab(int index){
		tabHost.setCurrentTab(index);
	}

	class VehiclesAdapter extends BaseAdapter{

		Context ctx;
		LayoutInflater lInflater;
		ArrayList<Vehicle> objects;

		VehiclesAdapter(Context context, ArrayList<Vehicle> drivers) {
			ctx = context;
			objects = drivers;
			lInflater = (LayoutInflater) ctx
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return objects.size();
		}

		private void updateVehicle(ArrayList<Vehicle> objects){
			this.objects = objects;
			notifyDataSetChanged();
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
			ViewHolder holder;
			if (convertView == null) {
				convertView = lInflater.inflate(R.layout.switch_driver_item, parent, false);
				holder = new ViewHolder();
				holder.imagePhoto = (ImageView) convertView.findViewById(R.id.imagePhoto);
				holder.tvName = ((TextView) convertView.findViewById(R.id.tvName));
				holder.tvInTrip = convertView.findViewById(R.id.tvInTrip);;
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}

			final Vehicle v = getVehicle(position);

			final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(DriverActivity.this);
			if(prefs.getInt(Constants.PREF_CURRENT_DRIVER, -1)>=0 && prefs.getInt(Constants.PREF_CURRENT_DRIVER, -1)==position){

				Spannable span = new SpannableString(Html.fromHtml("<font size=\"10px\" color=\"#3c454f\" face=\"fonts/EncodeSansNormal-600-SemiBold.ttf\">CURRENT</font><br>"+v.name));
				span.setSpan(new RelativeSizeSpan(0.95f), 0, 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

				holder.tvName.setText(span);
			}else
				holder.tvName.setText(v.name);

			if(v.photo == null || v.photo.equals(""))
				holder.imagePhoto.setImageResource(R.drawable.person_placeholder);
			else{
				DisplayImageOptions options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.person_placeholder)
				.showImageForEmptyUri(R.drawable.person_placeholder)
				.showImageOnFail(R.drawable.person_placeholder)
				.cacheInMemory(true)
				.cacheOnDisk(true)
				.build();
				ImageLoader.getInstance().displayImage(v.photo, holder.imagePhoto, options);
			}

			if(v.inTrip)
				holder.tvInTrip.setVisibility(View.VISIBLE);
			else
				holder.tvInTrip.setVisibility(View.GONE);

			return convertView;
		}

		Vehicle getVehicle(int position) {
			return ((Vehicle) getItem(position));
		}

		class ViewHolder {
			ImageView imagePhoto;
			TextView tvName;
			View tvInTrip;
		}

	}

	private View setupTab(Class<?> c, Bundle b, final String tag, int imageResId, int counter) {
		View tabview = createTabView(tabHost.getContext(), tag, imageResId, counter);
		TabSpec setContent = tabHost.newTabSpec(tag).setIndicator(tabview);
		tabHost.addTab(setContent, c, b);
		return tabview;
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
