package com.modusgo.ubi;

import java.text.DecimalFormat;
import java.util.ArrayList;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
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
import com.modusgo.ubi.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class GeofenceActivity extends MainActivity {

	long driverId = 0;
	
	MapView mapView;
    GoogleMap map;
    
    Button btnSave;
    TextView tvInstructions;
    TextView tvRadius;
    TextView tvRadiusUnits;
    
    ArrayList<LatLng> points;
    private boolean geofencingStarted = false;
    private boolean mapEnabled = true;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_geofence);
		super.onCreate(savedInstanceState);
		
		setActionBarTitle("TRIP DETAILS");
		
		if(savedInstanceState!=null){
			driverId = savedInstanceState.getLong(VehicleEntry._ID);
		}
		else if(getIntent()!=null){
			driverId = getIntent().getLongExtra(VehicleEntry._ID,0);
		}

		DbHelper dHelper = DbHelper.getInstance(this);
		vehicle = dHelper.getVehicleShort(driverId);
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

		points = new ArrayList<LatLng>();
		
		// Gets the MapView from the XML layout and creates it
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        map = mapView.getMap();
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.setOnMapLoadedCallback(new OnMapLoadedCallback() {
			@Override
			public void onMapLoaded() {
				CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(37.8430094,-95.0098992), 1);
		        map.animateCamera(cameraUpdate);
				new GetGeofenceTask(GeofenceActivity.this).execute("vehicles/"+vehicle.id+"/limits.json");
			}
		});
        map.setOnMapLongClickListener(new OnMapLongClickListener() {
			
			@Override
			public void onMapLongClick(LatLng point) {
				if(mapEnabled){
					if(points.size()>0){
						map.clear();
						points.clear();
						tvRadius.setText("n/a");
						if(prefs.getString(Constants.PREF_UNITS_OF_MEASURE, "mile").equals("mile")){
							tvRadiusUnits.setText("MILES");
						}
						else{
							tvRadiusUnits.setText("KM");
						}
						updateSaveBtn("Finsih");
				        tvInstructions.setText("Tap anywhere on\nthe map to begin setting up\nyour geofence borders");
						geofencingStarted = true;
					}
				}
			}
		});
        
        map.setOnMapClickListener(new OnMapClickListener() {
			
			@Override
			public void onMapClick(LatLng point) {
				if(mapEnabled && geofencingStarted){
					points.add(point);
					drawPolyline();
					updateSaveBtn("Finish");
				}
				if(points.size()>0){
					tvInstructions.setText("Tap anywhere on the map to set next point");
				}
			}
		});
        
        MapsInitializer.initialize(this);     

		mapEnabled = false;
		
        btnSave = (Button) findViewById(R.id.btnSave);
        tvInstructions = (TextView) findViewById(R.id.tvInstructions);
        tvRadius = (TextView) findViewById(R.id.tvRadius);
        tvRadiusUnits = (TextView) findViewById(R.id.tvRadiusUnits);

		btnSave.setText("Loading...");
        btnSave.setEnabled(false);
        btnSave.setBackgroundDrawable(Utils.getButtonBgStateListDrawable(prefs.getString(Constants.PREF_BR_BUTTONS_BG_COLOR, "#f15b2a")));
        try{
        	btnSave.setTextColor(Color.parseColor(prefs.getString(Constants.PREF_BR_BUTTONS_TEXT_COLOR, "#edf1f9")));
        }
	    catch(Exception e){
	    	e.printStackTrace();
	    }
        
        tvInstructions.setText("");
        
        btnSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(geofencingStarted){
					geofencingStarted = false;
					drawClosedPolyline();
					btnSave.setText("Save");
			        tvInstructions.setText("Press and hold anywhere on\nthe map to reset geofence borders");
				}
				else{
					new SetGeofenceTask(GeofenceActivity.this).execute("vehicles/"+vehicle.id+"/limits.json");
				}
			}
		});
	}
	
	private void drawPolyline(){
		map.clear();
		
		PolylineOptions options = new PolylineOptions();
		final Builder builder = LatLngBounds.builder();
		
		for (int i = 0; i < points.size(); i++) {
			options.add(points.get(i));
			if(i!=points.size()-1)
				map.addMarker(new MarkerOptions().position(points.get(i)).anchor(0.5f, 0.5f).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_geofence_point)));
			else
				map.addMarker(new MarkerOptions().position(points.get(i)).anchor(0.5f, 0.5f).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_geofence_start)));
			builder.include(points.get(i));
		}
		
		int color = Color.parseColor("#FFFFFF");
		map.addPolyline(options.color(color).width(8));
		
		LatLngBounds llb = builder.build();
		updateRadius(llb);
	}
	
	private void drawClosedPolyline(){
		map.clear();
		
		PolylineOptions options = new PolylineOptions();
		final Builder builder = LatLngBounds.builder();
		
		for (LatLng point : points) {
			options.add(point);
			builder.include(point);
			map.addMarker(new MarkerOptions().position(point).anchor(0.5f, 0.5f).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_geofence_point)));
		}
		options.add(points.get(0));
		
		int color = Color.parseColor("#FFFFFF");
		map.addPolyline(options.color(color).width(8));
		
		LatLngBounds llb = builder.build();
		updateRadius(llb);
		
	}
	
	private void updateSaveBtn(String title){
		btnSave.setText(title);
		if(points.size()>=3){
			btnSave.setEnabled(true);
		}
		else
			btnSave.setEnabled(false);
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
			
			updateRadius(llb);
			
			int color = Color.parseColor("#FFFFFF");
			map.addPolyline(options.color(color).width(8));
			
			CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(llb, 150);
	        map.animateCamera(cameraUpdate);
	        
	        tvInstructions.setText("Press and hold anywhere on\nthe map to reset geofence borders");
		}
		else{
	        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(vehicle.latitude, vehicle.longitude), 10);
	        map.animateCamera(cameraUpdate);

			geofencingStarted = true;
	        tvInstructions.setText("Tap anywhere on the map to begin\nsetting up your geofence borders");
		}

		updateSaveBtn("Save");
		mapEnabled = true;
	}
	
	private void updateRadius(LatLngBounds llb){
		float[] distance = new float[3];
		Location.distanceBetween(llb.northeast.latitude, llb.northeast.longitude, llb.southwest.latitude, llb.southwest.longitude, distance);
		
		DecimalFormat df = new DecimalFormat("0.0");
		
		if(prefs.getString(Constants.PREF_UNITS_OF_MEASURE, "mile").equals("mile")){
			tvRadius.setText(df.format(Utils.metersToMiles((distance[0]/2f))));
			tvRadiusUnits.setText("MILES");
		}
		else{
			tvRadius.setText(df.format(Utils.metersToKm((distance[0]/2f))));
			tvRadiusUnits.setText("KM");
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong(VehicleEntry._ID, driverId);
		super.onSaveInstanceState(outState);
	}
	
	@Override
    public void onResume() {
        mapView.onResume();
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
    
    class GetGeofenceTask extends BasePostRequestAsyncTask{
		
		public GetGeofenceTask(Context context) {
			super(context);
		}
		
		@Override
		protected void onPreExecute() {
			requestParams.add(new BasicNameValuePair("vehicle_id",""+vehicle.id));
			requestParams.add(new BasicNameValuePair("key", "geofence"));
			requestParams.add(new BasicNameValuePair("active", "true"));
			super.onPreExecute();
		}

		@Override
		protected JSONObject doInBackground(String... params) {
			return super.doInBackground(params);
		}
		
		@Override
		protected void onSuccess(JSONObject responseJSON) throws JSONException {
			points = new ArrayList<LatLng>();
			
			if(responseJSON.has("value") && responseJSON.get("value") instanceof JSONArray){
				JSONArray geofence = responseJSON.getJSONArray("value");
				
				for (int j = 0; j < geofence.length(); j++) {
					try {
						JSONArray point = geofence.getJSONArray(j);
						points.add(new LatLng(point.getDouble(0), point.getDouble(1)));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			
			updateActivity();
			
			super.onSuccess(responseJSON);
		}
		
		@Override
		protected void onError(String message) {
			finish();
			super.onError(message);
		}
	}
    
    class SetGeofenceTask extends BasePostRequestAsyncTask{
		
		public SetGeofenceTask(Context context) {
			super(context);
		}
		
		@Override
		protected void onPreExecute() {
			btnSave.setEnabled(false);
			btnSave.setText("Saving...");
			mapEnabled = false;
			
			requestParams.add(new BasicNameValuePair("vehicle_id",""+vehicle.id));
			requestParams.add(new BasicNameValuePair("key", "geofence"));
			requestParams.add(new BasicNameValuePair("active", "true"));
			try{
		        JSONArray geofence = new JSONArray();
		        for (LatLng point : points) {
		        	JSONArray pointJson = new JSONArray();
		        	pointJson.put(point.latitude);
		        	pointJson.put(point.longitude);
					geofence.put(pointJson); 
				}
		        requestParams.add(new BasicNameValuePair("value", geofence.toString()));
	        }
	        catch(JSONException e){
	        	e.printStackTrace();
	        }
			
			System.out.println(requestParams);
			
			super.onPreExecute();
		}
		
		@Override
		protected void onPostExecute(JSONObject result) {
			btnSave.setEnabled(true);
			btnSave.setText("Save");
			mapEnabled = true;
			super.onPostExecute(result);
		}
		
		@Override
		protected void onSuccess(JSONObject responseJSON) throws JSONException {
			finish();
			super.onSuccess(responseJSON);
		}
	}

}