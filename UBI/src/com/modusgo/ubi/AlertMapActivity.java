package com.modusgo.ubi;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

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
import com.modusgo.ubi.Trip.EventType;
import com.modusgo.ubi.Trip.Point;
import com.modusgo.ubi.db.AlertContract.AlertEntry;
import com.modusgo.ubi.db.DbHelper;
import com.modusgo.ubi.db.RouteContract.RouteEntry;
import com.modusgo.ubi.db.TripContract.TripEntry;
import com.modusgo.ubi.db.VehicleContract.VehicleEntry;
import com.modusgo.ubi.requesttasks.BaseRequestAsyncTask;
import com.modusgo.ubi.utils.Utils;

public class AlertMapActivity extends MainActivity {

	public static final String EXTRA_ALERT_ID = "alertId";
	
	long vehicleId = 0;
	long alertId = 0;
	Alert alert;
	Trip trip;
	
	MapView mapView;
    GoogleMap map;
    TextView tvDescription;
    TextView tvDate;
    TextView tvAddress;
    
    CameraUpdate cameraUpdate;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_alert_map);
		super.onCreate(savedInstanceState);
		
		setActionBarTitle("ALERT");
		
		if(savedInstanceState!=null){
			vehicleId = savedInstanceState.getLong(VehicleEntry._ID);
			alertId = savedInstanceState.getLong(EXTRA_ALERT_ID);
		}
		else if(getIntent()!=null){
			vehicleId = getIntent().getLongExtra(VehicleEntry._ID,0);
			alertId = getIntent().getLongExtra(EXTRA_ALERT_ID,0);
		}

		DbHelper dHelper = DbHelper.getInstance(this);
		vehicle = dHelper.getVehicleShort(vehicleId);
		dHelper.close();
		
		tvDescription = (TextView) findViewById(R.id.tvDescription);
		tvDate = (TextView) findViewById(R.id.tvDate);
		tvAddress = (TextView) findViewById(R.id.tvAddress);
		
		// Gets the MapView from the XML layout and creates it
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        map = mapView.getMap();
        if(map!=null){//no google play services installed -> map == null
	        map.getUiSettings().setMyLocationButtonEnabled(false);
	        map.setOnMapLoadedCallback(new OnMapLoadedCallback() {
				@Override
				public void onMapLoaded() {
					cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(37.8430094,-95.0098992), 1);
			        updateMap(true);
				}
			});
        }
        
        MapsInitializer.initialize(this);     

        alert = getAlertFromDB();
        trip = getTripFromDB();
        
        updateActivity();
        
		new GetAlertTask(this).execute("vehicles/"+vehicle.id+"/alerts/"+alertId);
	}
	
	private void updateActivity(){

		setActionBarTitle(alert.title.toUpperCase(Locale.US));
		tvDescription.setText(alert.description);
		
		SimpleDateFormat sdfFrom = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.getDefault());
		SimpleDateFormat sdfTo = new SimpleDateFormat("MM/dd/yyyy KK:mm aa z", Locale.getDefault());
		
		TimeZone tzFrom = TimeZone.getTimeZone(Constants.DEFAULT_TIMEZONE);
		sdfFrom.setTimeZone(tzFrom);
		TimeZone tzTo = TimeZone.getTimeZone(prefs.getString(Constants.PREF_TIMEZONE_OFFSET, Constants.DEFAULT_TIMEZONE));
		sdfTo.setTimeZone(tzTo);
		
		try {
			tvDate.setText(sdfTo.format(sdfFrom.parse(alert.timestamp)));
		} catch (ParseException e) {
			tvDate.setText(alert.timestamp);
			e.printStackTrace();
		}
		
		if(!TextUtils.isEmpty(alert.address))
			tvAddress.setText(alert.address);
		else{
			new FetchAddresses(this).execute();
		}
	}
	
	private void updateMap(boolean animate){
        //Trip
		if(map!=null){
			map.clear();
			final Builder builder = LatLngBounds.builder();	
			
			if(trip!=null){
				if(trip.route.size()>0){
					PolylineOptions options = new PolylineOptions();
					for (LatLng point : trip.route) {
						options.add(point);
						builder.include(point);
					}
					
					int color = Color.parseColor("#009900");
					map.addPolyline(options.color(color).width(8).zIndex(1));
					
					map.addMarker(new MarkerOptions().position(trip.route.get(0)).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_start)).title(vehicle.address));
					map.addMarker(new MarkerOptions().position(trip.route.get(trip.route.size()-1)).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_finish)).title(vehicle.address));
				}
			}
			
			//Geofence
			if(alert!=null){
				if(alert.geofence!=null && alert.geofence.size()>0){
					PolylineOptions options = new PolylineOptions();
					for (LatLng point : alert.geofence) {
						options.add(point);
						builder.include(point);
						map.addMarker(new MarkerOptions().position(point).anchor(0.5f, 0.5f).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_geofence_point)));
					}
					
					options.add(alert.geofence.get(0));		
					int color = Color.parseColor("#FFFFFF");
					map.addPolyline(options.color(color).width(8));
				}
				
				//Alert marker
				LatLng location = new LatLng(alert.location.latitude, alert.location.longitude);
				builder.include(location);
				map.addMarker(new MarkerOptions().position(location).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_unknown)).title(vehicle.address));
				if(animate){			
					try{
						int mapPadding = (int) Math.min(mapView.getHeight()*0.2f, mapView.getWidth()*0.2f);
						cameraUpdate = CameraUpdateFactory.newLatLngBounds(builder.build(), mapPadding);
					}
					catch(IllegalStateException e){
						cameraUpdate = CameraUpdateFactory.newLatLngZoom(location, 15);
					}
				}
			}
			
			map.animateCamera(cameraUpdate);
		}
		
	}
	
	private Alert getAlertFromDB(){
		Alert alert = new Alert(alertId);
		
		DbHelper dbHelper = DbHelper.getInstance(this);
		SQLiteDatabase db = dbHelper.openDatabase();
		Cursor c = db.query(AlertEntry.TABLE_NAME, 
				new String[]{
				AlertEntry._ID,
				AlertEntry.COLUMN_NAME_VEHICLE_ID,
				AlertEntry.COLUMN_NAME_TRIP_ID,
				AlertEntry.COLUMN_NAME_TYPE,
				AlertEntry.COLUMN_NAME_TIMESTAMP,
				AlertEntry.COLUMN_NAME_TITLE,
				AlertEntry.COLUMN_NAME_DESCRIPTION,
				AlertEntry.COLUMN_NAME_LATITUDE,
				AlertEntry.COLUMN_NAME_LONGITUDE,
				AlertEntry.COLUMN_NAME_SEEN_AT,
				AlertEntry.COLUMN_NAME_GEOFENCE,
				AlertEntry.COLUMN_NAME_ADDRESS}, 
				AlertEntry.COLUMN_NAME_VEHICLE_ID+" = ? AND " +AlertEntry._ID + " = ?", new String[]{Long.toString(vehicle.id), Long.toString(alertId)}, null, null, null);
		
		if(c.moveToFirst()){
			alert.vehicleId = c.getLong(1);
			alert.tripId = c.getLong(2);
			alert.type = c.getString(3);
			alert.timestamp = c.getString(4);
			alert.title = c.getString(5);
			alert.description = c.getString(6);
			alert.location = new LatLng(c.getDouble(7), c.getDouble(8));
			alert.seenAt = c.getString(9);
			alert.setGeofence(c.getString(10));
			alert.address = c.getString(11);
		}
		c.close();		
		dbHelper.closeDatabase();
		dbHelper.close();
		
		return alert;
	}
	
	private Trip getTripFromDB(){
		DbHelper dbHelper = DbHelper.getInstance(this);
		SQLiteDatabase db = dbHelper.openDatabase();
		Cursor c = db.query(TripEntry.TABLE_NAME, 
				new String[]{
				TripEntry._ID,
				TripEntry.COLUMN_NAME_EVENTS_COUNT,
				TripEntry.COLUMN_NAME_START_TIME,
				TripEntry.COLUMN_NAME_END_TIME,
				TripEntry.COLUMN_NAME_DISTANCE,
				TripEntry.COLUMN_NAME_AVG_SPEED,
				TripEntry.COLUMN_NAME_MAX_SPEED,
				TripEntry.COLUMN_NAME_GRADE,
				TripEntry.COLUMN_NAME_VIEWED_AT,
				TripEntry.COLUMN_NAME_UPDATED_AT}, 
				TripEntry._ID+" = ?", new String[]{Long.toString(alert.tripId)}, null, null, null);
		
		Trip t = null;
		
		if(c.moveToFirst()){
			t = new Trip(prefs, c.getLong(0), c.getInt(1), c.getString(2), c.getString(3), c.getFloat(4), c.getString(7));
			t.averageSpeed = c.getFloat(5);
			t.maxSpeed = c.getFloat(6);
			t.viewedAt = c.getString(8);
			t.updatedAt = c.getString(9);
		}
		c.close();
		
		if(t!=null){
			c = db.query(RouteEntry.TABLE_NAME, 
					new String[]{
					RouteEntry._ID,
					RouteEntry.COLUMN_NAME_LATITUDE,
					RouteEntry.COLUMN_NAME_LONGITUDE}, 
					RouteEntry.COLUMN_NAME_TRIP_ID+" = ?", new String[]{Long.toString(alert.tripId)}, null, null, RouteEntry._ID+" ASC");
			if(c.moveToFirst()){
				while (!c.isAfterLast()) {
					t.route.add(new LatLng(c.getDouble(1), c.getDouble(2)));
					c.moveToNext();
				}
			}
			c.close();
		}
		
		dbHelper.closeDatabase();
		dbHelper.close();
		
		return t;
	}
	
	class GetAlertTask extends BaseRequestAsyncTask{
		
		public GetAlertTask(Context context) {
			super(context);
		}

		@Override
		protected JSONObject doInBackground(String... params) {
	        requestParams.add(new BasicNameValuePair("vehicle_id", ""+vehicle.id));
	        requestParams.add(new BasicNameValuePair("alert_id", ""+alertId));
			return super.doInBackground(params);
		}
		
		@Override
		protected void onSuccess(JSONObject responseJSON) throws JSONException {
			alert.vehicleId = responseJSON.optLong("vehicle_id");
			alert.tripId = responseJSON.optLong("trip_id");
			alert.type = responseJSON.optString("uuid");
			alert.timestamp = responseJSON.optString("timestamp");
			alert.title = responseJSON.optString("title");
			alert.description = responseJSON.optString("description");
			if(responseJSON.has("location")){
				JSONObject locationJSON = responseJSON.getJSONObject("location");
				alert.location = new LatLng(locationJSON.optDouble("latitude"), locationJSON.optDouble("longitude"));					
			}
			else
				alert.location = new LatLng(0, 0);
			alert.seenAt = responseJSON.optString("seen_at");
			
			if(responseJSON.has("geofence") && responseJSON.get("geofence") instanceof JSONArray){
				alert.setGeofence(responseJSON.getJSONArray("geofence").toString());
			}
			
			DbHelper dbHelper = DbHelper.getInstance(context);
			dbHelper.saveAlert(vehicle.id, alert);
			dbHelper.close();
			
			updateActivity();
			
			if(trip==null)
				new GetTripTask(getApplicationContext()).execute("vehicles/"+vehicle.id+"/trips/"+alert.tripId+".json");
			else
				updateMap(true);
			
			super.onSuccess(responseJSON);
		}
		
		@Override
		protected void onError(String message) {
			// Do nothing
		}
	}
	
	class GetTripTask extends BaseRequestAsyncTask{
		
		public GetTripTask(Context context) {
			super(context);
		}

		@Override
		protected JSONObject doInBackground(String... params) {
	        requestParams.add(new BasicNameValuePair("trip_id", ""+alert.tripId));
	        requestParams.add(new BasicNameValuePair("vehicle_id", ""+vehicle.id));
	        
			return super.doInBackground(params);
		}
		
		@Override
		protected void onSuccess(JSONObject responseJSON) throws JSONException {
			trip = new Trip(prefs, alert.tripId, responseJSON.optInt("harsh_events_count"), Utils.fixTimezoneZ(responseJSON.optString("start_time")), Utils.fixTimezoneZ(responseJSON.optString("end_time")), responseJSON.optDouble("mileage"), responseJSON.optString("grade"));
			
			trip.averageSpeed = responseJSON.optDouble("avg_speed");
			trip.maxSpeed = responseJSON.optDouble("max_speed");
			SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.US);
			sdf.setTimeZone(TimeZone.getTimeZone(Constants.DEFAULT_TIMEZONE));
			trip.viewed = true;
			trip.updatedAt = responseJSON.optString("updated_at");
			trip.viewedAt = sdf.format(Calendar.getInstance().getTime());
			trip.fuel = responseJSON.optInt("fuel_left");
			trip.fuelUnit = responseJSON.optString("fuel_unit");
			
			if(responseJSON.has("route")){
				JSONArray routeJSON = responseJSON.getJSONArray("route");
				for (int i = 0; i < routeJSON.length(); i++) {
					JSONArray pointJSON = routeJSON.getJSONArray(i);
					trip.route.add(new LatLng(pointJSON.optDouble(0), pointJSON.optDouble(1)));					
				}
			}
			
			if(responseJSON.has("speeding")){
				JSONArray speedingJSON = responseJSON.getJSONArray("speeding");
				for (int i = 0; i < speedingJSON.length(); i++) {
					JSONArray speedingRouteJSON = speedingJSON.getJSONObject(i).getJSONArray("route");
					ArrayList<LatLng> speedingRoute = new ArrayList<LatLng>();
					
					for (int j = 0; j < speedingRouteJSON.length(); j++) {
						JSONArray pointJSON = speedingRouteJSON.getJSONArray(j);
						speedingRoute.add(new LatLng(pointJSON.optDouble(0,0), pointJSON.optDouble(1,0)));	
					}
					trip.speedingRoutes.add(speedingRoute);				
				}
			}
			
			if(responseJSON.has("points")){
				JSONArray pointsJSON = responseJSON.getJSONArray("points");
				for (int i = 0; i < pointsJSON.length(); i++) {
					JSONObject pointJSON = pointsJSON.getJSONObject(i);
					
					if(pointJSON.has("location")){
						JSONObject locationJSON = pointJSON.getJSONObject("location");
						Point p = new Point(new LatLng(locationJSON.optDouble("latitude",0), locationJSON.optDouble("longitude",0)),
								getEventType(pointJSON.optString("event")),
								pointJSON.optString("title"), "");
						trip.points.add(p);
					}
				}
			}
			
			DbHelper dHelper = DbHelper.getInstance(AlertMapActivity.this);
			dHelper.saveTrip(vehicle.id, trip);
			dHelper.saveRoute(trip.id, trip.route);
			dHelper.saveSpeedingRoute(trip.id, trip.speedingRoutes);
			dHelper.savePoints(trip.id, trip.points);
			dHelper.close();
			
			super.onSuccess(responseJSON);
		}
		
		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			updateMap(true);
		}
		
		@Override
		protected void onError(String message) {
			// Do nothing
		}
		
		private EventType getEventType(String event){
			switch (event) {
			case "start":
				return EventType.START;
			case "stop":
				return EventType.STOP;
			case "harsh_braking":
				return EventType.HARSH_BRAKING;
			case "harsh_acceleration":
				return EventType.HARSH_ACCELERATION;
			case "call_usage":
				return EventType.CALL_USAGE;
			case "phone_usage":
				return EventType.PHONE_USAGE;
			case "speeding":
				return EventType.SPEEDING;
			default:
				return EventType.UNKNOWN;
			}
		}
	}
	
	class FetchAddresses extends AsyncTask<Void, Void, Boolean> {
    	
    	Context context;
    	
    	public FetchAddresses(Context context) {
    		this.context = context;
		}
    	
    	@Override
    	protected void onPreExecute() {
			tvAddress.setText("Updating address...");
			super.onPreExecute();
    	}

		@Override
		protected Boolean doInBackground(Void... params) {
			
			if(TextUtils.isEmpty(alert.address)){
				Geocoder geocoder;
				List<Address> addresses;
				geocoder = new Geocoder(context, Locale.getDefault());
				try {
					addresses = geocoder.getFromLocation(alert.location.latitude, alert.location.longitude, 1);
					if(addresses.size()>0)
						alert.address = addresses.get(0).getAddressLine(0);
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
				DbHelper dbHelper = DbHelper.getInstance(context);
				dbHelper.saveAlert(vehicle.id, alert);
				dbHelper.close();
				
				return true;
			}
			else
				return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				tvAddress.setText(alert.address);
			}
			else
				tvAddress.setText("Unknown address");
			super.onPostExecute(result);
		}
    }
	
	@Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }	
	
	@Override
    public void onResume() {
        mapView.onResume();
        Utils.gaTrackScreen(this, "Current Location Screen");
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
}