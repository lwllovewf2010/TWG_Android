package com.modusgo.ubi;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

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
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.modusgo.ubi.customviews.GoogleMapFragment;
import com.modusgo.ubi.customviews.GoogleMapFragment.OnMapReadyListener;
import com.modusgo.ubi.db.DbHelper;
import com.modusgo.ubi.db.VehicleContract.VehicleEntry;
import com.modusgo.ubi.requesttasks.GetVehicleRequest;
import com.modusgo.ubi.utils.TimeAgoUtils;
import com.modusgo.ubi.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class DriverDetailsFragment extends Fragment  implements ConnectionCallbacks,
OnConnectionFailedListener, LocationListener, OnMapReadyListener {
	
	private final static String DRIVER_DERAIL = "DRIVER DETAIL";
	
	Vehicle vehicle;
	SharedPreferences prefs;
	
	TextView tvName;
	TextView tvVehicle;
	TextView tvLocation;
	TextView tvDate;
	TextView tvDateLabel;
	ImageView imagePhoto;
	TextView tvDistanceToCar;
	TextView tvDistanceToCarLabel;
	TextView tvFuel;
	TextView tvDiagnostics;
	TextView tvAlerts;
	TextView tvLocationHour;
	
	View btnDistanceToCar;
	View rlLastTrip;
	View tvInTrip;
	View rlLocation;
	View spaceFuel;
	View spaceDiagnostic;

	private GoogleMapFragment mMapFragment;
    private GoogleMap mMap;
    

    private GoogleApiClient mGoogleApiClient;
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

		((MainActivity)getActivity()).setActionBarTitle(DRIVER_DERAIL);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

		vehicle = ((DriverActivity)getActivity()).vehicle;
		
	    tvName = (TextView) rootView.findViewById(R.id.tvName);
	    tvVehicle = (TextView) rootView.findViewById(R.id.tvVehicle);
	    tvLocation = (TextView) rootView.findViewById(R.id.tvLocation);
	    tvDate = (TextView) rootView.findViewById(R.id.tvDate);
	    tvDateLabel = (TextView) rootView.findViewById(R.id.tvDateLabel);
	    tvLocationHour = (TextView) rootView.findViewById(R.id.tvLocationHour);
	    imagePhoto = (ImageView)rootView.findViewById(R.id.imagePhoto);
	    tvDistanceToCar = (TextView)rootView.findViewById(R.id.tvDistanceToCar);
	    tvDistanceToCarLabel = (TextView)rootView.findViewById(R.id.tvDistanceToCarLabel);
	    tvFuel = (TextView)rootView.findViewById(R.id.tvFuel);
	    tvDiagnostics = (TextView)rootView.findViewById(R.id.tvDiagnosticsCount);
	    tvAlerts = (TextView)rootView.findViewById(R.id.tvAlertsCount);
	    btnDistanceToCar = (View)tvDistanceToCar.getParent();
	    rlLocation = rootView.findViewById(R.id.rlLocation);
	    rlLastTrip = rootView.findViewById(R.id.rlDate);
	    tvInTrip = rootView.findViewById(R.id.tvInTrip);
	    spaceFuel = rootView.findViewById(R.id.spaceFuel);
	    spaceDiagnostic = rootView.findViewById(R.id.spaceDiagnostics);
	    
	    updateFragment();
	    
	    btnDistanceToCar.setEnabled(false);
	    btnDistanceToCar.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), FindMyCarActivity.class);
				intent.putExtra(VehicleEntry._ID, vehicle.id);
				startActivity(intent);			
			}
		});
	    
	    ((View)tvAlerts.getParent()).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), AlertsActivity.class);
				intent.putExtra(VehicleEntry._ID, vehicle.id);
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
	    
	    imagePhoto.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
	        	startActivity(new Intent(getActivity(), SettingsActivity.class));
			}
		});
		
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
	        mGoogleApiClient.connect();
		}
		catch(NullPointerException e){
			e.printStackTrace();
		}
	}
	
	public void updateDriverInfo(){
		if(vehicle.id == ((DriverActivity)getActivity()).vehicle.id)
			vehicle = ((DriverActivity)getActivity()).vehicle;
		tvName.setText(vehicle.name);
	    tvVehicle.setText(vehicle.getCarFullName());
	    if(TextUtils.isEmpty(vehicle.address)){
	    	tvLocation.setText("Unknown location");
	    	rlLocation.findViewById(R.id.locationImageArrow).setVisibility(View.GONE);
	    	rlLocation.setOnClickListener(null);
	    }
	    else{
	    	tvLocation.setText(vehicle.address);
	    	rlLocation.findViewById(R.id.locationImageArrow).setVisibility(View.VISIBLE);
	    	rlLocation.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(getActivity(), MapActivity.class);
					intent.putExtra(VehicleEntry._ID, vehicle.id);
					startActivity(intent);	
				}
			});
	    }
	    
	    SimpleDateFormat sdfFrom = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.getDefault());
		SimpleDateFormat sdfTo = new SimpleDateFormat("MM/dd/yyyy KK:mm aa z", Locale.getDefault());
		
		TimeZone tzFrom = TimeZone.getTimeZone(Constants.DEFAULT_TIMEZONE);
		sdfFrom.setTimeZone(tzFrom);
		TimeZone tzTo = TimeZone.getTimeZone(prefs.getString(Constants.PREF_TIMEZONE_OFFSET, Constants.DEFAULT_TIMEZONE));
		sdfTo.setTimeZone(tzTo);
		
		try {
			tvDate.setText(sdfTo.format(sdfFrom.parse(vehicle.lastTripDate)));
		} catch (ParseException e) {
			tvDate.setText(vehicle.lastTripDate);
			e.printStackTrace();
		}
	    
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
		
		View fuelBlock = (View)tvFuel.getParent();
		View diagnosticBlock = (View)tvDiagnostics.getParent();
		
		if(vehicle.carFuelLevel>=0 && !TextUtils.isEmpty(vehicle.carFuelUnit)){
			String fuelLeftString = vehicle.carFuelLevel+vehicle.carFuelUnit;
			int fuelUnitLength = vehicle.carFuelUnit.length();
		    SpannableStringBuilder cs = new SpannableStringBuilder(fuelLeftString);
		    cs.setSpan(new SuperscriptSpan(), fuelLeftString.length()-fuelUnitLength, fuelLeftString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		    cs.setSpan(new RelativeSizeSpan(0.5f), fuelLeftString.length()-fuelUnitLength, fuelLeftString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		    tvFuel.setText(cs);
		    tvFuel.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fuel_green, 0, 0, 0);
		    fuelBlock.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Toast.makeText(getActivity(), "The percentage shown is the last known fuel level reported from your vehicle.", Toast.LENGTH_SHORT).show();
				}
			});
		}
		else{
			if(!TextUtils.isEmpty(vehicle.carFuelStatus) && !vehicle.hideEngineIcon){
				tvFuel.setText("");
				tvFuel.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fuel_green, 0, R.drawable.ic_fuel_arrow_down, 0);
				fuelBlock.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Toast.makeText(getActivity(), vehicle.carFuelStatus, Toast.LENGTH_SHORT).show();
					}
				});
			}
			else{
				spaceFuel.setVisibility(View.GONE);
				fuelBlock.setVisibility(View.GONE);
			}
		}
		
		if(vehicle.hideEngineIcon){
			spaceDiagnostic.setVisibility(View.GONE);
			diagnosticBlock.setVisibility(View.GONE);
		}
	    
	    if(vehicle.carDTCCount<=0){
	    	tvDiagnostics.setText("");
	    	tvDiagnostics.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_diagnostics_green_medium, 0, 0, 0);
	    }else{
	    	tvDiagnostics.setText(""+vehicle.carDTCCount);
	    	tvDiagnostics.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_diagnostics_red_medium, 0, 0, 0);		    	
	    }
	    
	    if(vehicle.alerts<=0){
	    	tvAlerts.setText("");
	    	tvAlerts.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_alerts_green_medium, 0, 0, 0);
	    }else{
	    	tvAlerts.setText(""+vehicle.alerts);
	    	tvAlerts.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_alerts_red_medium, 0, 0, 0);		    	
	    }
	    
	    if(vehicle.lastTripId>0 && !vehicle.inTrip){
	    	rlLastTrip.findViewById(R.id.dateImageArrow).setVisibility(View.VISIBLE);
		    rlLastTrip.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(getActivity(), TripActivity.class);
					intent.putExtra(TripActivity.EXTRA_VEHICLE_ID, vehicle.id);
					intent.putExtra(TripActivity.EXTRA_TRIP_ID, vehicle.lastTripId);
					startActivity(intent);
				}
			});
	    }
	    else{
	    	rlLastTrip.findViewById(R.id.dateImageArrow).setVisibility(View.GONE);	
	    	rlLastTrip.setOnClickListener(null);
	    }
	    
	    if(vehicle.inTrip){
	    	tvInTrip.setVisibility(View.VISIBLE);
	    	tvDate.setVisibility(View.INVISIBLE);
	    	tvDateLabel.setVisibility(View.INVISIBLE);
	    	tvLocationHour.setText(TimeAgoUtils.getTimeAgo("", getActivity()));
	    }
	    else if(TextUtils.isEmpty(vehicle.lastTripDate)){
	    	tvInTrip.setVisibility(View.GONE);
	    	tvDate.setVisibility(View.INVISIBLE);
	    	tvDateLabel.setVisibility(View.INVISIBLE);
	    	tvLocationHour.setText("");
	    }
	    else{
	    	tvInTrip.setVisibility(View.GONE);
	    	tvDate.setVisibility(View.VISIBLE);
	    	tvDateLabel.setVisibility(View.VISIBLE);
	    	tvLocationHour.setText(TimeAgoUtils.getTimeAgo(vehicle.lastTripDate, getActivity()));
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
    	if(vehicle.latitude!=0 && vehicle.longitude!=0){
    		mMap.addMarker(new MarkerOptions().position(new LatLng(vehicle.latitude, vehicle.longitude)).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_car)));
    		float density = 1;
    		if(isAdded())
    			density = getResources().getDisplayMetrics().density;
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(vehicle.latitude-0.008f/density, vehicle.longitude), 14.0f));
    	}
    	
    	mMap.getUiSettings().setZoomControlsEnabled(false);
    }
	
    private void setUpLocationClientIfNeeded() {
        if (mGoogleApiClient == null) {
        	mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
            .addApi(LocationServices.API)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build();
        }
    }
    
    float distanceToCar[] = new float[3];
    DecimalFormat dsitanceFormat = new DecimalFormat("0.0");
    
    @Override
    public void onLocationChanged(Location location) {
    	if(vehicle.latitude!=0 && vehicle.longitude!=0){
			btnDistanceToCar.setEnabled(true);
			
			Location.distanceBetween(vehicle.latitude, vehicle.longitude, location.getLatitude(), location.getLongitude(), distanceToCar);
			float distance = 0;
			
			if(prefs.getString(Constants.PREF_UNITS_OF_MEASURE, "mile").equals("mile")){
				distance = Utils.metersToMiles(distanceToCar[0]);
				if(distance>=1.1)
					tvDistanceToCarLabel.setText("Miles to Car");
				else{
					if(distance>=0.1)
						tvDistanceToCarLabel.setText("Mile to Car");
					else{
						distance = Utils.milesToFeet(distance);
						tvDistanceToCarLabel.setText("Feet to Car");
					}
				}
			}
			else{
				distance = Utils.metersToKm(distanceToCar[0]);
				tvDistanceToCarLabel.setText("KM to Car");
			}
			
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
    	}
    	else{
			tvDistanceToCar.setText("N/A");
    		if(prefs.getString(Constants.PREF_UNITS_OF_MEASURE, "mile").equals("mile")){
				tvDistanceToCarLabel.setText("Miles to Car");
    		}
			else{
				tvDistanceToCarLabel.setText("KM to Car");
			}
    	}

        if (mGoogleApiClient != null) {
        	mGoogleApiClient.disconnect();
        }
    }
	
	@Override
    public void onConnected(Bundle connectionHint) {
		LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient,
                REQUEST,
                this);
    }
	
    /**
     * Implementation of {@link OnConnectionFailedListener}.
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
    	//Do nothing
    }
    
    private long lastVehicleUpdateTimeMillis = 0;
    
    @Override
	public void onResume() {
    	if(lastVehicleUpdateTimeMillis == 0 || System.currentTimeMillis() - lastVehicleUpdateTimeMillis > 60000){
    		lastVehicleUpdateTimeMillis = System.currentTimeMillis();
	    	new GetVehicleRequest(getActivity().getApplicationContext()).execute("vehicles/"+vehicle.id+".json");
    	}
    	
    	if(isAdded()){
	    	DbHelper dbHelper = DbHelper.getInstance(getActivity());
			vehicle = dbHelper.getVehicle(vehicle.id);
			dbHelper.close();
			updateFragment();
    	}
    	
		Utils.gaTrackScreen(getActivity(), "Driver Details Screen");
	    super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGoogleApiClient != null) {
        	mGoogleApiClient.disconnect();
        }
    }

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub
		
	}
    
//    class GetTripsTask extends BaseRequestAsyncTask{
//		
//		public GetTripsTask(Context context) {
//			super(context);
//		}
//		
//		@Override
//		protected void onPreExecute() {
//			super.onPreExecute();
//		}
//
//		@Override
//		protected JSONObject doInBackground(String... params) {
//			SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.US);
//			
//			DbHelper dbHelper = DbHelper.getInstance(getActivity());
//			SQLiteDatabase db = dbHelper.getReadableDatabase();
//			
//			Cursor c = db.query(TripEntry.TABLE_NAME, 
//					new String[]{TripEntry._ID},
//					TripEntry.COLUMN_NAME_VEHICLE_ID + " = " + vehicle.id, null, null, null, null);
//			int tripsInDb = c.getCount();
//			c.close();
//			db.close();
//			dbHelper.close();
//			
//			Calendar cStart = Calendar.getInstance();
//			Calendar cEnd = Calendar.getInstance();
//			cStart.setTimeInMillis(System.currentTimeMillis());
//			if(tripsInDb!=0)
//				cStart.add(Calendar.DAY_OF_YEAR, -7);
//			else
//				cStart.add(Calendar.YEAR, -20);
//			cEnd.setTimeInMillis(System.currentTimeMillis());
//			
//	        requestParams.add(new BasicNameValuePair("page", "1"));
//	        requestParams.add(new BasicNameValuePair("per_page", "1000"));
//	        requestParams.add(new BasicNameValuePair("start_time", sdf.format(cStart.getTime())));
//	        requestParams.add(new BasicNameValuePair("end_time", sdf.format(cEnd.getTime())));
//			return super.doInBackground(params);
//		}
//		
//		@Override
//		protected void onPostExecute(JSONObject result) {
//			super.onPostExecute(result);
//		}
//		
//		@Override
//		protected void onError(String message) {
//			//Do nothing
//		}
//		
//		@Override
//		protected void onSuccess(JSONObject responseJSON) throws JSONException {
//			JSONArray tripsJSON = responseJSON.getJSONArray("trips");
//			
//			ArrayList<Trip> trips = new ArrayList<Trip>();
//			
//			for (int i = 0; i < tripsJSON.length(); i++) {
//				JSONObject tripJSON = tripsJSON.getJSONObject(i);
//				
//				Trip t = new Trip(
//						tripJSON.optLong("id"), 
//						tripJSON.optInt("harsh_events_count"), 
//						Utils.fixTimezoneZ(tripJSON.optString("start_time")), 
//						Utils.fixTimezoneZ(tripJSON.optString("end_time")), 
//						tripJSON.optDouble("mileage"));
//				t.grade = tripJSON.optString("grade");
//				t.fuelLevel = tripJSON.optInt("fuel_left",-1);
//				t.fuelUnit = tripJSON.optString("fuel_unit");
//				trips.add(t);
//			}
//			
//			DbHelper dbHelper = DbHelper.getInstance(getActivity());
//			dbHelper.saveTrips(vehicle.id, trips);
//			dbHelper.close();
//			
//			super.onSuccess(responseJSON);
//		}
//	}

}