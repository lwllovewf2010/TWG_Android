package com.modusgo.ubi;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.LatLngBounds.Builder;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.modusgo.ubi.db.DbHelper;
import com.modusgo.ubi.db.VehicleContract.VehicleEntry;
import com.modusgo.ubi.requesttasks.BaseRequestAsyncTask;
import com.modusgo.ubi.utils.Utils;

public class FindMyCarActivity extends MainActivity implements ConnectionCallbacks,
OnConnectionFailedListener, LocationListener {
	
	private LocationClient mLocationClient;

    // These settings are the same as the settings for the map. They will in fact give you updates
    // at the maximal rates currently possible.
    private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(5000)         // 5 seconds
            .setFastestInterval(1000)    // every second
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	private Location myLocation;
    
	public static final String EXTRA_POINTS = "points";
	
	long vehicleId = 0;
	
	MapView mapView;
    GoogleMap map;
    
    Button btnStart;
    TextView tvDistance;
    TextView tvDistanceUnits;
    TextView tvTime;
    
    LinearLayout llInfo;
    TextView tvInfo;
    
    ArrayList<LatLng> points;
    
    private boolean locationUpdatesEnabled = false; 
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_find_my_car);
		super.onCreate(savedInstanceState);
		
		setActionBarTitle("FIND MY CAR");
		
		if(savedInstanceState!=null){
			vehicleId = savedInstanceState.getLong(VehicleEntry._ID);
		}
		else if(getIntent()!=null){
			vehicleId = getIntent().getLongExtra(VehicleEntry._ID,0);
		}

		DbHelper dHelper = DbHelper.getInstance(this);
		vehicle = dHelper.getVehicleShort(vehicleId);
		dHelper.close();
		
		points = new ArrayList<LatLng>();
		
		// Gets the MapView from the XML layout and creates it
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        map = mapView.getMap();
        //map.getUiSettings().setMyLocationButtonEnabled(false);
        //map.setMyLocationEnabled(true);
        map.setOnMapLoadedCallback(new OnMapLoadedCallback() {
			@Override
			public void onMapLoaded() {
				updateActivity();
			}
		});
        
        MapsInitializer.initialize(this);     
        
        btnStart = (Button) findViewById(R.id.btnStart);
        tvDistance = (TextView) findViewById(R.id.tvDistance);
        tvDistanceUnits = (TextView) findViewById(R.id.tvDistanceUnits);
        tvTime = (TextView) findViewById(R.id.tvTime);
        llInfo = (LinearLayout) findViewById(R.id.llInfo);
        tvInfo = (TextView) findViewById(R.id.tvInfo);
        
        btnStart.setEnabled(false);
        btnStart.setBackgroundDrawable(Utils.getButtonBgStateListDrawable(prefs.getString(Constants.PREF_BR_BUTTONS_BG_COLOR, Constants.BUTTON_BG_COLOR)));
        try{
        	btnStart.setTextColor(Color.parseColor(prefs.getString(Constants.PREF_BR_BUTTONS_TEXT_COLOR, Constants.BUTTON_TEXT_COLOR)));
        }
	    catch(Exception e){
	    	e.printStackTrace();
	    }
        
        btnStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mLocationClient!=null && mLocationClient.isConnected()){
					btnStart.setText("Start");
					mLocationClient.disconnect();
					locationUpdatesEnabled = false;
				}
				else{
					llInfo.setVisibility(View.GONE);
					tvInfo.setText("Retrieving your current location...");
					tvInfo.setVisibility(View.VISIBLE);
					btnStart.setEnabled(false);
			        setUpLocationClientIfNeeded();
			        mLocationClient.connect();
					locationUpdatesEnabled = true;
				}
			}
		});
        
	}
	
	private void drawPolyline(){
		PolylineOptions options = new PolylineOptions();
		
		for (int i = 0; i < points.size(); i++) {
			options.add(points.get(i));
		}
		
		int color = Color.parseColor("#00AEEF");
		map.addPolyline(options.color(color).width(8));
		
	}
	
	private void updateActivity(){
		if(points!=null && points.size()>0){
			PolylineOptions options = new PolylineOptions();
			final Builder builder = LatLngBounds.builder();

			map.clear();
			for (LatLng point : points) {
				options.add(point);
				builder.include(point);
				map.addMarker(new MarkerOptions().position(point).anchor(0.5f, 0.5f).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_geofence_point)));
			}
			options.add(points.get(0));
			
			LatLngBounds llb = builder.build();
			
			int color = Color.parseColor("#FFFFFF");
			map.addPolyline(options.color(color).width(8));
			
			CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(llb, 150);
	        map.animateCamera(cameraUpdate);
	        
		}
		else{
	        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(vehicle.latitude, vehicle.longitude), 10);
	        map.animateCamera(cameraUpdate);
		}
		map.addMarker(new MarkerOptions().position(new LatLng(vehicle.latitude, vehicle.longitude)).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_car)));
		
		btnStart.setEnabled(true);
	}
	
	private void updateMap(){
		map.clear();
		
		drawPolyline();
		
		map.addMarker(new MarkerOptions().position(new LatLng(vehicle.latitude, vehicle.longitude)).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_car)));
		map.addMarker(new MarkerOptions().position(new LatLng(myLocation.getLatitude(), myLocation.getLongitude())).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_mylocation)).anchor(0.5f, 0.5f));
	}
	
	private void setUpLocationClientIfNeeded() {
        if (mLocationClient == null) {
            mLocationClient = new LocationClient(
                    getApplicationContext(),
                    this,  // ConnectionCallbacks
                    this); // OnConnectionFailedListener
        }
    }
	
	@Override
    public void onLocationChanged(Location location) {
		if(tvInfo.getVisibility()==View.VISIBLE){
			Builder builder = LatLngBounds.builder();
			builder.include(new LatLng(vehicle.latitude, vehicle.longitude));
			builder.include(new LatLng(location.getLatitude(), location.getLongitude()));
			CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(builder.build(), 150);
	        map.animateCamera(cameraUpdate);

	        llInfo.setVisibility(View.GONE);
			tvInfo.setText("Calculating route...");
			tvInfo.setVisibility(View.VISIBLE);		
		}
		
        myLocation = location;
        
        new GetRouteTask(this).execute();
        
        updateMap();
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
        llInfo.setVisibility(View.GONE);
		tvInfo.setText("Unable to retrieve your current location");
		tvInfo.setVisibility(View.VISIBLE);
    }
    
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong(VehicleEntry._ID, vehicleId);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onPause() {
		mapView.onPause();
        if (mLocationClient != null) {
            mLocationClient.disconnect();
        }
		super.onPause();
	}
	
	@Override
    public void onResume() {
        mapView.onResume();

		Utils.gaTrackScreen(this, "Find My Car Screen");
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
    
    /** POLYLINE DECODER - http://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java **/
    private List<LatLng> decodePolyline(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();

        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((double) lat / 1E5, (double) lng / 1E5);
            poly.add(p);
        }

        return poly;
    }
    
    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;
        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }
        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }
        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }
    
    class GetRouteTask extends BaseRequestAsyncTask{

		public GetRouteTask(Context context) {
			super(context);
		}
		
		@Override
		protected JSONObject doInBackground(String... params) {
			
			baseUrl = "http://maps.googleapis.com/maps/api/directions/json";
			checkSuccess = false;
			requestParams.clear();
			requestParams.add(new BasicNameValuePair("origin", myLocation.getLatitude()+","+myLocation.getLongitude()));
			requestParams.add(new BasicNameValuePair("destination", vehicle.latitude+","+vehicle.longitude));
			requestParams.add(new BasicNameValuePair("sensor", "false"));
			requestParams.add(new BasicNameValuePair("mode", "walking"));
			
			System.out.println(requestParams);
			
			return super.doInBackground(params);
		}
		
		@Override
		protected void onSuccess(JSONObject responseJSON) throws JSONException {
			if(locationUpdatesEnabled){
				JSONArray routesJSON = responseJSON.getJSONArray("routes");
				if(routesJSON.length()>0){
					
					long distanceForSegment = routesJSON.getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("distance").getInt("value");
					String timeForSegment = routesJSON.getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("duration").getString("text");
	
	                JSONArray steps = routesJSON.getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps");
	
	                points.clear();
	                for(int i=0; i < steps.length(); i++) {
	                    String polyline = steps.getJSONObject(i).getJSONObject("polyline").getString("points");
	                    points.addAll(decodePolyline(polyline));
	                }
	                
	                DecimalFormat df = new DecimalFormat("0.00");
	                
	                if(prefs.getString(Constants.PREF_UNITS_OF_MEASURE, "mile").equals("mile")){
		                tvDistance.setText(""+df.format(Utils.metersToMiles(distanceForSegment)));
	        			tvDistanceUnits.setText("Miles");
	        		}
	        		else{
		                tvDistance.setText(""+df.format(Utils.metersToKm(distanceForSegment)));
		                tvDistanceUnits.setText("KM");
	        		}
	                tvTime.setText("Time: "+timeForSegment);
	                
	                updateMap();
	                
	                btnStart.setText("Stop");
	                
	                llInfo.setVisibility(View.VISIBLE);
					tvInfo.setVisibility(View.GONE);
				}
				else{
					llInfo.setVisibility(View.GONE);
					tvInfo.setText("Unable to calculate route");
					tvInfo.setVisibility(View.VISIBLE);
					
					btnStart.setText("Start");
					if(mLocationClient!=null && mLocationClient.isConnected())
						mLocationClient.disconnect();
				}
				btnStart.setEnabled(true);
			}
			super.onSuccess(responseJSON);
		}
	}

}