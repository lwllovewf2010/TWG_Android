package com.modusgo.ubi.requesttasks;

import java.util.ArrayList;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.bugsnag.MetaData;
import com.bugsnag.android.Bugsnag;
import com.modusgo.ubi.Constants;
import com.modusgo.ubi.db.DbHelper;
import com.modusgo.ubi.db.TrackingContract.TrackingEntry;

public class SendEventsRequest extends BasePostRequestAsyncTask {

	private static final float MPS_TO_KPH = 3.6f;
	
	private ArrayList<Long> eventIds;
	private int limit = 30;
	private boolean loop;
	
	public SendEventsRequest(Context context) {
		super(context);
	}
	
	public SendEventsRequest(Context context, boolean loop) {
		super(context);
		this.loop = loop;
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
				TrackingEntry.COLUMN_NAME_BLOCKED+ " = ? AND "+TrackingEntry.COLUMN_NAME_DRIVER_ID + " =? ", new String[]{"0", String.valueOf(prefs.getLong(Constants.PREF_DRIVER_ID, 0))}, null, null, null, String.valueOf(limit));
		
		JSONObject rootJSON = new JSONObject();
		JSONArray timepointsJSON = new JSONArray();
		eventIds = new ArrayList<Long>();
		
		try {
			JSONObject dataJSON = new JSONObject();
			try {
				dataJSON.put("script_version", context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			dataJSON.put("firmware", "android "+android.os.Build.VERSION.RELEASE);
			try{
				dataJSON.put("device_type", Build.BRAND +" "+ Build.MODEL);
			}
			catch(ClassCastException e){
				Bugsnag.notify(new Exception("ClassCastException deviceTypeWrong"), e.getMessage());				
				dataJSON.put("device_type", "n/a");
			}
			
			String vin = prefs.getString(Constants.PREF_JASTEC_VEHICLE_VIN, "");
			if(!TextUtils.isEmpty(vin))
				dataJSON.put("vin", vin);
			
			String dtcCodes = prefs.getString(Constants.PREF_JASTEC_DTCS, "");
			if(!TextUtils.isEmpty(dtcCodes))
				dataJSON.put("dtc_codes", dtcCodes);
			
			TelephonyManager manager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
			String carrierName = manager.getNetworkOperatorName();
			if(!TextUtils.isEmpty(carrierName))
				dataJSON.put("carrier", carrierName);
			
			rootJSON.put("data", dataJSON);
			
			if(c.moveToFirst()){
				while (!c.isAfterLast()) {
					JSONObject tpJSON = new JSONObject();
					JSONArray eventsJSON = new JSONArray();
					tpJSON.put("timestamp", c.getString(1));
					
					String event = c.getString(2);
					if(!TextUtils.isEmpty(event))
						eventsJSON.put(event);
					tpJSON.put("events", eventsJSON);
					
					JSONObject tpLocationJSON = new JSONObject();
					Double latitude = c.getDouble(3);
					Double longitude = c.getDouble(4);
					if(latitude!=0 && longitude!=0){
						tpLocationJSON.put("latitude", latitude);
						tpLocationJSON.put("longitude", longitude);
						tpLocationJSON.put("altitude", c.getDouble(5));
						tpLocationJSON.put("satellites", c.getInt(6));
						tpLocationJSON.put("heading", c.getFloat(7));
						tpLocationJSON.put("speed", c.getFloat(8) * MPS_TO_KPH);
						tpLocationJSON.put("fix_status", c.getInt(9));
						tpLocationJSON.put("horizontal_accuracy", c.getFloat(11));
						tpLocationJSON.put("vertical_accuracy", c.getFloat(12));
					}
					tpJSON.put("location", tpLocationJSON);
					
					JSONObject tpDataJSON = new JSONObject();
					String rawData = c.getString(10);
//					if(!TextUtils.isEmpty(rawData))
//						tpDataJSON.put("raw_data", rawData);
					if(!rawData.isEmpty()){
						String[] rawDataArray = rawData.split(", ");
						for (String string : rawDataArray) {
							String item[] = string.split(": ");
							tpDataJSON.put(item[0], item[1]);
						}
					}
							
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
		
		MetaData metaData = new MetaData();
		metaData.addToTab("Data", "raw_request_params", requestParams);
		Bugsnag.notify(new Exception("SendEventsRequest"), metaData);
		
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
		
		if(eventIds!=null && eventIds.size()>0 && loop){
			new SendEventsRequest(context, true).execute();
		}
		
		super.onSuccess(responseJSON);
	}
}
