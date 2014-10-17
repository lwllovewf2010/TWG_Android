package com.modusgo.ubi;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.modusgo.demo.R;
import com.modusgo.ubi.db.DbHelper;
import com.modusgo.ubi.db.TripContract.TripEntry;
import com.modusgo.ubi.db.VehicleContract.VehicleEntry;
import com.modusgo.ubi.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class TripsFragment extends Fragment{
	
	Driver driver;
	SharedPreferences prefs;
	
	ArrayList<ListItem> tripListItems;
	TripsAdapter adapter;
	
	LinearLayout llProgress;
	ListView lv;
	
	Calendar cStart;
	Calendar cEnd;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LinearLayout rootView = (LinearLayout)inflater.inflate(R.layout.framgent_trips, container, false);
		
		((MainActivity)getActivity()).setActionBarTitle("TRIPS");

		driver = ((DriverActivity)getActivity()).driver;
		tripListItems = new ArrayList<ListItem>();
		
		((TextView)rootView.findViewById(R.id.tvName)).setText(driver.name);
		
		ImageView imagePhoto = (ImageView)rootView.findViewById(R.id.imagePhoto);
	    if(driver.photo == null || driver.photo.equals(""))
	    	imagePhoto.setImageResource(R.drawable.person_placeholder);
	    else{
	    	DisplayImageOptions options = new DisplayImageOptions.Builder()
	        .showImageOnLoading(R.drawable.person_placeholder)
	        .showImageForEmptyUri(R.drawable.person_placeholder)
	        .showImageOnFail(R.drawable.person_placeholder)
	        .cacheInMemory(true)
	        .cacheOnDisk(true)
	        .build();
	    	
	    	ImageLoader.getInstance().displayImage(driver.photo, imagePhoto, options);
	    }
		
		rootView.findViewById(R.id.btnSwitchDriverMenu).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((DriverActivity)getActivity()).menu.toggle();
			}
		});
		
		rootView.findViewById(R.id.btnTimePeriod).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				createDialog().show();
			}
		});
		
		llProgress = (LinearLayout)rootView.findViewById(R.id.llProgress);
		lv = (ListView)rootView.findViewById(R.id.listView);
		
		adapter = new TripsAdapter();
		lv.setAdapter(adapter);
		
		cStart = Calendar.getInstance();
		cStart.add(Calendar.DAY_OF_YEAR, -7);
		cEnd = Calendar.getInstance();
		
		fillTripsListView(getTripsFromDb(cStart.getTime(), cEnd.getTime()));
		
		new GetTripsTask(getActivity()).execute("vehicles/"+driver.id+"/trips.json");
		
		return rootView;
	}
	
	String[] timePeriods = new String[]{"Last 7 Days", "This Month", "Last Month", "All"};
	
	private Dialog createDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Change time period").setItems(timePeriods,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						tripListItems.clear();
						switch (which) {
						case 0:
							cStart.setTimeInMillis(System.currentTimeMillis());
							cStart.add(Calendar.DAY_OF_YEAR, -7);
							cEnd.setTimeInMillis(System.currentTimeMillis());
							fillTripsListView(getTripsFromDb(cStart.getTime(), cEnd.getTime()));
							break;
						case 1:
							cStart.setTimeInMillis(System.currentTimeMillis());
							cStart.set(cStart.get(Calendar.YEAR), cStart.get(Calendar.MONTH), 1, 0, 0);
							cEnd.setTimeInMillis(System.currentTimeMillis());
							fillTripsListView(getTripsFromDb(cStart.getTime(), cEnd.getTime()));
							break;
						case 2:
							cStart.setTimeInMillis(System.currentTimeMillis());
							cStart.set(cStart.get(Calendar.YEAR), cStart.get(Calendar.MONTH)-1, 1, 0, 0);
							cEnd.set(cStart.get(Calendar.YEAR), cStart.get(Calendar.MONTH)+1,1,23,59);
							fillTripsListView(getTripsFromDb(cStart.getTime(), cEnd.getTime()));
							break;
						case 3:
							cStart.set(2000, Calendar.JANUARY, 1, 0, 0);
							cEnd.setTimeInMillis(System.currentTimeMillis());
							fillTripsListView(getTripsFromDb(cStart.getTime(), cEnd.getTime()));
							break;

						default:
							break;
						}
					}
				});
		return builder.create();
	}
	
	private ArrayList<Trip> getTripsFromDb(Date startDate, Date endDate){
		DbHelper dbHelper = DbHelper.getInstance(getActivity());
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.US);
		
		Cursor c = db.query(TripEntry.TABLE_NAME, 
				new String[]{
				TripEntry._ID,
				TripEntry.COLUMN_NAME_EVENTS_COUNT,
				TripEntry.COLUMN_NAME_START_TIME,
				TripEntry.COLUMN_NAME_END_TIME,
				TripEntry.COLUMN_NAME_DISTANCE}, 
				"datetime(" + TripEntry.COLUMN_NAME_START_TIME + ")>=datetime('"+ Utils.fixTimeZoneColon(sdf.format(startDate)) + "') AND " +
				"datetime(" + TripEntry.COLUMN_NAME_START_TIME + ")<=datetime('"+ Utils.fixTimeZoneColon(sdf.format(endDate)) + "')", null, null, null, "datetime("+TripEntry.COLUMN_NAME_START_TIME+") DESC");
		
		System.out.println("trips "+c.getCount());
		
		ArrayList<Trip> trips = new ArrayList<Trip>();
		if(c.moveToFirst()){
			while(!c.isAfterLast()){
				trips.add(new Trip(
						c.getLong(0), 
						c.getInt(1), 
						c.getString(2), 
						c.getString(3), 
						c.getDouble(4)));
				c.moveToNext();
			}
		}
		c.close();
		db.close();
		dbHelper.close();
		
		System.out.println("trips array "+trips.size());
		
		return trips;
	}
	
	private void fillTripsListView(ArrayList<Trip> trips){
		
		SimpleDateFormat sdfDate = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
		
		Calendar cPrev = Calendar.getInstance();
		Calendar cNow = Calendar.getInstance();
		tripListItems.clear();
		
		Random r = new Random();
		TripListHeader currentHeader = new TripListHeader("", "");
		Trip prevTrip = null;
		int tripsCount = trips.size();
		float tripsDistance = 0;
		int tripsDurationInMinutes = 0;
		
		DecimalFormat distanceFormat = new DecimalFormat("0.0");
		
		for (int i = 0; i < tripsCount; i++) {
			Trip t = trips.get(i);
			
			switch (r.nextInt(5)) {
			case 0:
				t.grade = "A";
				break;
			case 1:
				t.grade = "B";
				break;
			case 2:
				t.grade = "C+";
				break;
			case 3:
				t.grade = "C";
				break;
			case 4:
				t.grade = "E";
				break;
			case 5:
				t.grade = "F";
				break;

			default:
				break;
			}
			
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
		
		public GetTripsTask(Context context) {
			super(context);
		}
		
		@Override
		protected void onPreExecute() {
			if(tripListItems.size()==0){
				llProgress.setVisibility(View.VISIBLE);
				lv.setVisibility(View.GONE);
			}
			super.onPreExecute();
		}

		@Override
		protected JSONObject doInBackground(String... params) {
			SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.US);
			cStart.setTimeInMillis(0);
			cEnd.setTimeInMillis(System.currentTimeMillis());
	        requestParams.add(new BasicNameValuePair("page", "1"));
	        requestParams.add(new BasicNameValuePair("per_page", "1000"));
	        requestParams.add(new BasicNameValuePair("start_time", sdf.format(cStart.getTime())));
	        requestParams.add(new BasicNameValuePair("end_time", sdf.format(cEnd.getTime())));
			return super.doInBackground(params);
		}
		
		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			llProgress.setVisibility(View.GONE);
			lv.setVisibility(View.VISIBLE);
		}
		
		@Override
		protected void onSuccess(JSONObject responseJSON) throws JSONException {
			JSONArray tripsJSON = responseJSON.getJSONArray("trips");
			System.out.println(responseJSON);
			
			ArrayList<Trip> trips = new ArrayList<Trip>();
			
			for (int i = 0; i < tripsJSON.length(); i++) {
				JSONObject tipJSON = tripsJSON.getJSONObject(i);
				
				System.out.println("i = "+i);
				Trip t = new Trip(
						tipJSON.optLong("id"), 
						tipJSON.optInt("harsh_events_count"), 
						Utils.fixTimezoneZ(tipJSON.optString("start_time")), 
						Utils.fixTimezoneZ(tipJSON.optString("end_time")), 
						tipJSON.optDouble("mileage"));
				trips.add(t);
			}
			
			DbHelper dbHelper = DbHelper.getInstance(getActivity());
			dbHelper.saveTrips(trips);

			cStart.setTimeInMillis(System.currentTimeMillis());
			cStart.add(Calendar.DAY_OF_YEAR, -7);
			cEnd.setTimeInMillis(System.currentTimeMillis());
			
			fillTripsListView(getTripsFromDb(cStart.getTime(), cEnd.getTime()));
			
			super.onSuccess(responseJSON);
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
		TextView tvFuel;
	}
	
	class TripsAdapter extends BaseAdapter{

		final int TRIP_ITEM = 0;
		final int HEADER_ITEM = 1;
		
		LayoutInflater lInflater;
		
		public TripsAdapter() {
		    lInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
				holder.tvEventsCount = (TextView) view.findViewById(R.id.tvCounter);
				holder.tvStartTime = (TextView) view.findViewById(R.id.tvStartTime);
				holder.tvEndTime = (TextView) view.findViewById(R.id.tvEndTime);
				holder.tvScore = (TextView) view.findViewById(R.id.tvScore);
				holder.tvFuel = (TextView) view.findViewById(R.id.tvFuel);
				view.setTag(holder);
			}
			else{
				holder = (ViewHolderTrip) view.getTag();				
			}

			holder.tvEventsCount.setText(""+t.eventsCount);
			if(t.eventsCount>0){
				holder.tvEventsCount.setTextColor(Color.parseColor("#FFFFFF"));
				holder.tvEventsCount.setBackgroundResource(R.drawable.bg_alerts_triangle_red);
			}
			else{
				holder.tvEventsCount.setTextColor(Color.parseColor("#a1a6ad"));
				holder.tvEventsCount.setBackgroundResource(R.drawable.bg_alerts_triangle_gray);				
			}
			
			String grade = t.grade;
			holder.tvScore.setText(grade);
			if(grade.contains("A") || grade.contains("B")){
				holder.tvScore.setBackgroundResource(R.drawable.circle_score_green);
			}
			else if(grade.contains("C")){
				holder.tvScore.setBackgroundResource(R.drawable.circle_score_orange);
			}
			else if(grade.contains("D") || grade.contains("E") || grade.contains("F")){
				holder.tvScore.setBackgroundResource(R.drawable.circle_score_red);
			}
			else{
				holder.tvScore.setBackgroundResource(R.drawable.circle_score_gray);
			}
			
			holder.tvStartTime.setText(t.getStartDateString());
			holder.tvEndTime.setText(t.getEndDateString());
			if(t.distance<1000)
				holder.tvDistance.setText(new DecimalFormat("0.0").format(t.distance));
			else
				holder.tvDistance.setText(new DecimalFormat("0").format(t.distance));
			
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(getActivity(), TripActivity.class);
					intent.putExtra(VehicleEntry._ID, driver.id);
					intent.putExtra(TripActivity.EXTRA_TRIP_ID, t.id);
					startActivity(intent);
				}
			});
			
			return view;
		}
		
	}

}
