package com.modusgo.ubi.requesttasks;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;

import com.modusgo.ubi.Constants;
import com.modusgo.ubi.Trip;
import com.modusgo.ubi.TripsFragment;
import com.modusgo.ubi.db.DbHelper;
import com.modusgo.ubi.utils.Utils;

public class GetTripsTask extends BaseRequestAsyncTask{

	public final static String TAG_TRIP = "Trips";

	boolean getNewTrips = false;
	private Calendar cEndTime = Calendar.getInstance();
	ArrayList<Trip> trips;
	DbHelper dbHelper;
	SharedPreferences prefs;
	private long mId;
	private FragmentManager mFragmentManager;
	private boolean offlineMode = false;

	public GetTripsTask(Context context, boolean getNewTrips, long id, FragmentManager fragmentManager) {
		super(context);
		trips = new ArrayList<Trip>();
		mFragmentManager = fragmentManager;
		mId = id;
		dbHelper = DbHelper.getInstance(context);
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		trips.addAll(dbHelper.getTripsFromDb(Calendar.getInstance().getTime(), TripsFragment.TRIPS_PER_REQUEST, mId, prefs));
		this.getNewTrips = getNewTrips;
		if(getNewTrips)
			cEndTime.setTimeInMillis(System.currentTimeMillis());
		else
			cEndTime.setTime(trips.get(trips.size()-1).getStartDate());
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected JSONObject doInBackground(String... params) {
		SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.US);
		requestParams.add(new BasicNameValuePair("vehicle_id", ""+mId));
		requestParams.add(new BasicNameValuePair("page", "1"));
		requestParams.add(new BasicNameValuePair("per_page", ""+ TripsFragment.TRIPS_PER_REQUEST));
		requestParams.add(new BasicNameValuePair("end_time", sdf.format(cEndTime.getTime())));
		return super.doInBackground(params);
	}

	@Override
	protected void onPostExecute(JSONObject result) {
		super.onPostExecute(result);
	}

	@Override
	protected void onSuccess(JSONObject responseJSON) throws JSONException {
		JSONArray tripsJSON = responseJSON.getJSONArray("trips");
		System.out.println(responseJSON);

		if(offlineMode && getNewTrips){
			trips.clear();
			offlineMode = false;
			if(mFragmentManager != null)
				if(mFragmentManager.findFragmentByTag(TAG_TRIP) instanceof TripsFragment){
					((TripsFragment)mFragmentManager.findFragmentByTag(TAG_TRIP)).setThereOldTrip(true);
				}
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
					prefs,
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

		DbHelper dbHelper = DbHelper.getInstance(context);
		dbHelper.saveTrips(mId, newTrips);
		dbHelper.close();

		if(getNewTrips) {
			if(clearTrips){
				trips.clear();
			}

			trips.addAll(0, newTrips);
		}
		else {
			if(newTrips.size() == 0)
				if(mFragmentManager != null)
					if(mFragmentManager.findFragmentByTag(TAG_TRIP) instanceof TripsFragment){
						((TripsFragment)mFragmentManager.findFragmentByTag(TAG_TRIP)).setThereOldTrip(false);
					}

			trips.addAll(dbHelper.getTripsFromDb(cEndTime.getTime(), TripsFragment.TRIPS_PER_REQUEST, mId, prefs));
		}
		if(mFragmentManager != null)
			if(mFragmentManager.findFragmentByTag(TAG_TRIP) instanceof TripsFragment){
				((TripsFragment)mFragmentManager.findFragmentByTag(TAG_TRIP)).updateTripsListView();
			}


		super.onSuccess(responseJSON);
	}

	@Override
	protected void onError(String message) {
		offlineMode = true;
		if(!getNewTrips){
			ArrayList<Trip> dbTrips = dbHelper.getTripsFromDb(cEndTime.getTime(), TripsFragment.TRIPS_PER_REQUEST, mId, prefs);
			if(dbTrips.size()!=0)
				trips.addAll(dbTrips);
			else
				if(mFragmentManager != null)
					if(mFragmentManager.findFragmentByTag(TAG_TRIP) instanceof TripsFragment){
						((TripsFragment)mFragmentManager.findFragmentByTag(TAG_TRIP)).setThereOldTrip(false);
					}
			if(mFragmentManager != null)
				if(mFragmentManager.findFragmentByTag(TAG_TRIP) instanceof TripsFragment){
					((TripsFragment)mFragmentManager.findFragmentByTag(TAG_TRIP)).updateTripsListView();
				}
		}
	}
}
