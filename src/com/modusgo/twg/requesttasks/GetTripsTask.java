package com.modusgo.twg.requesttasks;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.modusgo.twg.Constants;
import com.modusgo.twg.DriverActivity;
import com.modusgo.twg.Trip;
import com.modusgo.twg.TripActivity;
import com.modusgo.twg.Vehicle;
import com.modusgo.twg.Trip.Point;
import com.modusgo.twg.db.DbHelper;
import com.modusgo.twg.db.TripContract.TripEntry;
import com.modusgo.twg.utils.Utils;

public class GetTripsTask extends BaseRequestAsyncTask
{
	private final static String TAG = "GetTripsTask";
	
	private final static int TRIPS_PER_REQUEST = 1000;

	boolean getNewTrips = false;
	private Calendar cEndTime = Calendar.getInstance();
	ArrayList<Trip> trips = null;
	private boolean offlineMode = false;
	private boolean thereAreOlderTrips = true;

	Vehicle vehicle;
	SharedPreferences prefs;

	public GetTripsTask(Context context, boolean getNewTrips)
	{
		super(context);

		vehicle = ((DriverActivity) context).vehicle;
		trips = new ArrayList<Trip>();

		this.getNewTrips = getNewTrips;
		if(getNewTrips) 
		{
			cEndTime.setTimeInMillis(System.currentTimeMillis());
		}
		else
		{
			cEndTime.setTime(trips.get(trips.size() - 1).getStartDate());
		}
		
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		//Initialize the trips array with the current trips in the database
		Calendar now = Calendar.getInstance();
		trips = getTripsFromDb(now.getTime(), TRIPS_PER_REQUEST);
	}

	@Override
	protected void onPreExecute()
	{
		// lRefresh.setRefreshing(true);
		super.onPreExecute();
	}

	@Override
	protected JSONObject doInBackground(String... params)
	{
		SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.US);
		requestParams.add(new BasicNameValuePair("vehicle_id", "" + vehicle.id));
		requestParams.add(new BasicNameValuePair("page", "1"));
		requestParams.add(new BasicNameValuePair("per_page", "" + TRIPS_PER_REQUEST));
		requestParams.add(new BasicNameValuePair("end_time", sdf.format(cEndTime.getTime())));
		return super.doInBackground(params);
	}

	@Override
	protected void onPostExecute(JSONObject result)
	{
		super.onPostExecute(result);
	}

	@Override
	protected void onSuccess(JSONObject responseJSON) throws JSONException
	{
		JSONArray tripsJSON = responseJSON.getJSONArray("trips");
		Log.d(TAG, responseJSON.toString());

		if(offlineMode && getNewTrips)
		{
			trips.clear();
			offlineMode = false;
			thereAreOlderTrips = true;
		}

		boolean clearTrips = true;

		ArrayList<Trip> newTrips = new ArrayList<Trip>();
		Trip firstOldTrip = null;
		if(trips.size() > 0)
			firstOldTrip = trips.get(0);

		for(int i = 0; i < tripsJSON.length(); i++)
		{
			JSONObject tripJSON = tripsJSON.getJSONObject(i);

			Log.d(TAG, "i = " + i);
			Trip t = new Trip(prefs, tripJSON.optLong("id"), tripJSON.optInt("harsh_events_count"),
					Utils.fixTimezoneZ(tripJSON.optString("start_time")), Utils.fixTimezoneZ(tripJSON
							.optString("end_time")), tripJSON.optDouble("mileage"));
			t.grade = tripJSON.optString("grade");
			t.fuel = (float) tripJSON.optDouble("fuel_used", -1);
			t.fuelUnit = tripJSON.optString("fuel_unit");
			t.fuelCost = (float) tripJSON.optDouble("fuel_cost");
			t.fuelStatus = tripJSON.optString("fuel_status");
			t.updatedAt = tripJSON.optString("updated_at");
			t.hidden = tripJSON.optBoolean("hidden");

			if(firstOldTrip != null && t.id == firstOldTrip.id)
			{
				clearTrips = false;
				break;
			}
			newTrips.add(t);
		}

		DbHelper dbHelper = DbHelper.getInstance(context);
		dbHelper.saveTrips(vehicle.id, newTrips);
		dbHelper.close();

		if(getNewTrips)
		{
			if(clearTrips)
			{
				trips.clear();
			}

			trips.addAll(0, newTrips);
		} else
		{
			if(newTrips.size() == 0)
				thereAreOlderTrips = false;

			trips.addAll(getTripsFromDb(cEndTime.getTime(), TRIPS_PER_REQUEST));
		}

		// updateTripsListView();

		super.onSuccess(responseJSON);
	}

	/**
	 * getTripsFromDb
	 * 
	 * @param endDate
	 * @param count
	 * @return
	 */
	private ArrayList<Trip> getTripsFromDb(Date endDate, int count)
	{
		DbHelper dbHelper = DbHelper.getInstance(context);
		SQLiteDatabase db = dbHelper.openDatabase();

		SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.US);

		Cursor c = db.query(
				TripEntry.TABLE_NAME,
				new String[]
				{ TripEntry._ID, TripEntry.COLUMN_NAME_EVENTS_COUNT, TripEntry.COLUMN_NAME_START_TIME,
						TripEntry.COLUMN_NAME_END_TIME, TripEntry.COLUMN_NAME_DISTANCE, TripEntry.COLUMN_NAME_GRADE,
						TripEntry.COLUMN_NAME_FUEL, TripEntry.COLUMN_NAME_FUEL_UNIT, TripEntry.COLUMN_NAME_FUEL_STATUS,
						TripEntry.COLUMN_NAME_FUEL_COST, TripEntry.COLUMN_NAME_VIEWED_AT,
						TripEntry.COLUMN_NAME_UPDATED_AT },
				TripEntry.COLUMN_NAME_VEHICLE_ID + " = " + vehicle.id + " AND " + TripEntry.COLUMN_NAME_HIDDEN
						+ " = 0 AND " + "datetime(" + TripEntry.COLUMN_NAME_START_TIME + ")<datetime('"
						+ Utils.fixTimeZoneColon(sdf.format(endDate)) + "')", null, null, null, "datetime("
						+ TripEntry.COLUMN_NAME_START_TIME + ") DESC", "" + count);

		Log.d(TAG, "trips from db: " + c.getCount());

		ArrayList<Trip> trips = new ArrayList<Trip>();
		if(c.moveToFirst())
		{
			while(!c.isAfterLast())
			{
				Trip t = new Trip(prefs, c.getLong(0), c.getInt(1), c.getString(2), c.getString(3), c.getDouble(4),
						c.getString(5));
				t.fuel = c.getFloat(6);
				t.fuelUnit = c.getString(7);
				t.fuelStatus = c.getString(8);
				t.fuelCost = c.getFloat(9);
				t.viewedAt = c.getString(10);
				t.updatedAt = c.getString(11);

				try
				{
					if(!TextUtils.isEmpty(t.viewedAt) && !TextUtils.isEmpty(t.updatedAt))
					{
						Calendar cViewedAt = Calendar.getInstance();
						cViewedAt.setTime(sdf.parse(t.viewedAt));
						Calendar cUpdatedAt = Calendar.getInstance();
						cUpdatedAt.setTime(sdf.parse(t.updatedAt));

						if(cViewedAt.after(cUpdatedAt))
							t.viewed = true;
					}

				} catch(ParseException e)
				{
					e.printStackTrace();
				}

				trips.add(t);
				c.moveToNext();
			}
		}
		c.close();
		dbHelper.closeDatabase();
		dbHelper.close();

		Log.d(TAG, "trips array " + trips.size());

		return trips;
	}

	@Override
	protected void onError(String message)
	{
		// offlineMode = true;
		// if(!getNewTrips){
		// ArrayList<Trip> dbTrips = getTripsFromDb(cEndTime.getTime(),
		// TRIPS_PER_REQUEST);
		// if(dbTrips.size()!=0)
		// trips.addAll(dbTrips);
		// else
		// thereAreOlderTrips = false;
		// updateTripsListView();
		// }
	}
}
