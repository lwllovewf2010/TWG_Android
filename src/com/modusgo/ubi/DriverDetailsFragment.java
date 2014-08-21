package com.modusgo.ubi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.modusgo.ubi.utils.RequestGet;

public class DriverDetailsFragment extends Fragment {
	
	Driver driver;
	SharedPreferences prefs;
	
	TextView tvName;
	TextView tvVehicle;
	TextView tvLocation;
	TextView tvDate;
	ImageView imagePhoto;
	ImageView imageDiagnostics;
	ImageView imageAlerts;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.driver_info_fragment, container, false);

		((MainActivity)getActivity()).setActionBarTitle("DRIVER DETAIL");

		prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		
		if(savedInstanceState!=null){
			driver = (Driver) savedInstanceState.getSerializable(DriverActivity.SAVED_DRIVER);
		}
		else if(getArguments()!=null){
			driver = (Driver) getArguments().getSerializable(DriverActivity.SAVED_DRIVER);
		}

	    tvName = (TextView) rootView.findViewById(R.id.tvName);
	    tvVehicle = (TextView) rootView.findViewById(R.id.tvVehicle);
	    tvLocation = (TextView) rootView.findViewById(R.id.tvLocation);
	    tvDate = (TextView) rootView.findViewById(R.id.tvDate);
	    imagePhoto = (ImageView)rootView.findViewById(R.id.imagePhoto);
	    imageDiagnostics = (ImageView)rootView.findViewById(R.id.imageDiagnostics);
	    imageAlerts = (ImageView)rootView.findViewById(R.id.imageAlerts);
	    
	    updateFragment();
	    
	    imageAlerts.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getActivity(), AlertsActivity.class));			
			}
		});
	    
	    new GetDriverTask(getActivity()).execute();
		
		return rootView;
	}
	
	private void updateFragment(){
		tvName.setText(driver.name);
	    tvVehicle.setText(driver.vehicle);
	    tvLocation.setText(driver.address);
	    
	    SimpleDateFormat sdfFrom = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.getDefault());
		SimpleDateFormat sdfTo = new SimpleDateFormat("MM/dd/yyyy KK:mm aa z", Locale.getDefault());
		try {
			tvDate.setText(sdfTo.format(sdfFrom.parse(driver.lastTripDate)));
		} catch (ParseException e) {
			tvDate.setText(driver.lastTripDate);
			e.printStackTrace();
		}
	    
	    imagePhoto.setImageResource(driver.imageId);
	    
	    if(driver.diagnosticsOK){
	    	imageDiagnostics.setImageResource(R.drawable.ic_diagnostics_green);
	    }else{
	    	imageDiagnostics.setImageResource(R.drawable.ic_diagnostics_red);		    	
	    }
	    
	    if(driver.alertsOK){
	    	imageAlerts.setImageResource(R.drawable.ic_alerts_green);
	    }else{
	    	imageAlerts.setImageResource(R.drawable.ic_alerts_red);		    	
	    }
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(DriverActivity.SAVED_DRIVER, driver);
		super.onSaveInstanceState(outState);
	}
	
	class GetDriverTask extends BaseRequestAsyncTask{

		public GetDriverTask(Context context) {
			super(context);
		}

		@Override
		protected HttpResponse doInBackground(Void... params) {			
			return new RequestGet(Constants.API_BASE_URL+"drivers/"+driver.id+".json", requestParams).execute();
		}
		
		@Override
		protected void onSuccess(JSONObject responseJSON) {
			try {
				System.out.println(responseJSON);
				
				driver.name = responseJSON.getString("name");
				driver.vehicle = responseJSON.getString("year")+" "+responseJSON.getString("make")+" "+responseJSON.getString("model");
				driver.lastTripDate = responseJSON.getString("last_trip");
				driver.alerts = responseJSON.getInt("count_new_alerts");
				driver.diags = responseJSON.getInt("count_new_diags");
				driver.diagnosticsOK = responseJSON.getInt("count_new_diags") == 0 ? true : false;
				driver.alertsOK = responseJSON.getInt("count_new_alerts") == 0 ? true : false;
				driver.address = responseJSON.getJSONObject("location").getString("address");
				driver.latitude = Double.parseDouble(responseJSON.getJSONObject("location").getJSONObject("map").getString("latitude"));
				driver.longitude = Double.parseDouble(responseJSON.getJSONObject("location").getJSONObject("map").getString("longitude"));
				
				updateFragment();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			super.onSuccess(responseJSON);
		}
	}
}