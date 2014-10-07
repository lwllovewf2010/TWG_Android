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

import com.modusgo.demo.R;
import com.modusgo.ubi.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;


public class AlertsActivity extends MainActivity {
	
	Driver driver;
	DriversHelper dHelper;
	int driverIndex = 0;
	
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
			driverIndex = savedInstanceState.getInt("id");
		}
		else if(getIntent()!=null){
			driverIndex = getIntent().getIntExtra("id",0);
		}

		dHelper = DriversHelper.getInstance();
		driver = dHelper.getDriverByIndex(driverIndex);
		
		((TextView)findViewById(R.id.tvName)).setText(driver.name);
		
		ImageView imagePhoto = (ImageView)findViewById(R.id.imagePhoto);
	    if(driver.imageUrl == null || driver.imageUrl.equals(""))
	    	imagePhoto.setImageResource(R.drawable.person_placeholder);
	    else{
	    	DisplayImageOptions options = new DisplayImageOptions.Builder()
	        .showImageOnLoading(R.drawable.person_placeholder)
	        .showImageForEmptyUri(R.drawable.person_placeholder)
	        .showImageOnFail(R.drawable.person_placeholder)
	        .cacheInMemory(true)
	        .cacheOnDisk(true)
	        .build();
	    	
	    	ImageLoader.getInstance().displayImage(driver.imageUrl, imagePhoto, options);
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
		new GetAlertsTask(this).execute("drivers/"+driver.id+"/alerts.json");
		super.onResume();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("id", driverIndex);
		super.onSaveInstanceState(outState);
	}
	
	class Alert{
		
		int id;
		int type;
		String eventTitle;
		String date;
		long tripId;
		boolean notViewed;
		
		public Alert(int id, int type, String eventTitle, String date, boolean viewed, long tripId) {
			super();
			this.id = id;
			this.type = type;
			this.eventTitle = eventTitle;
			this.date = date;
			this.notViewed = viewed;
			this.tripId = tripId;
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
				view.setTag(holder);
		    }
		    else{
		    	holder = (ViewHolder) view.getTag();
		    }

		    holder.tvEvent.setText(alert.eventTitle);	
		    try {
		    	holder.tvDate.setText(sdfTo.format(sdfFrom.parse(alert.date)));
			} catch (ParseException e) {
				holder.tvDate.setText(alert.date);
				e.printStackTrace();
			}
		    
		    if(alert.notViewed){
		    	holder.tvEvent.setTypeface(typefaceBold);
		    }
		    else{
		    	holder.tvEvent.setTypeface(typefaceLight);    	
		    }
		    
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
		    
		    view.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					new MarkAlertViewedTask(AlertsActivity.this, alert.id).execute("drivers/"+driver.id+"/alerts/"+alert.id+"/hide.json");
					Intent intent = new Intent(AlertsActivity.this, TripActivity.class);
					intent.putExtra("id", driverIndex);
					intent.putExtra(TripActivity.EXTRA_TRIP_ID, alert.tripId);
					startActivity(intent);	
				}
			});
			
			return view;
		}
		
	}
	
	private class ViewHolder{
		public TextView tvEvent;
		public TextView tvDate;
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
	        requestParams.add(new BasicNameValuePair("driver_id", ""+driver.id));
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
				alerts.add(new Alert(alertJSON.optInt("id"), 0, alertJSON.optString("title"), Utils.fixTimezoneZ(alertJSON.optString("created_at")), alertJSON.optBoolean("show_on_mobile"), alertJSON.optLong("trip_id")));
			}
			
			adapter.notifyDataSetChanged();			
			
			super.onSuccess(responseJSON);
		}
	}
	
	class MarkAlertViewedTask extends BaseRequestAsyncTask{
		
		int alertId;
		
		public MarkAlertViewedTask(Context context, int alertId) {
			super(context);
			this.alertId = alertId;
		}

		@Override
		protected JSONObject doInBackground(String... params) {
	        requestParams.add(new BasicNameValuePair("driver_id", ""+driver.id));
	        requestParams.add(new BasicNameValuePair("alert_id", ""+alertId));
			return super.doInBackground(params);
		}
	}
	
}
