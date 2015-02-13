package com.modusgo.ubi;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.modusgo.ubi.db.DbHelper;
import com.modusgo.ubi.db.VehicleContract.VehicleEntry;
import com.modusgo.ubi.requesttasks.GetTripsTask;
import com.modusgo.ubi.requesttasks.GetVehicleRequest;
import com.modusgo.ubi.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class TripsFragment extends Fragment{
	
	public final static int TRIPS_PER_REQUEST = 10;
	
	private Vehicle vehicle;
	private SharedPreferences prefs;
	
	private ArrayList<Trip> trips;
	private ArrayList<ListItem> tripListItems;
	private TripsAdapter adapter;
	
	private SwipeRefreshLayout lRefresh;
	LinearLayout llProgress;
	private ListView lv;
	
	private boolean thereAreOlderTrips = true;
	private DbHelper dbHelper;
	
	private BroadcastReceiver tripsUpdateReceiver;
	private IntentFilter tripsUpdateFilter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LinearLayout rootView = (LinearLayout)inflater.inflate(R.layout.framgent_trips, container, false);
		
		((MainActivity)getActivity()).setActionBarTitle("TRIPS");

		vehicle = ((DriverActivity)getActivity()).vehicle;
		trips = new ArrayList<Trip>();
		tripListItems = new ArrayList<ListItem>();
		
		tripsUpdateFilter = new IntentFilter(Constants.BROADCAST_UPDATE_TRIPS);
		tripsUpdateReceiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				long id = intent.getLongExtra("vehicle_id", 0);
				runTripsTask(true, id);
			}
		};
		
		prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		dbHelper = DbHelper.getInstance(getActivity());
		rootView.findViewById(R.id.btnSwitchDriverMenu).setBackgroundDrawable(Utils.getButtonBgStateListDrawable(prefs.getString(Constants.PREF_BR_SWITCH_DRIVER_MENU_BUTTON_COLOR, Constants.SWITCH_DRIVER_BUTTON_BG_COLOR)));
		rootView.findViewById(R.id.bottom_line).setBackgroundColor(Color.parseColor(prefs.getString(Constants.PREF_BR_LIST_HEADER_LINE_COLOR, Constants.LIST_HEADER_LINE_COLOR)));
		
		((TextView)rootView.findViewById(R.id.tvName)).setText(vehicle.name);
		
		ImageView imagePhoto = (ImageView)rootView.findViewById(R.id.imagePhoto);
	    if(vehicle.photo == null || vehicle.photo.equals(""))
	    	imagePhoto.setImageResource(R.drawable.person_placeholder);
	    else{
	    	DisplayImageOptions options = new DisplayImageOptions.Builder()
	        .showImageOnLoading(R.drawable.person_placeholder)
	        .showImageForEmptyUri(R.drawable.person_placeholder)
	        .showImageOnFail(R.drawable.person_placeholder)
	        .cacheInMemory(true)
	        .cacheOnDisk(true)
	        .build();
	    	
	    	ImageLoader.getInstance().displayImage(vehicle.photo, imagePhoto, options);
	    }
		
		rootView.findViewById(R.id.btnSwitchDriverMenu).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((DriverActivity)getActivity()).menu.toggle();
			}
		});
		
		rootView.findViewById(R.id.btnTimePeriod).setVisibility(View.GONE);
		
		lRefresh = (SwipeRefreshLayout) rootView.findViewById(R.id.lRefresh);
		llProgress = (LinearLayout) rootView.findViewById(R.id.llProgress);
		lv = (ListView) rootView.findViewById(R.id.listView);
		
		lRefresh.setColorSchemeResources(R.color.ubi_gray, R.color.ubi_green, R.color.ubi_orange, R.color.ubi_red);
		lRefresh.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				runTripsTask(true, vehicle.id);
				new GetVehicleRequest(getActivity().getApplicationContext()).execute("vehicles/"+vehicle.id+".json");
			}
		});
		
		adapter = new TripsAdapter();
		lv.setAdapter(adapter);
		
		trips.addAll(dbHelper.getTripsFromDb(Calendar.getInstance().getTime(), TRIPS_PER_REQUEST, vehicle.id, prefs));
		
		if(trips.size()==0)
			runTripsTask(true, vehicle.id);
		
		
		
		return rootView;
	}
	
	private void runTripsTask(boolean value,long id){
		new GetTripsTask(getActivity(), value, id, getFragmentManager()){
			
			@Override
			protected void onPreExecute() {
				lRefresh.setRefreshing(true);
				super.onPreExecute();
			}
			
			@Override
			protected void onPostExecute(JSONObject result) {
				lRefresh.setRefreshing(false);
				super.onPostExecute(result);
			}
		}.execute("vehicles/"+id+"/trips.json");
	}
	
	@Override
	public void onResume() {
		updateTripsListView();
		getActivity().registerReceiver(tripsUpdateReceiver, tripsUpdateFilter);
		Utils.gaTrackScreen(getActivity(), "Trips Screen");		
		super.onResume();
	}
	
	@Override
	public void onPause() {
		getActivity().unregisterReceiver(tripsUpdateReceiver);
		super.onPause();
	}
	
	public void setThereOldTrip(boolean bool){
		thereAreOlderTrips = bool;
	}
	
	
	
	public void updateTripsListView(){
		trips.clear();
		trips.addAll(dbHelper.getTripsFromDb(Calendar.getInstance().getTime(), TripsFragment.TRIPS_PER_REQUEST, vehicle.id, prefs));
		Trip.updateTimezones(prefs);
		SimpleDateFormat sdfDate = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
		TimeZone tzTo = TimeZone.getTimeZone(prefs.getString(Constants.PREF_TIMEZONE_OFFSET, Constants.DEFAULT_TIMEZONE));
		sdfDate.setTimeZone(tzTo);
		
		Calendar cPrev = Calendar.getInstance();
		cPrev.setTimeZone(tzTo);
		Calendar cNow = Calendar.getInstance();
		cNow.setTimeZone(tzTo);
		tripListItems.clear();
		
		TripListHeader currentHeader = new TripListHeader("", "");
		Trip prevTrip = null;
		int tripsCount = trips.size();
		float tripsDistance = 0;
		int tripsDurationInMinutes = 0;
		
		DecimalFormat distanceFormat = new DecimalFormat("0.0");
		
		for (int i = 0; i < tripsCount; i++) {
			Trip t = trips.get(i);
			
			if(prevTrip!=null){
				
				cPrev.setTime(prevTrip.getStartDate());
				cNow.setTime(t.getStartDate());
				
				if(cNow.get(Calendar.YEAR) != cPrev.get(Calendar.YEAR) || 
						(cNow.get(Calendar.YEAR) == cPrev.get(Calendar.YEAR) && cNow.get(Calendar.DAY_OF_YEAR) != cPrev.get(Calendar.DAY_OF_YEAR))){
					
					currentHeader.date = sdfDate.format(prevTrip.getStartDate());
					currentHeader.total = "Totals: "+(int)Math.floor(tripsDurationInMinutes/60)+" hr " + tripsDurationInMinutes%60 + " min "+distanceFormat.format(tripsDistance)+" MI";
					currentHeader = new TripListHeader("", "");

					System.out.println(tripsDurationInMinutes);
					tripsDistance = 0;
					tripsDurationInMinutes = 0;
					tripListItems.add(currentHeader);
				}
				if (i == tripsCount-1){
					tripsDistance += t.distance;
					tripsDurationInMinutes += Utils.durationInMinutes(t.getStartDate(), t.getEndDate());
					currentHeader.date = sdfDate.format(t.getStartDate());
					currentHeader.total = "Totals: "+(int)Math.floor(tripsDurationInMinutes/60)+" hr " + tripsDurationInMinutes%60 + " min "+distanceFormat.format(tripsDistance)+" MI";
				}
				//System.out.println(i);
			}
			else{
				tripsDurationInMinutes += Utils.durationInMinutes(t.getStartDate(), t.getEndDate());
				currentHeader.date = sdfDate.format(t.getStartDate());
				currentHeader.total = "Totals: "+(int)Math.floor(tripsDurationInMinutes/60)+" hr " + tripsDurationInMinutes%60 + " min "+distanceFormat.format(tripsDistance)+" MI";
				tripListItems.add(currentHeader);
			}

			tripsDistance += t.distance;
			tripsDurationInMinutes += Utils.durationInMinutes(t.getStartDate(), t.getEndDate());
			
			tripListItems.add(t);
			prevTrip = t;
		}
		adapter.notifyDataSetChanged();
	}
	
	
	
	class ViewHolderHeader{
		TextView tvDate;
		TextView tvTotals;
		View bottomLine;
	}
	
	class ViewHolderTrip{
		TextView tvEventsCount;
		TextView tvScore;
		TextView tvStartTime;
		TextView tvEndTime;
		TextView tvDistance;
		TextView tvDistanceUnits;
		View lFuel;
		TextView tvFuel;
		TextView tvFuelUnit;
		ImageView imageFuelArrow;
		View distanceBlock;
	}
	
	class TripsAdapter extends BaseAdapter{

		final int TRIP_ITEM = 0;
		final int HEADER_ITEM = 1;
		String units;
		
		LayoutInflater lInflater;
		
		public TripsAdapter() {
		    lInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		    
			if(prefs.getString(Constants.PREF_UNITS_OF_MEASURE, "mile").equals("mile")){
				units = "Miles";
			}
			else{
				units = "KM";
			}
		}
		
		@Override
		public int getCount() {
			return tripListItems.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public int getViewTypeCount() {
			return 2;
		}
		
		@Override
		public int getItemViewType(int position) {
			if(tripListItems.get(position) instanceof Trip)
				return TRIP_ITEM;
			else
				return HEADER_ITEM;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(thereAreOlderTrips && position == getCount() - 1)
				runTripsTask(false, vehicle.id);
			
			if(getItemViewType(position)==HEADER_ITEM)
				return getHeaderView(position, convertView, parent);
			else
				return getTripView(position, convertView, parent);
//			for (Map.Entry<String, ArrayList<Trip>> entry : trips.entrySet()) {
//			    ArrayList<Trip> trips = entry.getValue();
//				int size = trips.size() + 1; 
//				if(position == 0) return getHeaderView(entry.getKey(), parent);
//				if(position < size) return getTripView(trips.get(position-1), parent);  
//				position -= size;
//			}
			
//	        return null;
		}
		
		private View getHeaderView(int position, View convertView, ViewGroup parent){
			ViewHolderHeader holder;
			TripListHeader h = (TripListHeader) tripListItems.get(position);
			View view = convertView;
			if(view==null){
				view = lInflater.inflate(R.layout.trips_list_header, parent, false);
				holder = new ViewHolderHeader();
				holder.tvDate = (TextView) view.findViewById(R.id.tvDate);
				holder.tvTotals = (TextView) view.findViewById(R.id.tvTotals);
				holder.bottomLine = view.findViewById(R.id.bottom_line);
				
				view.setTag(holder);
			}
			else{
				holder = (ViewHolderHeader) view.getTag();
			}
			
			holder.tvDate.setText(h.date);
			holder.tvTotals.setText(h.total);
			holder.bottomLine.setBackgroundColor(Color.parseColor(prefs.getString(Constants.PREF_BR_LIST_HEADER_LINE_COLOR, Constants.LIST_HEADER_LINE_COLOR)));
			
			return view;
		}
		
		private View getTripView(int position, View convertView, ViewGroup parent){
			ViewHolderTrip holder;
			final Trip t = (Trip)tripListItems.get(position);
			
			View view = convertView;
			if(view==null){
				view = lInflater.inflate(R.layout.trips_list_item, parent, false);
				holder = new ViewHolderTrip();
				holder.tvDistance = (TextView) view.findViewById(R.id.tvDistance);
				holder.tvDistanceUnits = (TextView) view.findViewById(R.id.tvDistanceUnits);
				holder.tvEventsCount = (TextView) view.findViewById(R.id.tvCounter);
				holder.tvStartTime = (TextView) view.findViewById(R.id.tvStartTime);
				holder.tvEndTime = (TextView) view.findViewById(R.id.tvEndTime);
				holder.tvScore = (TextView) view.findViewById(R.id.tvScore);
				holder.lFuel = view.findViewById(R.id.lFuel);
				holder.tvFuel = (TextView) view.findViewById(R.id.tvFuel);
				holder.tvFuelUnit = (TextView) view.findViewById(R.id.tvFuelUnit);
				holder.imageFuelArrow = (ImageView) view.findViewById(R.id.imageFuelArrow);
				holder.distanceBlock = (View) holder.tvDistance.getParent();
				view.setTag(holder);
			}
			else{
				holder = (ViewHolderTrip) view.getTag();				
			}
			
			holder.distanceBlock.setBackgroundColor(Color.parseColor("#00aeef"));
				
			if(t.eventsCount>0){
				holder.tvEventsCount.setTextColor(Color.parseColor("#FFFFFF"));
				holder.tvEventsCount.setBackgroundResource(R.drawable.bg_alerts_triangle_red);
				holder.tvEventsCount.setText(""+t.eventsCount);
			}
			else{
				holder.tvEventsCount.setBackgroundResource(R.drawable.bg_alerts_triangle_green);
				holder.tvEventsCount.setText("");				
			}
			
			String grade = t.grade;
			holder.tvScore.setText(grade);
			if(grade.contains("A")){
				holder.tvScore.setBackgroundResource(R.drawable.circle_score_green_light);
			}
			else if(grade.contains("B")){
				holder.tvScore.setBackgroundResource(R.drawable.circle_score_green_dark);
			}
			else if(grade.contains("C")){
				holder.tvScore.setBackgroundResource(R.drawable.circle_score_orange);
			}
			else if(grade.contains("D") || grade.contains("E") || grade.contains("F")){
				holder.tvScore.setBackgroundResource(R.drawable.circle_score_red);
			}
			else{
				holder.tvScore.setBackgroundResource(R.drawable.ic_score_arrow);
				holder.tvScore.setText("");
			}
			
			if(t.fuel>=0 && !TextUtils.isEmpty(t.fuelUnit)){

				DecimalFormat df = new DecimalFormat("0.#");
				holder.lFuel.setVisibility(View.VISIBLE);
				holder.tvFuel.setVisibility(View.VISIBLE);
				if(t.fuelUnit.equals("%")){
					holder.tvFuel.setText(""+df.format(t.fuel)+t.fuelUnit);
					holder.tvFuelUnit.setVisibility(View.GONE);
				}
				else{
					holder.tvFuel.setText(""+df.format(t.fuel));
					holder.tvFuelUnit.setText(t.fuelUnit);
					holder.tvFuelUnit.setVisibility(View.VISIBLE);
				}
				holder.imageFuelArrow.setVisibility(View.GONE);
			}
			else{
				if(!TextUtils.isEmpty(t.fuelStatus)){
					holder.lFuel.setVisibility(View.VISIBLE);
					holder.tvFuel.setVisibility(View.INVISIBLE);
					holder.tvFuel.setText("0.0");
					holder.tvFuelUnit.setVisibility(View.GONE);
					holder.imageFuelArrow.setVisibility(View.VISIBLE);
				}
				else{
					holder.lFuel.setVisibility(View.GONE);
				}
			}
			
			if(t.viewed){
				holder.tvStartTime.setTypeface(holder.tvStartTime.getTypeface(), Typeface.NORMAL);
				holder.tvEndTime.setTypeface(holder.tvEndTime.getTypeface(), Typeface.NORMAL);
			}
			else{
				holder.tvStartTime.setTypeface(holder.tvStartTime.getTypeface(), Typeface.BOLD);
				holder.tvEndTime.setTypeface(holder.tvEndTime.getTypeface(), Typeface.BOLD);
			}
			
			holder.tvStartTime.setText(t.getStartDateString());
			holder.tvEndTime.setText(t.getEndDateString());
			if(t.distance<1000)
				holder.tvDistance.setText(new DecimalFormat("0.0").format(t.distance));
			else
				holder.tvDistance.setText(new DecimalFormat("0").format(t.distance));
			
			holder.tvDistanceUnits.setText(units);
			
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(!t.viewed){
						SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.US);
						t.viewed = true;
						t.viewedAt = sdf.format(Calendar.getInstance().getTime());
						dbHelper.saveTrip(vehicle.id, t);
						dbHelper.close();
					}
					Intent intent = new Intent(getActivity(), TripActivity.class);
					intent.putExtra(VehicleEntry._ID, vehicle.id);
					intent.putExtra(TripActivity.EXTRA_TRIP_ID, t.id);
					startActivity(intent);
				}
			});
			
			return view;
		}
		
	}

}
