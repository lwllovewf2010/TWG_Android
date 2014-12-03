package com.modusgo.ubi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.farmers.ubi.R;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.daimajia.swipe.implments.SwipeItemMangerImpl.Mode;
import com.google.android.gms.maps.model.LatLng;
import com.modusgo.ubi.db.AlertContract.AlertEntry;
import com.modusgo.ubi.db.DbHelper;
import com.modusgo.ubi.db.VehicleContract.VehicleEntry;
import com.modusgo.ubi.requesttasks.BasePostRequestAsyncTask;
import com.modusgo.ubi.requesttasks.BaseRequestAsyncTask;
import com.modusgo.ubi.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;


public class AlertsActivity extends MainActivity {

	private final static int ALERTS_PER_REQUEST = 10;
	
	long vehicleId = 0;
	
    ListView lvAlerts;
    SwipeRefreshLayout lRefresh;
    
    ArrayList<Alert> alerts;
    AlertsAdapter adapter;

	private boolean offlineMode = false;
	private boolean thereAreOlderTrips = true;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_alerts);
		super.onCreate(savedInstanceState);

		setActionBarTitle("ALERTS");
		
		if(savedInstanceState!=null){
			vehicleId = savedInstanceState.getLong(VehicleEntry._ID);
		}
		else if(getIntent()!=null){
			vehicleId = getIntent().getLongExtra(VehicleEntry._ID,0);
		}

		DbHelper dHelper = DbHelper.getInstance(this);
		vehicle = dHelper.getVehicleShort(vehicleId);
		dHelper.close();
		
		((TextView)findViewById(R.id.tvName)).setText(vehicle.name);
		
		ImageView imagePhoto = (ImageView)findViewById(R.id.imagePhoto);
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
		
		findViewById(R.id.btnSwitchDriverMenu).setVisibility(View.GONE);
		Button btnClearAll = (Button) findViewById(R.id.btnTimePeriod);
		btnClearAll.setText("Clear all");
		
		btnClearAll.setVisibility(View.VISIBLE);
		btnClearAll.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(AlertsActivity.this);
		        builder.setMessage("Clear all alerts?")
		               .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		                   public void onClick(DialogInterface dialog, int id) {
		                       new ClearAlertsTask(AlertsActivity.this).execute("vehicles/"+vehicle.id+"/alerts/clearall.json");
		                   }
		               })
		               .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		                   public void onClick(DialogInterface dialog, int id) {
		                       dialog.dismiss();
		                   }
		               });
		        builder.show();
			}
		});
		
		lRefresh = (SwipeRefreshLayout) findViewById(R.id.lRefresh);
		
		lRefresh.setColorSchemeResources(R.color.ubi_gray, R.color.ubi_green, R.color.ubi_orange, R.color.ubi_red);
		lRefresh.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				new GetAlertsTask(getApplicationContext(), true).execute("vehicles/"+vehicle.id+"/alerts.json");
			}
		});
		
		alerts = new ArrayList<Alert>();
		
		updateAlertsList();
		
		adapter = new AlertsAdapter();
		adapter.setMode(Mode.Single);
		
		lvAlerts = (ListView)findViewById(R.id.listViewAlerts);
		lvAlerts.setAdapter(adapter);
		lvAlerts.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				alerts.get(position).seenAt = "1";
				Intent intent = new Intent(AlertsActivity.this, AlertMapActivity.class);
				intent.putExtra(VehicleEntry._ID, vehicleId);
				intent.putExtra(AlertMapActivity.EXTRA_ALERT_ID, alerts.get(position).id);
				startActivity(intent);
			}
		});
		
		new GetAlertsTask(this, true).execute("vehicles/"+vehicle.id+"/alerts.json");
		
		if(!prefs.getBoolean(Constants.PREF_ALERTS_DELETE_POPUP_SHOWED, false)){
			AlertDialog.Builder builder = new AlertDialog.Builder(AlertsActivity.this);
	        builder.setMessage("Swipe left to delete alerts from your list.")
	               .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                	   prefs.edit().putBoolean(Constants.PREF_ALERTS_DELETE_POPUP_SHOWED, true).commit();
	                       dialog.dismiss();
	                   }
	               });
	        builder.show();
		}
	}
	
	private ArrayList<Alert> getAlertsFromDB(String endDate, int count){
		ArrayList<Alert> alerts = new ArrayList<Alert>();
		
		DbHelper dbHelper = DbHelper.getInstance(this);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		Cursor c = db.query(AlertEntry.TABLE_NAME, 
				new String[]{
				AlertEntry._ID,
				AlertEntry.COLUMN_NAME_VEHICLE_ID,
				AlertEntry.COLUMN_NAME_TRIP_ID,
				AlertEntry.COLUMN_NAME_TYPE,
				AlertEntry.COLUMN_NAME_TIMESTAMP,
				AlertEntry.COLUMN_NAME_DESCRIPTION,
				AlertEntry.COLUMN_NAME_LATITUDE,
				AlertEntry.COLUMN_NAME_LONGITUDE,
				AlertEntry.COLUMN_NAME_SEEN_AT}, 
				AlertEntry.COLUMN_NAME_VEHICLE_ID+" = " + Long.toString(vehicle.id) + " AND " +
				"datetime(" + AlertEntry.COLUMN_NAME_TIMESTAMP + ")<datetime('"+ Utils.fixTimezoneZ(Utils.fixTimeZoneColon(endDate)) + "')", null, null, null, "datetime("+AlertEntry.COLUMN_NAME_TIMESTAMP+") DESC", ""+count);
		
		if(c.moveToFirst()){
			while (!c.isAfterLast()) {
				Alert a = new Alert(c.getLong(0));
				a.vehicleId = c.getLong(1);
				a.tripId = c.getLong(2);
				a.type = c.getString(3);
				a.timestamp = c.getString(4);
				a.description = c.getString(5);
				a.location = new LatLng(c.getDouble(6), c.getDouble(7));
				a.seenAt = c.getString(8);
				alerts.add(a);
				c.moveToNext();
			}
		}
		c.close();		
		db.close();
		dbHelper.close();
		
		return alerts;
	}
	
	private void updateAlertsList() {
		if(adapter!=null){
			adapter.notifyDataSetChanged();
		}
	}
	
	@Override
	protected void onResume() {
		updateAlertsList();
		Utils.gaTrackScreen(this, "Alerts Screen");
		super.onResume();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong(VehicleEntry._ID, vehicleId);
		super.onSaveInstanceState(outState);
	}
	
	class AlertsAdapter extends BaseSwipeAdapter{

		SimpleDateFormat sdfFrom = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.getDefault());
		SimpleDateFormat sdfTo = new SimpleDateFormat("MM/dd/yyyy KK:mm aa z", Locale.getDefault());
		
		
		Typeface typefaceBold = Typeface.createFromAsset(getAssets(), "fonts/EncodeSansNormal-600-SemiBold.ttf");
		Typeface typefaceLight = Typeface.createFromAsset(getAssets(), "fonts/EncodeSansNormal-300-Light.ttf");

		@Override
		public int getSwipeLayoutResourceId(int arg0) {
			return R.id.lSwipe;
		}

		@Override
		public int getCount() {
			return alerts.size();
		}
		
		@Override
		public Object getItem(int position) {
			return null;
		}
		
		@Override
		public long getItemId(int position) {
			return alerts.get(position).id;
		}

		
		@Override
		public void fillValues(final int position, View convertView) {
			
			if(thereAreOlderTrips && position == getCount() - 1)
				new GetAlertsTask(getApplicationContext(), false).execute("vehicles/"+vehicle.id+"/alerts.json");
			
			final Alert alert = alerts.get(position);
			
			ViewHolder holder = new ViewHolder();
			holder.tvEvent = (TextView) convertView.findViewById(R.id.tvEvent);
			holder.tvDate = (TextView) convertView.findViewById(R.id.tvDate);
			holder.imageArrow = (ImageView) convertView.findViewById(R.id.imageArrow);
			holder.btnDelete = (Button) convertView.findViewById(R.id.btnDelete);
			

		    holder.tvEvent.setText(alert.description);	
		    try {
		    	holder.tvDate.setText(sdfTo.format(sdfFrom.parse(alert.timestamp)));
			} catch (ParseException e) {
				holder.tvDate.setText(alert.timestamp);
				e.printStackTrace();
			}
		    
		    if(TextUtils.isEmpty(alert.seenAt)){
		    	holder.tvEvent.setTypeface(typefaceBold);
		    }
		    else{
		    	holder.tvEvent.setTypeface(typefaceLight);    	
		    }
		    
		    holder.btnDelete.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					new DeleteAlertTask(AlertsActivity.this, alert.id).execute("vehicles/"+vehicle.id+"/alerts/"+alert.id+"/delete.json");
					mItemManger.closeItem(position);
					alerts.remove(alert);
					adapter.notifyDataSetChanged();
				}
			});
		    
//		    switch (alert.type) {
//			    case 0:
//			    	((ImageView)view.findViewById(R.id.imageIcon)).setImageResource(R.drawable.ic_alerts_red);
//			    	break;
//			    case 1:
//			    	((ImageView)view.findViewById(R.id.imageIcon)).setImageResource(R.drawable.ic_diagnostics_red);
//			    	break;
//			    default:
//			    	break;
//		    }
//		    if(alert.tripId!=0){
//		    	holder.imageArrow.setVisibility(View.VISIBLE);
//		    }
//		    else{
//		    	holder.imageArrow.setVisibility(View.INVISIBLE);
//		    }
		    
		}
		
		@Override
		public View generateView(int position, ViewGroup parent) {
			LayoutInflater lInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			return lInflater.inflate(R.layout.alerts_item, parent, false);
		}
	}
	
	private class ViewHolder{
		public TextView tvEvent;
		public TextView tvDate;
		public ImageView imageArrow;
		public Button btnDelete;
	}
	
	class GetAlertsTask extends BaseRequestAsyncTask{

		boolean loadNewAlerts = false;
		
		public GetAlertsTask(Context context, boolean getNewTrips) {
			super(context);
			this.loadNewAlerts = getNewTrips;
		}
		
		@Override
		protected void onPreExecute() {
			lRefresh.setRefreshing(true);
			super.onPreExecute();
		}
		
		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			lRefresh.setRefreshing(false);
		}

		@Override
		protected JSONObject doInBackground(String... params) {
	        requestParams.add(new BasicNameValuePair("vehicle_id", ""+vehicle.id));
			requestParams.add(new BasicNameValuePair("page", "1"));
	        requestParams.add(new BasicNameValuePair("per_page", ""+ALERTS_PER_REQUEST));
	        if(loadNewAlerts){
				SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.US);
				requestParams.add(new BasicNameValuePair("end_time", sdf.format(Calendar.getInstance().getTime())));
	        }
			else
		        requestParams.add(new BasicNameValuePair("end_time", alerts.get(alerts.size()-1).timestamp));
	        
			return super.doInBackground(params);
		}
		
		@Override
		protected void onSuccess(JSONObject responseJSON) throws JSONException {
			JSONArray alertsJSON = responseJSON.getJSONArray("alerts");
			ArrayList<Alert> loadedAlerts = new ArrayList<Alert>();
			Alert firstOldAlert = null;
			if(alerts.size()>0 && !offlineMode)
				firstOldAlert = alerts.get(0);
			
			for (int i = 0; i < alertsJSON.length(); i++) {
				JSONObject alertJSON = alertsJSON.getJSONObject(i);
				Alert a = new Alert(alertJSON.optInt("id"));
				a.vehicleId = alertJSON.optLong("vehicle_id");
				a.tripId = alertJSON.optLong("trip_id");
				a.type = alertJSON.optString("uuid");
				a.timestamp = Utils.fixTimezoneZ(Utils.fixTimeZoneColon(alertJSON.optString("timestamp")));
				a.title = alertJSON.optString("title");
				a.description = alertJSON.optString("description");
				if(alertJSON.has("location")){
					JSONObject locationJSON = alertJSON.getJSONObject("location");
					a.location = new LatLng(locationJSON.optDouble("latitude"), locationJSON.optDouble("longitude"));					
				}
				else
					a.location = new LatLng(0, 0);
				a.seenAt = alertJSON.optString("seen_at");
				
				if(firstOldAlert != null && a.id == firstOldAlert.id){
					break;
				}
				
				loadedAlerts.add(a);
			}
			DbHelper dbHelper = DbHelper.getInstance(context);
			
			if(loadNewAlerts) {
				if(loadedAlerts.size()>0 || offlineMode){
					offlineMode = false;
					dbHelper.deleteAllAlerts(vehicle.id);
					dbHelper.close();
					alerts.clear();
				}
				alerts.addAll(0, loadedAlerts);
			}
			else {
				if(loadedAlerts.size() == 0)
					thereAreOlderTrips = false;
				alerts.addAll(loadedAlerts);
			}

			dbHelper.saveAlerts(vehicle.id, loadedAlerts);
			dbHelper.close();

			updateAlertsList();
			
			super.onSuccess(responseJSON);
		}
		
		@Override
		protected void onError(String message) {
			offlineMode = true;
			if(!loadNewAlerts || alerts.size()==0){
				String endDate;
				if(alerts.size()==0){
					SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.US);
					endDate = sdf.format(Calendar.getInstance().getTime());
				}
				else
					endDate = alerts.get(alerts.size()-1).timestamp;
				
				ArrayList<Alert> dbAlerts = getAlertsFromDB(endDate, ALERTS_PER_REQUEST);
				if(dbAlerts.size()!=0)
					alerts.addAll(dbAlerts);
				else
					thereAreOlderTrips = false;
				updateAlertsList();
			}
			super.onError(message);
		}
	}
	
	class DeleteAlertTask extends BasePostRequestAsyncTask{

		long alertId;
		
		public DeleteAlertTask(Context context, long alertId) {
			super(context);
			this.alertId = alertId;
		}
		
		@Override
		protected JSONObject doInBackground(String... params) {
	        requestParams.add(new BasicNameValuePair("vehicle_id", ""+vehicle.id));
	        requestParams.add(new BasicNameValuePair("alert_id", ""+alertId));
			return super.doInBackground(params);
		}
		@Override
		protected void onSuccess(JSONObject responseJSON) throws JSONException {
			DbHelper dbHelper = DbHelper.getInstance(context);
			dbHelper.deleteAlert(vehicle.id, alertId);
			dbHelper.close();
			super.onSuccess(responseJSON);
		}
		
		@Override
		protected void onError(String message) {
			// Do nothing
		}	
	}
	
	class ClearAlertsTask extends BasePostRequestAsyncTask{

		public ClearAlertsTask(Context context) {
			super(context);
		}

		@Override
		protected void onPreExecute() {
			lRefresh.setRefreshing(true);
			super.onPreExecute();
		}
		
		@Override
		protected JSONObject doInBackground(String... params) {
	        requestParams.add(new BasicNameValuePair("vehicle_id", ""+vehicle.id));
			return super.doInBackground(params);
		}
		
		@Override
		protected void onPostExecute(JSONObject result) {
			lRefresh.setRefreshing(false);
			super.onPostExecute(result);
		}
		
		@Override
		protected void onSuccess(JSONObject responseJSON) throws JSONException {
			DbHelper dbHelper = DbHelper.getInstance(context);
			dbHelper.deleteAllAlerts(vehicle.id);
			dbHelper.close();
			updateAlertsList();
			super.onSuccess(responseJSON);
		}
	}
}
