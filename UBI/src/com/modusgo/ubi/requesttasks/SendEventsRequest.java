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

import com.modusgo.ubi.db.DDEventContract.DDEventEntry;
import com.modusgo.ubi.db.DbHelper;

public class SendEventsRequest extends BasePostRequestAsyncTask {
	
	ArrayList<Long> eventIds;
	
	public SendEventsRequest(Context context) {
		super(context);
	}
	
	@Override
	protected JSONObject doInBackground(String... params) {
		DbHelper dbHelper = DbHelper.getInstance(context);
		SQLiteDatabase db = dbHelper.openDatabase();
		Cursor c = db.query(DDEventEntry.TABLE_NAME, 
				new String[]{
				DDEventEntry._ID,
				DDEventEntry.COLUMN_NAME_EVENT,
				DDEventEntry.COLUMN_NAME_TIMESTAMP}, 
				null, null, null, null, null);
		
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
					eventsJSON.put(c.getString(1));
					tpJSON.put("events", eventsJSON);
					tpJSON.put("timestamp", c.getString(2));
					
					JSONObject tpDataJSON = new JSONObject();
					tpJSON.put("data", tpDataJSON);
					
					timepointsJSON.put(tpJSON);
					
					eventIds.add(c.getLong(0));
					c.moveToNext();
				}
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
		
		return super.doInBackground("device.json");
	}
	
	@Override
	protected void onError(String message) {
		//Do nothing
		Log.w("UBI", message);
	}

	@Override
	protected void onSuccess(JSONObject responseJSON) throws JSONException {
		DbHelper dbHelper = DbHelper.getInstance(context);
		dbHelper.deleteDDEvents(eventIds);
		dbHelper.close();
		super.onSuccess(responseJSON);
	}
}
