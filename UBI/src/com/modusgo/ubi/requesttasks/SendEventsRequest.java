package com.modusgo.ubi.requesttasks;

import java.util.ArrayList;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.modusgo.ubi.Constants;
import com.modusgo.ubi.db.TrackingContract.TrackingEntry;
import com.modusgo.ubi.db.DbHelper;

public class SendEventsRequest extends BasePostRequestAsyncTask {

	private static final float MPS_TO_KPH = 3.6f;
	
	ArrayList<Long> eventIds;
	
	public SendEventsRequest(Context context) {
		super(context);
	}
	
	@Override
	protected JSONObject doInBackground(String... params) {
		DbHelper dbHelper = DbHelper.getInstance(context);
		SQLiteDatabase db = dbHelper.openDatabase();
		Cursor c = db.query(TrackingEntry.TABLE_NAME, 
				new String[]{
				TrackingEntry._ID,
				TrackingEntry.COLUMN_NAME_TIMESTAMP,
				TrackingEntry.COLUMN_NAME_EVENT,
				TrackingEntry.COLUMN_NAME_LATITUDE,
				TrackingEntry.COLUMN_NAME_LONGITUDE,
				TrackingEntry.COLUMN_NAME_ALTITUDE,
				TrackingEntry.COLUMN_NAME_SATELITES,
				TrackingEntry.COLUMN_NAME_HEADING,
				TrackingEntry.COLUMN_NAME_SPEED,
				TrackingEntry.COLUMN_NAME_FIX_STATUS,
				TrackingEntry.COLUMN_NAME_RAW_DATA,
				TrackingEntry.COLUMN_NAME_HORIZONTAL_ACCURACY,
				TrackingEntry.COLUMN_NAME_VERTICAL_ACCURACY,
				TrackingEntry.COLUMN_NAME_BLOCKED,
				TrackingEntry.COLUMN_NAME_DRIVER_ID}, 
				TrackingEntry.COLUMN_NAME_BLOCKED+ " = ? AND "+TrackingEntry.COLUMN_NAME_DRIVER_ID + " =? ", new String[]{"0", String.valueOf(prefs.getLong(Constants.PREF_DRIVER_ID, 0))}, null, null, null, "30");
		
		JSONObject rootJSON = new JSONObject();
		JSONArray timepointsJSON = new JSONArray();
		eventIds = new ArrayList<Long>();
		
		try {
			JSONObject dataJSON = new JSONObject();
			rootJSON.put("data", dataJSON);
			
			if(c.moveToFirst()){
				while (!c.isAfterLast()) {
					JSONObject tpJSON = new JSONObject();
					JSONArray eventsJSON = new JSONArray();
					tpJSON.put("timestamp", c.getString(1));
					eventsJSON.put(c.getString(2));
					tpJSON.put("events", eventsJSON);
					JSONObject tpLocationJSON = new JSONObject();
					tpLocationJSON.put("latitude", c.getDouble(3));
					tpLocationJSON.put("longitude", c.getDouble(4));
					tpLocationJSON.put("altitude", c.getDouble(5));
					tpLocationJSON.put("satellites", c.getInt(6));
					tpLocationJSON.put("heading", c.getFloat(7));
					tpLocationJSON.put("speed", c.getFloat(8) * MPS_TO_KPH);
					tpLocationJSON.put("fix_status", c.getInt(9));
					tpJSON.put("location", tpLocationJSON);
					JSONObject tpDataJSON = new JSONObject();
					tpDataJSON.put("raw_data", c.getString(10));
					tpDataJSON.put("horizontal_accuracy", c.getFloat(11));
					tpDataJSON.put("vertical_accuracy", c.getFloat(12));
					tpJSON.put("data", tpDataJSON);
					
					timepointsJSON.put(tpJSON);
					
					eventIds.add(c.getLong(0));
					c.moveToNext();
				}
				dbHelper.setTrackingEventsBlock(eventIds, true);
			}
			else{
				status = 0;
				message = "Nothing to send";
				return null;
			}
				
			rootJSON.put("time_points", timepointsJSON);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		finally{
			c.close();
			dbHelper.closeDatabase();
			dbHelper.close();			
		}
		
		requestParams.add(new BasicNameValuePair("data",rootJSON.toString()));
		System.out.println(requestParams);
		
		return super.doInBackground("device.json");
	}
	
	@Override
	protected void onError(String message) {
		DbHelper dbHelper = DbHelper.getInstance(context);
		dbHelper.setTrackingEventsBlock(eventIds, false);
		dbHelper.close();
		Log.w("UBI", message);
	}

	@Override
	protected void onSuccess(JSONObject responseJSON) throws JSONException {
		DbHelper dbHelper = DbHelper.getInstance(context);
		dbHelper.deleteTrackingEvents(eventIds);
		dbHelper.close();
		super.onSuccess(responseJSON);
	}
}
