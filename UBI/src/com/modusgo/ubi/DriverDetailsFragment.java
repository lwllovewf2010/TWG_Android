package com.modusgo.ubi;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
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
import android.text.TextUtils;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.modusgo.ubi.customviews.GoogleMapFragment;
import com.modusgo.ubi.customviews.GoogleMapFragment.OnMapReadyListener;
import com.modusgo.ubi.db.DbHelper;
import com.modusgo.ubi.db.VehicleContract.VehicleEntry;
import com.modusgo.ubi.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class DriverDetailsFragment extends Fragment  implements ConnectionCallbacks,
OnConnectionFailedListener, LocationListener, OnMapReadyListener {
	
	Driver driver;
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

	private GoogleMapFragment mMapFragment;
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

		driver = ((DriverActivity)getActivity()).driver;
		
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
	    
	    btnDistanceToCar.setEnabled(false);
	    btnDistanceToCar.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), FindMyCarActivity.class);
				intent.putExtra(VehicleEntry._ID, driver.id);
				startActivity(intent);			
			}
		});
	    
	    ((View)tvAlerts.getParent()).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), AlertsActivity.class);
				intent.putExtra(VehicleEntry._ID, driver.id);
				startActivity(intent);			
			}
		});
	    
	    if(prefs.getBoolean(Constants.PREF_DIAGNOSTIC, false)){
		    ((View)tvDiagnostics.getParent()).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					((DriverActivity)getActivity()).switchTab(3);
				}
			});
	    }
	    else{
	    	rootView.findViewById(R.id.spaceDiagnostics).setVisibility(View.GONE);
	    	((View)tvDiagnostics.getParent()).setVisibility(View.GONE);
	    }
	    
	    rootView.findViewById(R.id.rlLocation).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), MapActivity.class);
				intent.putExtra(VehicleEntry._ID, driver.id);
				startActivity(intent);	
			}
		});
	    
	    new GetDriverTask(getActivity()).execute("vehicles/"+driver.id+".json");
	    new GetTripsTask(getActivity()).execute("vehicles/"+driver.id+"/trips.json");
		
		return rootView;
	}
	
	@Override
    public void onMapReady() {
        mMap = mMapFragment.getMap();
        if(mMap!=null)
        	setUpMap();
    }
	
	private void updateFragment(){
		try{
			if(mMap!=null){
				mMap.clear();
				setUpMap();
			}
			updateDriverInfo();
			
	        setUpLocationClientIfNeeded();
	        mLocationClient.connect();
		}
		catch(NullPointerException e){
			e.printStackTrace();
		}
	}
	
	private void updateDriverInfo(){
		tvName.setText(driver.name);
	    tvVehicle.setText(driver.getCarFullName());
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
	    
		if(driver.photo == null || driver.photo.equals(""))
	    	imagePhoto.setImageResource(R.drawable.person_placeholder);
	    else{
	    	DisplayImageOptions options = new DisplayImageOptions.Builder()
	        .showImageOnLoading(R.drawable.person_placeholder)
	        .showImageForEmptyUri(R.drawable.person_placeholder)
	        .showImageOnFail(R.drawable.person_placeholder)
	        .cacheInMemory(true)
	        .cacheOnDisk(true)
	        .build();
	    	
	    	ImageLoader.getInstance().displayImage(driver.photo, imagePhoto, options);
	    }
	    
		if(driver.carFuelLevel>=0){
			String fuelLestString = driver.carFuelLevel+"%";
		    SpannableStringBuilder cs = new SpannableStringBuilder(fuelLestString);
		    cs.setSpan(new SuperscriptSpan(), fuelLestString.length()-1, fuelLestString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		    cs.setSpan(new RelativeSizeSpan(0.5f), fuelLestString.length()-1, fuelLestString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		    tvFuel.setText(cs);
		}
		else{
			tvFuel.setText("N/A");
		}
	    
	    if(driver.carCheckup){
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
	    
	    if(driver.lastTripId>0){
	    	rlLastTrip.findViewById(R.id.imageArrow).setVisibility(View.VISIBLE);
		    rlLastTrip.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(getActivity(), TripActivity.class);
					intent.putExtra(VehicleEntry._ID, driver.id);
					intent.putExtra(TripActivity.EXTRA_TRIP_ID, driver.lastTripId);
					startActivity(intent);
				}
			});
	    }
	    else{
	    	rlLastTrip.findViewById(R.id.imageArrow).setVisibility(View.GONE);	    	
	    }
	    
	    if(TextUtils.isEmpty(driver.lastTripDate)){
	    	rlLastTrip.setVisibility(View.GONE);
	    }
	    
        setUpMapIfNeeded();
	}
	
	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the map.
	    if (mMap == null) {
			mMapFragment = new GoogleMapFragment();
		    getChildFragmentManager().beginTransaction().replace(R.id.mapContainer, mMapFragment).commitAllowingStateLoss();
	    }
    }

    private void setUpMap() {
    	if(driver.latitude!=0 && driver.longitude!=0){
    		mMap.addMarker(new MarkerOptions().position(new LatLng(driver.latitude, driver.longitude)).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_car)));
    		float density = 1;
    		if(isAdded())
    			density = getResources().getDisplayMetrics().density;
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(driver.latitude-0.016f/density, driver.longitude), 14.0f));
    	}
    	
    	mMap.getUiSettings().setZoomControlsEnabled(false);
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
	    super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mLocationClient != null) {
            mLocationClient.disconnect();
        }
    }
    
    class GetDriverTask extends BaseRequestAsyncTask{

		public GetDriverTask(Context context) {
			super(context);
		}
		
		@Override
		protected void onError(String message) {
			//Do nothing
		}

		@Override
		protected JSONObject doInBackground(String... params) {			
			return super.doInBackground(params);
		}
		
		@Override
		protected void onSuccess(JSONObject responseJSON) throws JSONException {
			System.out.println(responseJSON);
			
			JSONObject vehicleJSON = responseJSON.getJSONObject("vehicle");
			
			driver = Driver.fromJSON(vehicleJSON);
			DbHelper dbHelper = DbHelper.getInstance(getActivity());
			dbHelper.saveDriver(driver);
			dbHelper.close();
			
			updateFragment();
			
			super.onSuccess(responseJSON);
		}
	}
    
    class GetTripsTask extends BaseRequestAsyncTask{
		
		public GetTripsTask(Context context) {
			super(context);
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected JSONObject doInBackground(String... params) {
			SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.US);
			
			Calendar cStart = Calendar.getInstance();
			Calendar cEnd = Calendar.getInstance();
			cStart.setTimeInMillis(System.currentTimeMillis());
			cStart.add(Calendar.DAY_OF_YEAR, -7);
			cEnd.setTimeInMillis(System.currentTimeMillis());
			
	        requestParams.add(new BasicNameValuePair("page", "1"));
	        requestParams.add(new BasicNameValuePair("per_page", "1000"));
	        requestParams.add(new BasicNameValuePair("start_time", sdf.format(cStart.getTime())));
	        requestParams.add(new BasicNameValuePair("end_time", sdf.format(cEnd.getTime())));
			return super.doInBackground(params);
		}
		
		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
		}
		
		@Override
		protected void onSuccess(JSONObject responseJSON) throws JSONException {
			JSONArray tripsJSON = responseJSON.getJSONArray("trips");
			
			ArrayList<Trip> trips = new ArrayList<Trip>();
			
			for (int i = 0; i < tripsJSON.length(); i++) {
				JSONObject tipJSON = tripsJSON.getJSONObject(i);
				
				Trip t = new Trip(
						tipJSON.optLong("id"), 
						tipJSON.optInt("harsh_events_count"), 
						Utils.fixTimezoneZ(tipJSON.optString("start_time")), 
						Utils.fixTimezoneZ(tipJSON.optString("end_time")), 
						tipJSON.optDouble("mileage"));
				trips.add(t);
			}
			
			DbHelper dbHelper = DbHelper.getInstance(getActivity());
			dbHelper.saveTrips(driver.id, trips);
			dbHelper.close();
			
			super.onSuccess(responseJSON);
		}
	}

}