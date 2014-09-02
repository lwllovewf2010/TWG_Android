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
	    	imagePhoto.setImageResource(driver.imageId);
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
		
		new GetAlertsTask(this).execute("drivers/"+driver.id+"/alerts.json");
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("id", driverIndex);
		super.onSaveInstanceState(outState);
	}
	
	class Alert{
		
		int type;
		String eventTitle;
		String date;
		long tripId;
		
		public Alert(int type, String eventTitle, String date, long tripId) {
			super();
			this.type = type;
			this.eventTitle = eventTitle;
			this.date = date;
			this.tripId = tripId;
		}
		
	}
	
	class AlertsAdapter extends ArrayAdapter<Alert>{
		
		SimpleDateFormat sdfFrom = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.getDefault());
		SimpleDateFormat sdfTo = new SimpleDateFormat("MM/dd/yyyy KK:mm aa z", Locale.getDefault());
		
		public AlertsAdapter(Context context, int resource, List<Alert> objects) {
			super(context, resource, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			final Alert alert = getItem(position);
			
			View view = convertView;
		    if (view == null) {
		    	LayoutInflater lInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = lInflater.inflate(R.layout.alerts_item, parent, false);
		    }

		    ((TextView)view.findViewById(R.id.tvEvent)).setText(alert.eventTitle);	
		    try {
				((TextView) view.findViewById(R.id.tvDate)).setText(sdfTo.format(sdfFrom.parse(alert.date)));
			} catch (ParseException e) {
				((TextView) view.findViewById(R.id.tvDate)).setText(alert.date);
				e.printStackTrace();
			}
		    
		    switch (alert.type) {
			case 0:
				((ImageView)view.findViewById(R.id.imageIcon)).setImageResource(R.drawable.ic_alerts_red);
				break;
			case 1:
				((ImageView)view.findViewById(R.id.imageIcon)).setImageResource(R.drawable.ic_diagnostics_red);
				break;
			default:
				break;
			}
		    
		    view.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(AlertsActivity.this, TripActivity.class);
					intent.putExtra("id", driverIndex);
					intent.putExtra(TripActivity.EXTRA_TRIP_ID, alert.tripId);
					startActivity(intent);		
				}
			});
			
			return view;
		}
		
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
				alerts.add(new Alert(0, alertJSON.getString("title"), Utils.fixTimezoneZ(alertJSON.getString("created_at")), alertJSON.getLong("trip_id")));
			}
			
			adapter.notifyDataSetChanged();			
			
			super.onSuccess(responseJSON);
		}
	}
	
}
