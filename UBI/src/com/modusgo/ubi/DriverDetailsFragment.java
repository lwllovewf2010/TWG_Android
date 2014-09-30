package com.modusgo.ubi;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.modusgo.demo.R;
import com.modusgo.ubi.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class DriverDetailsFragment extends Fragment  implements ConnectionCallbacks,
OnConnectionFailedListener, LocationListener{
	
	Driver driver;
	DriversHelper dHelper;
	int driverIndex = 0;
	SharedPreferences prefs;
	
	TextView tvName;
	TextView tvVehicle;
	TextView tvLocation;
	TextView tvDate;
	ImageView imagePhoto;
	TextView tvDistanceToCar;
	TextView tvFuel;
	TextView tvDiagnostics;
	TextView tvAlerts;
	
	View btnDistanceToCar;
	View rlLastTrip;

    private GoogleMap mMap;
    
    private LocationClient mLocationClient;

    // These settings are the same as the settings for the map. They will in fact give you updates
    // at the maximal rates currently possible.
    private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(5000)         // 5 seconds
            .setFastestInterval(3000)    // every 3 seconds
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	
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
	    tvDistanceToCar = (TextView)rootView.findViewById(R.id.tvDistanceToCar);
	    tvFuel = (TextView)rootView.findViewById(R.id.tvFuel);
	    tvDiagnostics = (TextView)rootView.findViewById(R.id.tvDiagnosticsCount);
	    tvAlerts = (TextView)rootView.findViewById(R.id.tvAlertsCount);
	    btnDistanceToCar = (View)tvDistanceToCar.getParent();
	    rlLastTrip = rootView.findViewById(R.id.rlDate);
	    
	    updateFragment();

	    rlLastTrip.findViewById(R.id.imageArrow).setVisibility(View.GONE);
	    
	    btnDistanceToCar.setEnabled(false);
	    btnDistanceToCar.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), FindMyCarActivity.class);
				intent.putExtra("id", driverIndex);
				startActivity(intent);			
			}
		});
	    
	    ((View)tvAlerts.getParent()).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), AlertsActivity.class);
				intent.putExtra("id", driverIndex);
				startActivity(intent);			
			}
		});
	    
	    ((View)tvDiagnostics.getParent()).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				((DriverActivity)getActivity()).switchTab(3);
			}
		});
	    
	    rootView.findViewById(R.id.rlLocation).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), MapActivity.class);
				intent.putExtra("id", driverIndex);
				startActivity(intent);	
			}
		});
		
		return rootView;
	}
	
	private void updateFragment(){
		tvName.setText(driver.name);
	    tvVehicle.setText(driver.vehicle);
	    if(driver.address == null || driver.address.equals(""))
	    	tvLocation.setText("Unknown address");
	    else
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
	    
		if(driver.fuelLeft>=0){
			String fuelLestString = driver.fuelLeft+"%";
		    SpannableStringBuilder cs = new SpannableStringBuilder(fuelLestString);
		    cs.setSpan(new SuperscriptSpan(), fuelLestString.length()-1, fuelLestString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		    cs.setSpan(new RelativeSizeSpan(0.5f), fuelLestString.length()-1, fuelLestString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		    tvFuel.setText(cs);
		}
		else{
			tvFuel.setText("N/A");
		}
	    
	    if(driver.diags<=0){
	    	tvDiagnostics.setText("");
	    	tvDiagnostics.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_diagnostics_green_medium, 0, 0, 0);
	    }else{
	    	tvDiagnostics.setText(""/*+driver.diags*/);
	    	tvDiagnostics.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_diagnostics_red_medium, 0, 0, 0);		    	
	    }
	    
	    if(driver.alerts<=0){
	    	tvAlerts.setText("");
	    	tvAlerts.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_alerts_green_medium, 0, 0, 0);
	    }else{
	    	if(driver.alerts==0)
	    		tvAlerts.setText("…");
	    	else
	    		tvAlerts.setText(""+driver.alerts);
	    	tvAlerts.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_alerts_red_medium, 0, 0, 0);		    	
	    }
	    
	    if(driver.lastTripId!=-1){
	    	rlLastTrip.findViewById(R.id.imageArrow).setVisibility(View.VISIBLE);
		    rlLastTrip.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(getActivity(), TripActivity.class);
					intent.putExtra("id", driverIndex);
					intent.putExtra(TripActivity.EXTRA_TRIP_ID, driver.lastTripId);
					startActivity(intent);
				}
			});
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
    		mMap.addMarker(new MarkerOptions().position(new LatLng(driver.latitude, driver.longitude)).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_car)));
    		final float density = getResources().getDisplayMetrics().density;			
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(driver.latitude-0.016f/density, driver.longitude), 14.0f));
    	}
    	
    	mMap.getUiSettings().setZoomControlsEnabled(false);
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
	
    private void setUpLocationClientIfNeeded() {
        if (mLocationClient == null) {
            mLocationClient = new LocationClient(
                    getActivity().getApplicationContext(),
                    this,  // ConnectionCallbacks
                    this); // OnConnectionFailedListener
        }
    }
    
    float distanceToCar[] = new float[3];
    DecimalFormat dsitanceFormat = new DecimalFormat("0.0");
    
    @Override
    public void onLocationChanged(Location location) {
		btnDistanceToCar.setEnabled(true);
		
		Location.distanceBetween(driver.latitude, driver.longitude, location.getLatitude(), location.getLongitude(), distanceToCar);
		float distance = Utils.metersToMiles(distanceToCar[0]);
		
		if(distance>=10000){
			tvDistanceToCar.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
			tvDistanceToCar.setText(""+Math.round(distance));
		}
		else if(distance>=1000){
			tvDistanceToCar.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
			tvDistanceToCar.setText(""+Math.round(distance));
		}
		else if(distance>=100){
			tvDistanceToCar.setText(""+Math.round(distance));			
		}
		else
			tvDistanceToCar.setText(dsitanceFormat.format(distance));

        if (mLocationClient != null) {
            mLocationClient.disconnect();
        }
    }
	
	@Override
    public void onConnected(Bundle connectionHint) {
        mLocationClient.requestLocationUpdates(
                REQUEST,
                this);  // LocationListener
    }
	
	/**
     * Callback called when disconnected from GCore. Implementation of {@link ConnectionCallbacks}.
     */
    @Override
    public void onDisconnected() {
        // Do nothing
    }

    /**
     * Implementation of {@link OnConnectionFailedListener}.
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
    	//Do nothing
    }
    
    @Override
	public void onResume() {
	    new GetDriverTask(getActivity()).execute("drivers/"+driver.id+".json");
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mLocationClient != null) {
            mLocationClient.disconnect();
        }
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
		protected void onSuccess(JSONObject responseJSON) throws JSONException {
			
			driver.name = responseJSON.optString("name");
			driver.vehicle = responseJSON.optString("year")+" "+responseJSON.optString("make")+" "+responseJSON.optString("model");
			driver.VIN = responseJSON.optString("vin");
			driver.lastTripDate = responseJSON.isNull("last_trip") ? "Undefined" : Utils.fixTimezoneZ(responseJSON.optString("last_trip"));
			driver.lastTripId = responseJSON.optLong("last_trip_id",-1);
			driver.profileDate = Utils.fixTimezoneZ(responseJSON.optString("profile_date"));
			driver.alerts = responseJSON.optInt("count_alerts");
			driver.diags = responseJSON.optInt("count_diags");
			
			JSONObject locationJSON = responseJSON.optJSONObject("location");
			if(locationJSON!=null){
				driver.address = locationJSON.optString("address");
				JSONObject mapJSON = locationJSON.getJSONObject("map");
				if(mapJSON!=null){
					driver.latitude = mapJSON.optDouble("latitude");
					driver.longitude = mapJSON.optDouble("longitude");
				}
				else{
					driver.latitude = 0;
					driver.longitude = 0;
				}
			}
			else{
				driver.address = "Undefined";
				driver.latitude = 0;
				driver.longitude = 0;
			}
				
			dHelper.setDriver(driverIndex, driver);
			
			try{
				if(mMap!=null){
					mMap.clear();
					setUpMap();
				}
				updateFragment();
				
		        setUpLocationClientIfNeeded();
		        mLocationClient.connect();
			}
			catch(NullPointerException e){
				e.printStackTrace();
			}
			super.onSuccess(responseJSON);
		}
	}
}