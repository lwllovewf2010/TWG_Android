package com.modusgo.ubi;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import com.modusgo.ubi.db.TripContract.TripEntry;
import com.modusgo.ubi.db.VehicleContract.VehicleEntry;
import com.modusgo.ubi.requesttasks.BaseRequestAsyncTask;
import com.modusgo.ubi.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class TripsFragment extends Fragment{
	
	private final static int TRIPS_PER_REQUEST = 10;
	
	Vehicle vehicle;
	SharedPreferences prefs;
	
	ArrayList<Trip> trips;
	ArrayList<ListItem> tripListItems;
	TripsAdapter adapter;
	
	SwipeRefreshLayout lRefresh;
	LinearLayout llProgress;
	ListView lv;
	
	private boolean offlineMode = false;
	private boolean thereAreOlderTrips = true;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LinearLayout rootView = (LinearLayout)inflater.inflate(R.layout.framgent_trips, container, false);
		
		((MainActivity)getActivity()).setActionBarTitle("TRIPS");

		vehicle = ((DriverActivity)getActivity()).vehicle;
		trips = new ArrayList<Trip>();
		tripListItems = new ArrayList<ListItem>();
		
		prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
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
				new GetTripsTask(getActivity(), true).execute("vehicles/"+vehicle.id+"/trips.json");
			}
		});
		
		adapter = new TripsAdapter();
		lv.setAdapter(adapter);
		
		trips.addAll(getTripsFromDb(Calendar.getInstance().getTime(), TRIPS_PER_REQUEST));
		
		if(trips.size()==0)
			new GetTripsTask(getActivity(), true).execute("vehicles/"+vehicle.id+"/trips.json");
		
		return rootView;
	}
	
	@Override
	public void onResume() {
		updateTripsListView();
		Utils.gaTrackScreen(getActivity(), "Trips Screen");		
		super.onResume();
	}
	
	private ArrayList<Trip> getTripsFromDb(Date endDate, int count){
		DbHelper dbHelper = DbHelper.getInstance(getActivity());
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.US);
		
		Cursor c = db.query(TripEntry.TABLE_NAME, 
				new String[]{
				TripEntry._ID,
				TripEntry.COLUMN_NAME_EVENTS_COUNT,
				TripEntry.COLUMN_NAME_START_TIME,
				TripEntry.COLUMN_NAME_END_TIME,
				TripEntry.COLUMN_NAME_DISTANCE,
				TripEntry.COLUMN_NAME_GRADE,
				TripEntry.COLUMN_NAME_FUEL,
				TripEntry.COLUMN_NAME_FUEL_UNIT,
				TripEntry.COLUMN_NAME_FUEL_STATUS,
				TripEntry.COLUMN_NAME_FUEL_COST,
				TripEntry.COLUMN_NAME_VIEWED_AT,
				TripEntry.COLUMN_NAME_UPDATED_AT},
				TripEntry.COLUMN_NAME_VEHICLE_ID + " = " + vehicle.id + " AND " + TripEntry.COLUMN_NAME_HIDDEN + " = 0 AND " +
				"datetime(" + TripEntry.COLUMN_NAME_START_TIME + ")<datetime('"+ Utils.fixTimeZoneColon(sdf.format(endDate)) + "')", null, null, null, "datetime("+TripEntry.COLUMN_NAME_START_TIME+") DESC", ""+count);

		System.out.println("trips from db: "+c.getCount());
		
		ArrayList<Trip> trips = new ArrayList<Trip>();
		if(c.moveToFirst()){
			while(!c.isAfterLast()){
				Trip t = new Trip(
						c.getLong(0), 
						c.getInt(1), 
						c.getString(2), 
						c.getString(3), 
						c.getDouble(4),
						c.getString(5));
				t.fuel = c.getFloat(6);
				t.fuelUnit = c.getString(7);
				t.fuelStatus = c.getString(8);
				t.fuelCost = c.getFloat(9);
				t.viewedAt = c.getString(10);
				t.updatedAt = c.getString(11);
				
				try {
					if(!TextUtils.isEmpty(t.viewedAt) && !TextUtils.isEmpty(t.updatedAt)){
						Calendar cViewedAt = Calendar.getInstance();
						cViewedAt.setTime(sdf.parse(t.viewedAt));
						Calendar cUpdatedAt = Calendar.getInstance();
						cUpdatedAt.setTime(sdf.parse(t.updatedAt));
						
						if(cViewedAt.after(cUpdatedAt))
							t.viewed = true;
					}
					
				} catch (ParseException e) {
					e.printStackTrace();
				}
				
				trips.add(t);
				c.moveToNext();
			}
		}
		c.close();
		db.close();
		dbHelper.close();
		
		System.out.println("trips array "+trips.size());
		
		return trips;
	}
	
	private void updateTripsListView(){
		
		SimpleDateFormat sdfDate = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
		
		Calendar cPrev = Calendar.getInstance();
		Calendar cNow = Calendar.getInstance();
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
				tripListItems.add(currentHeader);
			}

			tripsDistance += t.distance;
			tripsDurationInMinutes += Utils.durationInMinutes(t.getStartDate(), t.getEndDate());
			
			tripListItems.add(t);
			prevTrip = t;
		}
		adapter.notifyDataSetChanged();
	}
	
	class GetTripsTask extends BaseRequestAsyncTask{
		
		boolean getNewTrips = false;
		private Calendar cEndTime = Calendar.getInstance();
		
		public GetTripsTask(Context context, boolean getNewTrips) {
			super(context);
			this.getNewTrips = getNewTrips;
			if(getNewTrips)
				cEndTime.setTimeInMillis(System.currentTimeMillis());
			else
				cEndTime.setTime(trips.get(trips.size()-1).getStartDate());
		}
		
		@Override
		protected void onPreExecute() {
			lRefresh.setRefreshing(true);
			super.onPreExecute();
		}

		@Override
		protected JSONObject doInBackground(String... params) {
			SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.US);
			requestParams.add(new BasicNameValuePair("vehicle_id", ""+vehicle.id));
			requestParams.add(new BasicNameValuePair("page", "1"));
	        requestParams.add(new BasicNameValuePair("per_page", ""+TRIPS_PER_REQUEST));
	        requestParams.add(new BasicNameValuePair("end_time", sdf.format(cEndTime.getTime())));
			return super.doInBackground(params);
		}
		
		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			lRefresh.setRefreshing(false);
		}
		
		@Override
		protected void onSuccess(JSONObject responseJSON) throws JSONException {
			JSONArray tripsJSON = responseJSON.getJSONArray("trips");
			System.out.println(responseJSON);
			
			if(offlineMode && getNewTrips){
				trips.clear();
				offlineMode = false;
				thereAreOlderTrips = true;
			}
			
			boolean clearTrips = true;
			
			ArrayList<Trip> newTrips = new ArrayList<Trip>();
			Trip firstOldTrip = null;
			if(trips.size()>0)
				firstOldTrip = trips.get(0);
			
			for (int i = 0; i < tripsJSON.length(); i++) {
				JSONObject tripJSON = tripsJSON.getJSONObject(i);
				
				System.out.println("i = "+i);
				Trip t = new Trip(
						tripJSON.optLong("id"), 
						tripJSON.optInt("harsh_events_count"), 
						Utils.fixTimezoneZ(tripJSON.optString("start_time")), 
						Utils.fixTimezoneZ(tripJSON.optString("end_time")), 
						tripJSON.optDouble("mileage"));
				t.grade = tripJSON.optString("grade");
				t.fuel = (float) tripJSON.optDouble("fuel_used",-1);
				t.fuelUnit = tripJSON.optString("fuel_unit");
				t.fuelCost = (float) tripJSON.optDouble("fuel_cost");
				t.fuelStatus = tripJSON.optString("fuel_status");
				t.updatedAt = tripJSON.optString("updated_at");
				t.hidden = tripJSON.optBoolean("hidden");
				
				if(firstOldTrip != null && t.id == firstOldTrip.id){
					clearTrips = false;
					break;
				}
				newTrips.add(t);
			}
			
			DbHelper dbHelper = DbHelper.getInstance(getActivity());
			dbHelper.saveTrips(vehicle.id, newTrips);
			dbHelper.close();
			
			if(getNewTrips) {
				if(clearTrips){
					trips.clear();
				}
				
				trips.addAll(0, newTrips);
			}
			else {
				if(newTrips.size() == 0)
					thereAreOlderTrips = false;
				
				trips.addAll(getTripsFromDb(cEndTime.getTime(), TRIPS_PER_REQUEST));
			}
			
			updateTripsListView();
			
			super.onSuccess(responseJSON);
		}
		
		@Override
		protected void onError(String message) {
			offlineMode = true;
			if(!getNewTrips){
				ArrayList<Trip> dbTrips = getTripsFromDb(cEndTime.getTime(), TRIPS_PER_REQUEST);
				if(dbTrips.size()!=0)
					trips.addAll(dbTrips);
				else
					thereAreOlderTrips = false;
				updateTripsListView();
			}
		}
	}
	
	class ViewHolderHeader{
		TextView tvDate;
		TextView tvTotals;
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
				new GetTripsTask(getActivity(), false).execute("vehicles/"+vehicle.id+"/trips.json");
			
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
				view.findViewById(R.id.bottom_line).setBackgroundColor(Color.parseColor(prefs.getString(Constants.PREF_BR_LIST_HEADER_LINE_COLOR, Constants.LIST_HEADER_LINE_COLOR)));
				view.setTag(holder);
			}
			else{
				holder = (ViewHolderHeader) view.getTag();
			}
			
			holder.tvDate.setText(h.date);
			holder.tvTotals.setText(h.total);
			
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
				view.setTag(holder);
			}
			else{
				holder = (ViewHolderTrip) view.getTag();				
			}
				
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
				holder.tvScore.setBackgroundResource(R.drawable.circle_score_green);
			}
			else if(grade.contains("B")){
				holder.tvScore.setBackgroundResource(R.drawable.circle_score_yellow);
			}
			else if(grade.contains("C")){
				holder.tvScore.setBackgroundResource(R.drawable.circle_score_orange);
			}
			else if(grade.contains("D") || grade.contains("E") || grade.contains("F")){
				holder.tvScore.setBackgroundResource(R.drawable.circle_score_red);
			}
			else{
				holder.tvScore.setBackgroundResource(R.drawable.circle_score_gray);
				holder.tvScore.setText("N/A");
			}
			
			System.out.println("fuel: "+t.fuel+" unit: "+t.fuelUnit + " cost: "+t.fuelCost+" status: "+t.fuelStatus);
			
			if(t.fuel>=0 && !TextUtils.isEmpty(t.fuelUnit)){

				DecimalFormat df = new DecimalFormat("0.0");
				holder.lFuel.setVisibility(View.VISIBLE);
				holder.tvFuel.setText(""+df.format(t.fuel));
				holder.tvFuel.setVisibility(View.VISIBLE);
				holder.tvFuelUnit.setText(t.fuelUnit);
				holder.tvFuelUnit.setVisibility(View.VISIBLE);
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
						DbHelper dHelper = DbHelper.getInstance(getActivity());
						dHelper.saveTrip(vehicle.id, t);
						dHelper.close();
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
