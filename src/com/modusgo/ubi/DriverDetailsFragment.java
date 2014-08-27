package com.modusgo.ubi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.modusgo.ubi.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class DriverDetailsFragment extends Fragment {
	
	Driver driver;
	DriversHelper dHelper;
	int driverIndex = 0;
	SharedPreferences prefs;
	
	TextView tvName;
	TextView tvVehicle;
	TextView tvLocation;
	TextView tvDate;
	ImageView imagePhoto;
	TextView tvFuel;
	TextView tvDiagnostics;
	TextView tvAlerts;

    private GoogleMap mMap;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.driver_details_fragment, container, false);

		((MainActivity)getActivity()).setActionBarTitle("DRIVER DETAIL");
		
		prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		
		if(savedInstanceState!=null){
			driverIndex = savedInstanceState.getInt("id");
		}
		else if(getArguments()!=null){
			driverIndex = getArguments().getInt("id");
		}

		dHelper = DriversHelper.getInstance();
		driver = dHelper.getDriverByIndex(driverIndex);
		
	    tvName = (TextView) rootView.findViewById(R.id.tvName);
	    tvVehicle = (TextView) rootView.findViewById(R.id.tvVehicle);
	    tvLocation = (TextView) rootView.findViewById(R.id.tvLocation);
	    tvDate = (TextView) rootView.findViewById(R.id.tvDate);
	    imagePhoto = (ImageView)rootView.findViewById(R.id.imagePhoto);
	    tvFuel = (TextView)rootView.findViewById(R.id.tvFuel);
	    tvDiagnostics = (TextView)rootView.findViewById(R.id.tvDiagnosticsCount);
	    tvAlerts = (TextView)rootView.findViewById(R.id.tvAlertsCount);
	    
	    updateFragment();
	    
	    ((View)tvAlerts.getParent()).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getActivity(), AlertsActivity.class));			
			}
		});
	    
	    ((View)tvDiagnostics.getParent()).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				((DriverActivity)getActivity()).switchTab(3);
			}
		});
	    
	    new GetDriverTask(getActivity()).execute("drivers/"+driver.id+".json");
		
		return rootView;
	}
	
	private void updateFragment(){
		tvName.setText(driver.name+"'s");
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
	    
		String fuelLestString = driver.fuelLeft+"%";
	    SpannableStringBuilder cs = new SpannableStringBuilder(fuelLestString);
	    cs.setSpan(new SuperscriptSpan(), fuelLestString.length()-1, fuelLestString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	    cs.setSpan(new RelativeSizeSpan(0.5f), fuelLestString.length()-1, fuelLestString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	    tvFuel.setText(cs);
	    
	    if(driver.diagnosticsOK){
	    	tvDiagnostics.setText("");
	    	tvDiagnostics.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_diagnostics_green, 0, 0, 0);
	    }else{
	    	tvDiagnostics.setText(""/*+driver.diags*/);
	    	tvDiagnostics.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_diagnostics_red, 0, 0, 0);		    	
	    }
	    
	    if(driver.alertsOK){
	    	tvAlerts.setText("");
	    	tvAlerts.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_alerts_green, 0, 0, 0);
	    }else{
	    	tvAlerts.setText(""+driver.alerts);
	    	tvAlerts.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_alerts_red, 0, 0, 0);		    	
	    }
        setUpMapIfNeeded();
	}
	
	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the map.
	    if (mMap == null) {
	        // Try to obtain the map from the SupportMapFragment.
	    	try {
	    		mMap = ((SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		        // Check if we were successful in obtaining the map.
		        if (mMap != null)
		            setUpMap();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
	        
	    }
    }

    private void setUpMap() {
    	if(driver.latitude!=0 && driver.longitude!=0){
    		mMap.addMarker(new MarkerOptions().position(new LatLng(driver.latitude, driver.longitude)).icon(BitmapDescriptorFactory.fromResource(R.drawable.map_car_marker)));
    		final float density = getResources().getDisplayMetrics().density;			
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(driver.latitude-0.016f/density, driver.longitude), 14.0f));
    	}
    }
    
    /**** The mapfragment's id must be removed from the FragmentManager
     **** or else if the same it is passed on the next time then 
     **** app will crash ****/
    @Override
    public void onDestroyView() {
        if (mMap != null) {
        	try{
	            getActivity().getSupportFragmentManager().beginTransaction()
	                .remove(getActivity().getSupportFragmentManager().findFragmentById(R.id.map)).commitAllowingStateLoss();
        	}
        	catch(IllegalStateException e){
        		e.printStackTrace();
        	}
            mMap = null;
        }
        super.onDestroyView();
    }
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt("id", driverIndex);
		super.onSaveInstanceState(outState);
	}
	
	class GetDriverTask extends BaseRequestAsyncTask{

		public GetDriverTask(Context context) {
			super(context);
		}

		@Override
		protected JSONObject doInBackground(String... params) {			
			return super.doInBackground(params);
		}
		
		@Override
		protected void onSuccess(JSONObject responseJSON) {
			try {
				
				driver.name = responseJSON.getString("name");
				driver.vehicle = responseJSON.getString("year")+" "+responseJSON.getString("make")+" "+responseJSON.getString("model");
				driver.VIN = responseJSON.getString("vin");
				driver.lastTripDate = Utils.fixTimezoneZ(responseJSON.getString("last_trip"));
				driver.profileDate = Utils.fixTimezoneZ(responseJSON.getString("profile_date"));
				driver.alerts = responseJSON.getInt("count_new_alerts");
				driver.diags = responseJSON.getInt("count_new_diags");
				driver.diagnosticsOK = responseJSON.getInt("count_new_diags") == 0 ? true : false;
				driver.alertsOK = responseJSON.getInt("count_new_alerts") == 0 ? true : false;
				driver.address = responseJSON.getJSONObject("location").getString("address");
				driver.latitude = Double.parseDouble(responseJSON.getJSONObject("location").getJSONObject("map").getString("latitude"));
				driver.longitude = Double.parseDouble(responseJSON.getJSONObject("location").getJSONObject("map").getString("longitude"));
				
				dHelper.setDriver(driverIndex, driver);
				
				if(mMap!=null){
					mMap.clear();
					setUpMap();
				}
				updateFragment();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			super.onSuccess(responseJSON);
		}
	}
}