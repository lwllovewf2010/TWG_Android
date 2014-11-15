package com.modusgo.ubi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.modusgo.ubi.db.DbHelper;
import com.modusgo.ubi.db.VehicleContract.VehicleEntry;
import com.modusgo.ubi.requesttasks.BaseRequestAsyncTask;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;


public class AlertsActivity extends MainActivity {
	
	long vehicleId = 0;
	
    ListView lvAlerts;
    LinearLayout llProgress;
    
    ArrayList<Alert> alerts;
    AlertsAdapter adapter;
	
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
		findViewById(R.id.btnTimePeriod).setVisibility(View.GONE);
		
		llProgress = (LinearLayout) findViewById(R.id.llProgress);
		
		alerts = new ArrayList<Alert>();
		
		adapter = new AlertsAdapter(this, R.layout.alerts_item, alerts);
		
		lvAlerts = (ListView)findViewById(R.id.listViewAlerts);
		lvAlerts.setAdapter(adapter);
		
	}
	
	@Override
	protected void onResume() {
		new GetAlertsTask(this).execute("vehicles/"+vehicle.id+"/alerts.json");
		super.onResume();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong(VehicleEntry._ID, vehicleId);
		super.onSaveInstanceState(outState);
	}
	
	class Alert{
		
		long id;
		long vehicleId;
		long tripId;
		String type;
		String timestamp;
		String description;
		LatLng location;
		String seenAt;
		
		public Alert(int id) {
			super();
			this.id = id;
		}
	}
	
	class AlertsAdapter extends ArrayAdapter<Alert>{
		
		SimpleDateFormat sdfFrom = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.getDefault());
		SimpleDateFormat sdfTo = new SimpleDateFormat("MM/dd/yyyy KK:mm aa z", Locale.getDefault());
		
		
		Typeface typefaceBold = Typeface.createFromAsset(getAssets(), "fonts/EncodeSansNormal-600-SemiBold.ttf");
		Typeface typefaceLight = Typeface.createFromAsset(getAssets(), "fonts/EncodeSansNormal-300-Light.ttf");
        
		
		public AlertsAdapter(Context context, int resource, List<Alert> objects) {
			super(context, resource, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			final Alert alert = getItem(position);
			ViewHolder holder;
			
			View view = convertView;
		    if (view == null) {
		    	LayoutInflater lInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = lInflater.inflate(R.layout.alerts_item, parent, false);
				holder = new ViewHolder();
				holder.tvEvent = (TextView) view.findViewById(R.id.tvEvent);
				holder.tvDate = (TextView) view.findViewById(R.id.tvDate);
				holder.imageArrow = (ImageView) view.findViewById(R.id.imageArrow);
				view.setTag(holder);
		    }
		    else{
		    	holder = (ViewHolder) view.getTag();
		    }

		    holder.tvEvent.setText(alert.description);	
		    try {
		    	holder.tvDate.setText(sdfTo.format(sdfFrom.parse(alert.timestamp)));
			} catch (ParseException e) {
				holder.tvDate.setText(alert.timestamp);
				e.printStackTrace();
			}
		    
//		    if(alert.notViewed){
//		    	holder.tvEvent.setTypeface(typefaceBold);
//		    }
//		    else{
//		    	holder.tvEvent.setTypeface(typefaceLight);    	
//		    }
		    
//		    switch (alert.type) {
//			case 0:
//				((ImageView)view.findViewById(R.id.imageIcon)).setImageResource(R.drawable.ic_alerts_red);
//				break;
//			case 1:
//				((ImageView)view.findViewById(R.id.imageIcon)).setImageResource(R.drawable.ic_diagnostics_red);
//				break;
//			default:
//				break;
//			}
		    if(alert.tripId!=0){

		    	holder.imageArrow.setVisibility(View.VISIBLE);
			    view.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						new MarkAlertViewedTask(AlertsActivity.this, alert.id).execute("drivers/"+vehicle.id+"/alerts/"+alert.id+"/hide.json");
						Intent intent = new Intent(AlertsActivity.this, TripActivity.class);
						intent.putExtra("id", vehicleId);
						intent.putExtra(TripActivity.EXTRA_TRIP_ID, alert.tripId);
						startActivity(intent);	
					}
			    });
		    }
		    else{
		    	holder.imageArrow.setVisibility(View.INVISIBLE);
		    	view.setOnClickListener(null);
		    }
			
			return view;
		}
		
	}
	
	private class ViewHolder{
		public TextView tvEvent;
		public TextView tvDate;
		public ImageView imageArrow;
	}
	
	class GetAlertsTask extends BaseRequestAsyncTask{
		
		public GetAlertsTask(Context context) {
			super(context);
		}
		
		@Override
		protected void onPreExecute() {
			llProgress.setVisibility(View.VISIBLE);
			lvAlerts.setVisibility(View.GONE);
			super.onPreExecute();
		}
		
		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			llProgress.setVisibility(View.GONE);
			lvAlerts.setVisibility(View.VISIBLE);
		}

		@Override
		protected JSONObject doInBackground(String... params) {
	        requestParams.add(new BasicNameValuePair("vehicle_id", ""+vehicle.id));
	        requestParams.add(new BasicNameValuePair("page", "1"));
	        requestParams.add(new BasicNameValuePair("per_page", "1000"));
	        
			return super.doInBackground(params);
		}
		
		@Override
		protected void onSuccess(JSONObject responseJSON) throws JSONException {
			JSONArray alertsJSON = responseJSON.getJSONArray("alerts");
			alerts.clear();
			for (int i = 0; i < alertsJSON.length(); i++) {
				JSONObject alertJSON = alertsJSON.getJSONObject(i);
				Alert a = new Alert(alertJSON.optInt("id"));
				a.vehicleId = alertJSON.optLong("vehicle_id");
				a.tripId = alertJSON.optLong("trip_id");
				a.type = alertJSON.optString("uuid");
				a.timestamp = alertJSON.optString("timestamp");
				a.description = alertJSON.optString("description");
				if(alertJSON.has("location")){
					JSONObject locationJSON = alertJSON.getJSONObject("location");
					a.location = new LatLng(locationJSON.optDouble("latitude"), locationJSON.optDouble("longitude"));					
				}
				else
					a.location = new LatLng(0, 0);
				a.seenAt = alertJSON.optString("seen_at");
				alerts.add(a);
			}
			
			adapter.notifyDataSetChanged();			
			
			super.onSuccess(responseJSON);
		}
	}
	
	class MarkAlertViewedTask extends BaseRequestAsyncTask{
		
		long alertId;
		
		public MarkAlertViewedTask(Context context, long alertId) {
			super(context);
			this.alertId = alertId;
		}

		@Override
		protected JSONObject doInBackground(String... params) {
	        requestParams.add(new BasicNameValuePair("vehicle_id", ""+vehicle.id));
	        requestParams.add(new BasicNameValuePair("alert_id", ""+alertId));
			return super.doInBackground(params);
		}
	}
	
}
